/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.elasticsearch.metrics;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.elasticsearch.endpoints.CatEndpoint;
import com.appdynamics.extensions.elasticsearch.endpoints.CatEndpointsUtil;
import com.appdynamics.extensions.elasticsearch.util.LineUtils;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.base.Strings;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.appdynamics.extensions.elasticsearch.util.Constants.NAME;
import static com.appdynamics.extensions.elasticsearch.util.Constants.PROPERTIES;

public class CatMetricsClient implements Runnable {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(CatMetricsClient.class);
    private static final String UNASSIGNED = "UNASSIGNED";

    private int unassigned;
    private static final Pattern pattern = Pattern.compile("(b|kb|mb|gb|tb|pd|ms|s|m|h|%)$",
            Pattern.CASE_INSENSITIVE);
    private final String metricPrefix;
    private final String uri;
    private final Phaser phaser;
    private final AtomicBoolean heartBeat;
    private final CloseableHttpClient httpClient;
    private final MetricWriteHelper metricWriteHelper;
    private final CatEndpoint catEndpoint;

    public CatMetricsClient(String metricPrefix, String uri, Phaser phaser, AtomicBoolean heartBeat,
                            CloseableHttpClient httpClient, MetricWriteHelper metricWriteHelper,
                            CatEndpoint catEndpoint) {
        this.metricPrefix = metricPrefix;
        this.uri = uri;
        this.heartBeat = heartBeat;
        this.httpClient = httpClient;
        this.metricWriteHelper = metricWriteHelper;
        this.catEndpoint = catEndpoint;
        this.phaser = phaser;
        phaser.register();
    }

    @Override
    public void run() {
        String url = CatEndpointsUtil.getURL(uri, catEndpoint.getEndpoint());
        LOGGER.debug("Url formed is {}", url);
        List<String> response = getResponse(url);
        if (response != null) {
            List<Metric> metrics = fetchMetricsFromResponse(response);
            if (metrics.isEmpty()) {
                LOGGER.debug("No metrics retrieved from endpoint {}", catEndpoint.getEndpoint());
            } else {
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }
        }
        phaser.arriveAndDeregister();
    }

    private List<String> getResponse(String url) {
        List<String> response = HttpClientUtils.getResponseAsLines(httpClient, url);
        if (response == null) {
            LOGGER.error("Response form endpoint {} is null", catEndpoint.getEndpoint());
            return null;
        }
        heartBeat.compareAndSet(false, true);
        if (response.isEmpty() || response.size() == 1) {
            LOGGER.error("Response from endpoint {} is empty", catEndpoint.getEndpoint());
            return null;
        }
        return response;
    }

    private List<Metric> fetchMetricsFromResponse(List<String> response) {
        List<Metric> metrics = new ArrayList<>();
        String headerLine = response.remove(0);
        List<List<String>> _2DResponseList = LineUtils.to2DList(response);
        LOGGER.debug("Header line from endpoint {} is {}", catEndpoint.getEndpoint(), headerLine);
        Map<String, Integer> headerInvertedIndex = LineUtils.getInvertedIndex(headerLine);
        int headerSize = headerInvertedIndex.size();
        List<String> metricPathKeys = catEndpoint.getMetricPathKeys();
        List<Integer> keyOffsets = LineUtils.getMetricKeyOffsets(headerInvertedIndex, metricPathKeys);
        if (keyOffsets.size() != catEndpoint.getMetricPathKeys().size()) {
            LOGGER.error("Could not find all keys {} in header {} for endpoint {}. Check configuration.",
                    metricPathKeys, headerLine, catEndpoint.getEndpoint());
            return metrics;
        }
        for (List<String> line : _2DResponseList) {
            int currentLineSize = line.size();
            if (currentLineSize == headerSize) {
                LinkedList<String> metricTokens = LineUtils.getMetricTokensFromOffsets(line, keyOffsets);
                List<Metric> metricsFromLine = getConfiguredMetricsFromLine(headerInvertedIndex, line, metricTokens);
                if (metricsFromLine == null) {
                    return new ArrayList<>();
                } else {
                    metrics.addAll(metricsFromLine);
                }
            } else {
                LOGGER.debug("Current line {} does not have all entries for header {}. Size of line {}, size of " +
                        "header {}. This could mean that a node or shard is UNASSIGNED ", line, headerLine,
                        currentLineSize, headerSize);
                // line size is small so contains should be OK
                if (line.contains(UNASSIGNED)) {
                    LOGGER.debug("Found a response line with UNASSIGNED for endpoint {}", catEndpoint.getEndpoint());
                    Metric metric = new Metric(UNASSIGNED, String.valueOf(unassigned++), metricPrefix, UNASSIGNED);
                    metrics.add(metric);
                }
            }
        }
        return metrics;
    }

    private List<Metric> getConfiguredMetricsFromLine(Map<String, Integer> headerInvertedIndex, List<String> line,
                                                      LinkedList<String> metricTokens) {
        List<Metric> metrics = new ArrayList<>();
        for (Map<String, ?> metricsConfigured : catEndpoint.getMetrics()) {
            String metricNameConfigured = (String) metricsConfigured.get(NAME);
            Map<String, ?> metricProperties = (Map<String, ?>) metricsConfigured.get(PROPERTIES);
            int metricIndex = headerInvertedIndex.getOrDefault(metricNameConfigured, -1);
            if (!Strings.isNullOrEmpty(metricNameConfigured) && metricIndex != -1) {
                String metricValue = dropTrailingUnits(line.get(metricIndex));
                metricTokens.add(metricNameConfigured);
                String[] tokens = metricTokens.toArray(new String[0]);
                metricTokens.removeLast();
                Metric metric;
                if (metricProperties == null || metricProperties.size() == 0) {
                    LOGGER.debug("Creating metric with default properties name {}, value {}, prefix {}, tokens {}",
                            metricNameConfigured, metricValue, metricPrefix, tokens);
                    metric = new Metric(metricNameConfigured, metricValue, metricPrefix, tokens);
                } else {
                    LOGGER.debug("Creating metric name {}, value {}, prefix {}, tokens {}, properties {}",
                            metricNameConfigured, metricValue, metricPrefix, tokens, metricProperties);
                    metric = new Metric(metricNameConfigured, metricValue, metricProperties, metricPrefix, tokens);
                }
                metrics.add(metric);
            } else {
                LOGGER.error("The metric is not configured correctly. Skipping metrics collection for endpoint {}",
                        catEndpoint.getEndpoint());
                return null;
            }
        }
        return metrics;
    }

    private String dropTrailingUnits(String metricValue) {
        return pattern.matcher(metricValue).replaceAll("");
    }
}

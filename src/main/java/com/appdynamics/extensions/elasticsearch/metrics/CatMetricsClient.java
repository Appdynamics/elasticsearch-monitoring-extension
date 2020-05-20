/*
 * Copyright (c) 2020 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.elasticsearch.metrics;

import com.appdynamics.extensions.MetricWriteHelper;
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

import static com.appdynamics.extensions.elasticsearch.util.Constants.ENDPOINT;
import static com.appdynamics.extensions.elasticsearch.util.Constants.METRICS;
import static com.appdynamics.extensions.elasticsearch.util.Constants.METRIC_PATH_KEYS;
import static com.appdynamics.extensions.elasticsearch.util.Constants.NAME;
import static com.appdynamics.extensions.elasticsearch.util.Constants.PROPERTIES;

public class CatMetricsClient implements Runnable {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(CatMetricsClient.class);
    private static final Pattern pattern = Pattern.compile("(b|kb|mb|gb|tb|pd|ms|s|m|h|%)$",
            Pattern.CASE_INSENSITIVE);
    private final String metricPrefix;
    private final String uri;
    private final Phaser phaser;
    private final AtomicBoolean heartBeat;
    private final CloseableHttpClient httpClient;
    private final MetricWriteHelper metricWriteHelper;
    private final Map<String, ?> catEndpoint;

    public CatMetricsClient(String metricPrefix, String uri, Phaser phaser, AtomicBoolean heartBeat,
                            CloseableHttpClient httpClient, MetricWriteHelper metricWriteHelper,
                            Map<String, ?> catEndpoint) {
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
        String url = CatEndpointsUtil.getURL(uri, (String) catEndpoint.get(ENDPOINT));
        LOGGER.debug("Url formed is {}", url);
        List<String> response = getResponse(url);
        if (response != null) {
            List<Metric> metrics = fetchMetricsFromResponse(response);
            if (metrics.isEmpty()) {
                LOGGER.debug("No metrics retrieved from endpoint {}", catEndpoint.get(ENDPOINT));
            } else {
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }
        }
        phaser.arriveAndDeregister();
    }

    private List<String> getResponse(String url) {
        List<String> response = HttpClientUtils.getResponseAsLines(httpClient, url);
        if (response == null) {
            LOGGER.error("Response form endpoint {} is null", catEndpoint.get(ENDPOINT));
            return null;
        }
        heartBeat.compareAndSet(false, true);
        if (response.isEmpty() || response.size() == 1) {
            LOGGER.error("Response from endpoint {} is empty", catEndpoint.get(ENDPOINT));
            return null;
        }
        return response;
    }

    private List<Metric> fetchMetricsFromResponse(List<String> response) {
        List<Metric> metrics = new ArrayList<>();
        String headerLine = response.remove(0);
        List<List<String>> _2DResponseList = LineUtils.to2DList(response);
        LOGGER.debug("Header line from endpoint {} is {}", catEndpoint.get(ENDPOINT), headerLine);
        Map<String, Integer> headerInvertedIndex = LineUtils.getInvertedIndex(headerLine);
        int headerSize = headerInvertedIndex.size();
        List<String> metricPathKeys = (List<String>) catEndpoint.get(METRIC_PATH_KEYS);
        List<Integer> keyOffsets = LineUtils.getMetricKeyOffsets(headerInvertedIndex, metricPathKeys);
        if (keyOffsets.size() != ((List<String>) catEndpoint.get(METRIC_PATH_KEYS)).size()) {
            LOGGER.error("Could not find all keys {} in header {} for endpoint {}. Check configuration.",
                    metricPathKeys, headerLine, catEndpoint.get(ENDPOINT));
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
            }
        }
        return metrics;
    }

    private List<Metric> getConfiguredMetricsFromLine(Map<String, Integer> headerInvertedIndex, List<String> line,
                                                      LinkedList<String> metricTokens) {
        List<Metric> metrics = new ArrayList<>();
        for (Map<String, ?> metricsConfigured : (List<Map<String, ?>>) catEndpoint.get(METRICS)) {
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
                LOGGER.error("The metric is not configured correctly. Either the metric {} is not the header or " +
                        "response from the endpoint {} or it is empty", metricNameConfigured, catEndpoint.get(ENDPOINT));
            }
        }
        return metrics;
    }

    private String dropTrailingUnits(String metricValue) {
        return pattern.matcher(metricValue).replaceAll("");
    }
}

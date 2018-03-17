/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.elasticsearch;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.http.UrlBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;


public class ElasticSearchMonitorTask implements Runnable{
    private static final Logger logger = Logger.getLogger(ElasticSearchMonitorTask.class);
    private static final String METRIC_SEPARATOR = "|";

    private Map server;
    private MonitorConfiguration configuration;
    private List<Map> catEndPoints;

    public ElasticSearchMonitorTask(MonitorConfiguration configuration, Map server, List<Map> catEndPoints) {
        this.configuration = configuration;
        this.server = server;
        this.catEndPoints = catEndPoints;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        String displayName = (String) server.get("displayName");
        try {
            if (!Strings.isNullOrEmpty(displayName)) {
                logger.debug("Fetching metrics for the server " + displayName);
                fetchMetrics(displayName);
            }
        } catch (Exception e) {
            String msg = "Exception while running the Elastic Search task in the server " + displayName;
            logger.error(msg, e);
            configuration.getMetricWriter().registerError(msg, e);
        } finally {
            long endTime = System.currentTimeMillis() - startTime;
            logger.debug("Elastic Search monitor thread for server " + displayName + " ended. Time taken is " + endTime);
        }

    }

    private void fetchMetrics(String displayName) {
        if (catEndPoints != null && !catEndPoints.isEmpty()) {
            CatMetricsClient catMetricsClient = new CatMetricsClient();
            Map<String,String> metrics = Maps.newHashMap();
            for (Map catApiConfig : catEndPoints) {
                String endPoint = (String) catApiConfig.get("endPoint");
                String metricPrefix = (String) catApiConfig.get("metricPrefix");
                List<String> metricKeys = (List<String>) catApiConfig.get("metricKeys");
                try {
                    UrlBuilder urlBuilder = UrlBuilder.fromYmlServerConfig(server).path(endPoint);
                    String url = urlBuilder.build();
                    String response = HttpClientUtils.getResponseAsStr(configuration.getHttpClient(), url);
                    metrics.putAll(catMetricsClient.extractMetrics(response, metricKeys, metricPrefix));

                } catch (Exception e) {
                    logger.error("Unable to execute the request " + endPoint + " Failed with Error :" + e);
                }

            }
            printMetrics(displayName, metrics);
        } else {
            logger.warn("catEndPoints in config.yml is not configured for Elastic Search server " + displayName);
        }
    }

    private void printMetrics(String displayName, Map<String, String> metrics) {
        StringBuilder sb = new StringBuilder(configuration.getMetricPrefix());
        sb.append(METRIC_SEPARATOR).append(displayName).append(METRIC_SEPARATOR);
        for (Map.Entry<String, String> entry : metrics.entrySet()) {
            printMetric(sb.toString() + entry.getKey(), entry.getValue());
        }
        //printMetric(sb.toString() + "Metric_Count_Ext", String.valueOf(metrics.size()));
    }

    public void printMetric(String metricName, String metricValue) {
        if (metricValue != null) {
            //logger.debug(metricName + " : " + metricValue);
            configuration.getMetricWriter().printMetric(metricName, metricValue, MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
        } else {
            logger.warn("The metric at " + metricName + " is null");
        }
    }
}

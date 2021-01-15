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

package com.appdynamics.extensions.elasticsearch;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.elasticsearch.metrics.CatMetricsClient;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.MetricPathUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.appdynamics.extensions.elasticsearch.util.Constants.DISPLAY_NAME;
import static com.appdynamics.extensions.elasticsearch.util.Constants.ENDPOINT;
import static com.appdynamics.extensions.elasticsearch.util.Constants.METRICS;
import static com.appdynamics.extensions.elasticsearch.util.Constants.METRIC_PATH_KEYS;
import static com.appdynamics.extensions.util.AssertUtils.assertNotNull;

public class ElasticsearchMonitorTask implements AMonitorTaskRunnable {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(ElasticsearchMonitorTask.class);
    private final String serverName;
    private final MonitorContextConfiguration configuration;
    private final MetricWriteHelper metricWriteHelper;
    private final Map<String, ?> server;
    private final List<Map<String, ?>> catEndpoints;

    ElasticsearchMonitorTask(String serverName, MonitorContextConfiguration configuration,
                             MetricWriteHelper metricWriteHelper, Map<String, ?> server,
                             List<Map<String, ?>> catEndpoints) {
        this.serverName = serverName;
        this.configuration = configuration;
        this.metricWriteHelper = metricWriteHelper;
        this.server = server;
        this.catEndpoints = catEndpoints;
    }

    @Override
    public void onTaskComplete() {
        LOGGER.info("Finished collecting metrics for {}", serverName);
    }

    @Override
    public void run() {
        LOGGER.debug("Fetching metrics for the server {}", serverName);
        boolean heartBeat = spawnCatMetricsClientTasks();
        printHeartBeat(heartBeat);
    }

    private void printHeartBeat(boolean heartBeat) {
        List<Metric> metrics = new ArrayList<>();
        String metricName = "HeartBeat";
        String metricValue = heartBeat ? "1" : "0";
        Metric metric = new Metric(metricName, metricValue, configuration.getMetricPrefix(), serverName, metricName);
        metrics.add(metric);
        metricWriteHelper.transformAndPrintMetrics(metrics);
    }

    private boolean spawnCatMetricsClientTasks() {
        Phaser phaser = new Phaser();
        phaser.register();
        String uri = (String) server.get("uri");
        // use this across multiple threads to check if server is up, if any one thread is able to make a connection
        // to the server heartbeat will be updated to true
        AtomicBoolean heartBeat = new AtomicBoolean();
        CloseableHttpClient httpClient = configuration.getContext().getHttpClient();
        catEndpoints.forEach(catEndpoint -> {
            String catDisplayName = (String) catEndpoint.get(DISPLAY_NAME);
            assertNotNull(catDisplayName, "Display name for catEndpoints cannot be empty");
            assertNotNull(catEndpoint.get(ENDPOINT), "Endpoint for catEndpoints cannot be empty");
            assertNotNull(catEndpoint.get(METRIC_PATH_KEYS), "The metricPathKeys for catEndpoints is not configured");
            assertNotNull(catEndpoint.get(METRICS), "The metrics section for catEndpoint is not configured");
            String metricPrefix = MetricPathUtils.buildMetricPath(configuration.getMetricPrefix(), serverName,
                    catDisplayName);
            CatMetricsClient catMetricsClientTask = new CatMetricsClient(metricPrefix, uri, phaser, heartBeat,
                    httpClient, metricWriteHelper, catEndpoint);
            configuration.getContext().getExecutorService().execute(catDisplayName, catMetricsClientTask);
        });
        phaser.arriveAndAwaitAdvance();
        return heartBeat.get();
    }
}

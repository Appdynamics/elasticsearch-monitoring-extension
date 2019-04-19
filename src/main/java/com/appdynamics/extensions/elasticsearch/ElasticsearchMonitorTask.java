/*
 * Copyright (c) 2019 AppDynamics,Inc.
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
import com.appdynamics.extensions.elasticsearch.endpoints.CatEndpoint;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

public class ElasticsearchMonitorTask implements AMonitorTaskRunnable {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(ElasticsearchMonitorTask.class);
    private final String serverName;
    private final MonitorContextConfiguration configuration;
    private final MetricWriteHelper metricWriteHelper;
    private final Map<String, ?> server;
    private final List<CatEndpoint> catEndpoints;

    ElasticsearchMonitorTask(String serverName, MonitorContextConfiguration configuration,
                             MetricWriteHelper metricWriteHelper, Map<String, ?> server,
                             List<CatEndpoint> catEndpoints) {
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
        fetchMetrics();
    }

    private void fetchMetrics() {
        Phaser phaser = new Phaser();
        phaser.register();
        for (CatEndpoint catEndpoint : catEndpoints) {
            
        }
        phaser.arriveAndAwaitAdvance();
//        responses = catEndpoints.parallelStream().map(catEndpoint -> CatEndpointsUtil.getURI((String) server.get("uri"),
//                catEndpoint.getEndpoint())).map(uri -> HttpClientUtils.getResponseAsStr(configuration.getContext().getHttpClient(), uri)).map(this::nothingDo).collect(Collectors.toList());
    }
}

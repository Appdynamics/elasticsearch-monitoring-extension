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

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.elasticsearch.endpoints.CatEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.List;
import java.util.Map;


import static com.appdynamics.extensions.elasticsearch.endpoints.CatEndpointsUtil.getCatEndpoints;
import static com.appdynamics.extensions.elasticsearch.util.Constants.*;
import static com.appdynamics.extensions.util.AssertUtils.assertNotNull;

public class ElasticsearchMonitor extends ABaseMonitor {
    private final static ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private List<CatEndpoint> catEndpoints;

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        assertNotNull(catEndpoints, "Please configure catEndpoints in config.yml");
        getServers().forEach(server -> {
            String serverName = (String) server.get(DISPLAY_NAME);
            assertNotNull(serverName, "The displayName for server cannot be null");
            ElasticsearchMonitorTask task = new ElasticsearchMonitorTask(serverName, getContextConfiguration(),
                    tasksExecutionServiceProvider.getMetricWriteHelper(), server, catEndpoints);
            tasksExecutionServiceProvider.submit(serverName, task);
        });
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(SERVERS);
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        initializeCatEndpoints();
        validateCatEndpoints();
    }

    @Override
    protected void onConfigReload(File file) {
        initializeCatEndpoints();
        validateCatEndpoints();
    }

    private void initializeCatEndpoints() {
        catEndpoints =
                getCatEndpoints((List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(CAT_ENDPOINTS));
    }

    private void validateCatEndpoints() {
        catEndpoints.forEach(this::validateCatEndpoint);
    }

    private void validateCatEndpoint(CatEndpoint catEndpoint) {
        assertNotNull(catEndpoint.getDisplayName(), "display name for catEndpoints cannot be null or empty");
        assertNotNull(catEndpoint.getEndpoint(), "Endpoint for catEndpoints cannot be null or empty");
//        List<String> metricPathKeys =
//                catEndpoint.getMetricPathKeys() == null || catEndpoint.getMetricPathKeys().isEmpty() ? null :
//                        catEndpoint.getMetricPathKeys();
        assertNotNull(catEndpoint.getMetricPathKeys(), "The metricPathKeys for catEndpoints cannot be null or empty");
//        List<Map<String, ?>> metrics = catEndpoint.getMetrics() == null || catEndpoint.getMetrics().isEmpty() ? null
//                : catEndpoint.getMetrics();
        assertNotNull(catEndpoint.getMetrics(), "The metrics section cannot be null");
    }
}

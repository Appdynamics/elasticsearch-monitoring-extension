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

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.elasticsearch.endpoints.CatEndpoint;
import com.appdynamics.extensions.elasticsearch.endpoints.CatEndpointsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.elasticsearch.util.Constants.CAT_ENDPOINTS;
import static com.appdynamics.extensions.elasticsearch.util.Constants.DEFAULT_METRIC_PREFIX;
import static com.appdynamics.extensions.elasticsearch.util.Constants.DISPLAY_NAME;
import static com.appdynamics.extensions.elasticsearch.util.Constants.MONITOR_NAME;
import static com.appdynamics.extensions.elasticsearch.util.Constants.SERVERS;
import static com.appdynamics.extensions.util.AssertUtils.assertNotNull;

public class ElasticsearchMonitor extends ABaseMonitor {

    /**
     * ObjectMapper to read YAML
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * The List of all CatEndpoints configured in config.yml
     */
    private List<CatEndpoint> catEndpoints;

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
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
            final String serverName = (String) server.get(DISPLAY_NAME);
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
        initMonitor();
    }

    @Override
    protected void onConfigReload(File file) {
        initMonitor();
    }

    /**
     * Initialize and validate the CatEndpoints once after a machine agent restart or config reload and reuse for
     * every run
     */
    private void initMonitor() {
        catEndpoints =
                CatEndpointsUtil.getCatEndpoints((List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(CAT_ENDPOINTS));
        validateCatEndpoints();
    }

    private void validateCatEndpoints() {
        catEndpoints.forEach(this::validateCatEndpoint);
    }

    /**
     * Validate {@code CatEndpoint}, and check if the configuration is as expected. Throws runtime exception is
     * configuration is not valid
     *
     * @param catEndpoint {@code CatEndpoint} that has to be validated
     */
    private void validateCatEndpoint(CatEndpoint catEndpoint) {
        assertNotNull(catEndpoint.getDisplayName(), "Display name for catEndpoints cannot be empty");
        assertNotNull(catEndpoint.getEndpoint(), "Endpoint for catEndpoints cannot be empty");
        assertNotNull(catEndpoint.getMetricPathKeys(), "The metricPathKeys for catEndpoints is not configured");
        assertNotNull(catEndpoint.getMetrics(), "The metrics section for catEndpoint is not configured");
    }
}

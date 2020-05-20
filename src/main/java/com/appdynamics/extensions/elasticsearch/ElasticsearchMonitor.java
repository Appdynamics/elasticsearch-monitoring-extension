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

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.elasticsearch.util.Constants.CAT_ENDPOINTS;
import static com.appdynamics.extensions.elasticsearch.util.Constants.DEFAULT_METRIC_PREFIX;
import static com.appdynamics.extensions.elasticsearch.util.Constants.DISPLAY_NAME;
import static com.appdynamics.extensions.elasticsearch.util.Constants.MONITOR_NAME;
import static com.appdynamics.extensions.elasticsearch.util.Constants.SERVERS;
import static com.appdynamics.extensions.util.AssertUtils.assertNotNull;

public class ElasticsearchMonitor extends ABaseMonitor {

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
        getServers().forEach(server -> {
            final String serverName = (String) server.get(DISPLAY_NAME);
            assertNotNull(serverName, "The displayName for server cannot be null");
            final List<Map<String, ?>> catEndpoints =
                    (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(CAT_ENDPOINTS);
            ElasticsearchMonitorTask task = new ElasticsearchMonitorTask(serverName, getContextConfiguration(),
                    tasksExecutionServiceProvider.getMetricWriteHelper(), server, catEndpoints);
            tasksExecutionServiceProvider.submit(serverName, task);
        });
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(SERVERS);
    }
}

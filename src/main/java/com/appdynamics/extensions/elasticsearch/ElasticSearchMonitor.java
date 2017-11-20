/**
 * Copyright 2017 AppDynamics, Inc.
 *
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
import com.appdynamics.extensions.util.AssertUtils;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Map;

public class ElasticSearchMonitor extends ABaseMonitor {

	private static final Logger logger = Logger.getLogger(ElasticSearchMonitor.class);

	private class TaskRunnable implements Runnable {
		public void run() {

		}
	}

	protected String getDefaultMetricPrefix() {
		return "Custom Metrics|Elastic Search|";
	}

	public String getMonitorName() {
		return "ElasticSearch Monitor";
	}

	protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
		Map<String, ?> config = configuration.getConfigYml();
		if(config != null) {
			List<Map> servers = (List) config.get("servers");
			List<Map> catEndPoints = (List) config.get("catEndPoints");
			if(servers != null && !servers.isEmpty()) {
				for (Map server : servers) {
					ElasticSearchMonitorTask task = new ElasticSearchMonitorTask(configuration, server, catEndPoints, tasksExecutionServiceProvider.getMetricWriteHelper());
					tasksExecutionServiceProvider.submit(server.get("displayName").toString(), task);
				}
			} else {
				logger.error("There are no servers configured");
			}
		} else {
			logger.error("The config.yml is not loaded due to errors.The task will not run");
		}

	}

	protected int getTaskCount() {
		Map<String, ?> config = configuration.getConfigYml();
		List<Map> servers = (List) config.get("servers");
		AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
		return servers.size();
	}

}

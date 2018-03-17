/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.elasticsearch;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ElasticSearchMonitor extends AManagedMonitor {

	private static final Logger logger = Logger.getLogger(ElasticSearchMonitor.class);
	private static final String METRIC_PREFIX = "Custom Metrics|Elastic Search|";
	private static final String CONFIG_ARG = "config-file";

	private boolean initialized;
	private MonitorConfiguration configuration;

	public ElasticSearchMonitor() {
		System.out.println(logVersion());
	}

	/*
	 * Main execution method that uploads the metrics to AppDynamics Controller
	 *
	 * @see
	 * com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map,
	 * com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
	 */
	public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext arg1) throws TaskExecutionException {
		logVersion();
		if (!initialized) {
			initialize(taskArguments);
		}
		logger.debug("The raw arguments are " + taskArguments);
		configuration.executeTask();
		logger.info("ElasticSearch monitor run completed successfully.");
		return new TaskOutput("Elastic Search monitor run completed successfully.");
	}


	private void initialize(Map<String, String> taskArgs) {
		if(!initialized) {
            logger.debug("Initializing Elastic Search Monitor configuration");
			final String configFilePath = taskArgs.get(CONFIG_ARG);
			MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(this);
			MonitorConfiguration conf = new MonitorConfiguration(METRIC_PREFIX, new TaskRunnable(), metricWriteHelper);
			conf.setConfigYml(configFilePath);
			conf.checkIfInitialized(MonitorConfiguration.ConfItem.CONFIG_YML, MonitorConfiguration.ConfItem.EXECUTOR_SERVICE, MonitorConfiguration.ConfItem.HTTP_CLIENT, MonitorConfiguration.ConfItem.METRIC_PREFIX, MonitorConfiguration.ConfItem.METRIC_WRITE_HELPER);
			this.configuration = conf;
			initialized = true;
		}
	}

	private class TaskRunnable implements Runnable {
		public void run() {
			Map<String, ?> config = configuration.getConfigYml();
			if(config != null) {
				List<Map> servers = (List) config.get("servers");
				List<Map> catEndPoints = (List) config.get("catEndPoints");
				if(servers != null && !servers.isEmpty()) {
					for (Map server : servers) {
						ElasticSearchMonitorTask task = new ElasticSearchMonitorTask(configuration, server, catEndPoints);
						configuration.getExecutorService().execute(task);
					}
				} else {
					logger.error("There are no servers configured");
				}
			} else {
				logger.error("The config.yml is not loaded due to errors.The task will not run");
			}
		}
	}

	public static String getImplementationVersion() {
		return ElasticSearchMonitor.class.getPackage().getImplementationTitle();
	}

	private String logVersion() {
		String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
		logger.info(msg);
		return msg;
	}

	public static void main(String[] args) throws TaskExecutionException {

		ConsoleAppender ca = new ConsoleAppender();
		ca.setWriter(new OutputStreamWriter(System.out));
		ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
		ca.setThreshold(org.apache.log4j.Level.TRACE);

		logger.getRootLogger().addAppender(ca);

		final ElasticSearchMonitor monitor = new ElasticSearchMonitor();

		final Map<String, String> taskArgs = new HashMap<String, String>();
		taskArgs.put("config-file", "src/main/resources/conf/config.yml");
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					monitor.execute(taskArgs, null);
				} catch (Exception e) {
					logger.error("Error while running the task", e);
				}
			}
		}, 2, 30, TimeUnit.SECONDS);
	}
}

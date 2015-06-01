package com.appdynamics.extensions.elasticsearch;

import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.Map;


public class ElasticSearchMonitorTest {

    public static final String CONFIG_ARG = "config-file";

   /* @Test
    public void testCassandraMonitorExtension() throws TaskExecutionException {
        ElasticSearchMonitor elasticMonitor = new ElasticSearchMonitor();
        Map<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put(CONFIG_ARG, "src/test/resources/conf/config.yaml");
        elasticMonitor.execute(taskArgs, null);
    }*/
}

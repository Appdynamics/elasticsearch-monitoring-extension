/**
 * Copyright 2013 AppDynamics, Inc.
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import com.singularity.ee.util.httpclient.HttpClientWrapper;
import com.singularity.ee.util.httpclient.HttpExecutionRequest;
import com.singularity.ee.util.httpclient.HttpExecutionResponse;
import com.singularity.ee.util.httpclient.HttpOperation;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;
import com.singularity.ee.util.log4j.Log4JLogger;

public class ElasticSearchMonitor extends AManagedMonitor {

    private static Logger logger = Logger.getLogger(ElasticSearchMonitor.class.getName());

    private static final String metricPathPrefix = "Custom Metrics|Elastic Search|";

    private static final String indexStatsResource = "_stats";
    private static final String nodeStatsResource = "_cluster/nodes/stats?all=true";
    private static final String clusterStatsResource = "_cluster/health";


    private String host;
    private String port;

    public ElasticSearchMonitor() {
        logger.setLevel(Level.INFO);
    }

    /*
     * Main execution method that uploads the metrics to AppDynamics Controller
     * 
     * @see
     * com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map,
     * com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
     */
    public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext arg1) throws TaskExecutionException {
        String indexStatsJsonStr = null;
        String nodesStatsJsonStr = null;
        String clusterStatsJsonStr = null;
        try {
            // checks for arguemts in monitor.xml (host and port)
            checkArgs(taskArguments);

            // connects and gets the response string for indices, nodes and
            // cluster
            boolean indexStatsRetrieved = true;
            try {
                indexStatsJsonStr = getJsonResponseString(getIndexStatsResourcePath());
            } catch(Exception e) {
                indexStatsRetrieved = false;    
            }
            boolean nodeStatsRetrieved = true;
            try {
                nodesStatsJsonStr = getJsonResponseString(getNodeStatsResourcePath());
            } catch(Exception e) {
                nodeStatsRetrieved = false;
            }
            boolean clusterStatsRetrieved = true;
            try {
                clusterStatsJsonStr = getJsonResponseString(getClusterStatsResourcePath());
            }catch(Exception e) {
                clusterStatsRetrieved = false;
            }

            // retrieves the desired metrics and uploads them to controller
            if(clusterStatsRetrieved) {
                populateClusterStats(clusterStatsJsonStr);
            }
            if(nodeStatsRetrieved) {
                populateNodeStats(nodesStatsJsonStr);
            }
            if(indexStatsRetrieved) {
                populateIndexStats(indexStatsJsonStr);
            }

            return new TaskOutput("Elastic Search Metric Upload Complete");
        } catch (Exception e) {
            logger.error("Elastic Search Metric upload failed");
            return new TaskOutput("Elastic Search Metric upload failed");
        }
    }

    /**
     * Checks if the arguments supplied from monitor.xml are valid
     *
     * @param taskArguments
     * @throws RuntimeException
     */
    private void checkArgs(Map<String, String> taskArguments) throws RuntimeException {
        if (!taskArguments.containsKey("host") || !taskArguments.containsKey("port") || (taskArguments.get("host") == null)
                || (taskArguments.get("port") == null) || (taskArguments.get("host") == "") || (taskArguments.get("port") == "")) {
            logger.error("Required task arguments are missing in monitor.xml, Please provide elastic search host and port");
            throw new RuntimeException("Required task arguments are missing in monitor.xml, Please provide elastic search host and port");
        }
        host = taskArguments.get("host");
        port = taskArguments.get("port");
    }

    /**
     * Connects to the provided webresource and returns the JSON response string
     *
     * @param resource
     * @return
     * @throws IOException
     */
    private String getJsonResponseString(String resource) {
        IHttpClientWrapper httpClient = HttpClientWrapper.getInstance();
        HttpExecutionRequest request = new HttpExecutionRequest(resource, "", HttpOperation.GET);
        HttpExecutionResponse response = httpClient.executeHttpOperation(request, new Log4JLogger(logger));
        if (response.isExceptionHappened() || response.getStatusCode() == 400) {
            logger.error("Elastic search instance down OR URL "+resource +" not supported");
            throw new RuntimeException("Elastic search instance down OR URL "+resource +" not supported");
        }

        return response.getResponseBody();
    }

    /**
     * Retrieves the desired index stats and uploads them to AppDynamics
     * Controller
     *
     * @param jsonString
     * @throws Exception
     */
    private void populateIndexStats(String jsonString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode indicesRootNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("indices");
            if (indicesRootNode != null && indicesRootNode.size() <= 0) {
                indicesRootNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("_all").path("indices");
            }
            Iterator<String> nodes = indicesRootNode.fieldNames();
            while (nodes.hasNext()) {
                String indexName = nodes.next();
                JsonNode node = indicesRootNode.path(indexName);
                int primarySize = convertBytesToKB(node.path("primaries").path("store").path("size_in_bytes").asInt());
                int size = convertBytesToKB(node.path("total").path("store").path("size_in_bytes").asInt());
                int num_docs = node.path("primaries").path("docs").path("count").asInt();

                String indexMetricPath = "Indices|" + indexName + "|";

                printMetric(indexMetricPath, "primary size", primarySize);
                printMetric(indexMetricPath, "size", size);
                printMetric(indexMetricPath, "num docs", num_docs);
            }
            logger.info("No of indices: " + mapper.readValue(jsonString.getBytes(), JsonNode.class).findValue("indices").size());
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved Index statistics successfully");
            }

        } catch (Exception e) {
            logger.error("Error in retrieving index statistics");
            throw new RuntimeException("Error in retrieving index statistics");
        }
    }

    /**
     * Retrieves the desired node stats and uploads them to AppDynamics
     * Controller
     *
     * @param jsonString
     * @throws Exception
     */
    private void populateNodeStats(String jsonString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode nodesRootNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("nodes");
            Iterator<String> nodes = nodesRootNode.fieldNames();
            while (nodes.hasNext()) {
                JsonNode node = nodesRootNode.path(nodes.next());
                String nodeName = node.path("name").asText();

                int indicesSize = convertBytesToKB(node.path("indices").path("store").path("size_in_bytes").asInt());
                int num_docs = node.path("indices").path("docs").path("count").asInt();
                int open_file_descriptors = node.path("process").path("open_file_descriptors").asInt();

                JsonNode memory = node.path("jvm").path("mem");
                int heap_used = convertBytesToKB(memory.path("heap_used_in_bytes").asInt());
                int heap_committed = convertBytesToKB(memory.path("heap_committed_in_bytes").asInt());
                int non_heap_used = convertBytesToKB(memory.path("non_heap_used_in_bytes").asInt());
                int non_heap_committed = convertBytesToKB(memory.path("non_heap_committed_in_bytes").asInt());
                int threads_count = node.path("jvm").path("threads").path("count").asInt();

                String nodeMetricPath = "Nodes|" + nodeName + "|";

                printMetric(nodeMetricPath, "size of indices", indicesSize);
                printMetric(nodeMetricPath, "num docs", num_docs);
                printMetric(nodeMetricPath, "open file descriptors", open_file_descriptors);

                printMetric(nodeMetricPath, "heap used", heap_used);
                printMetric(nodeMetricPath, "heap committed", heap_committed);
                printMetric(nodeMetricPath, "non heap used", non_heap_used);
                printMetric(nodeMetricPath, "non heap committed", non_heap_committed);
                printMetric(nodeMetricPath, "threads count", threads_count);
            }
            logger.info("No of nodes: " + mapper.readValue(jsonString.getBytes(), JsonNode.class).findValue("nodes").size());
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved Node statistics successfully");
            }
        } catch (Exception e) {
            logger.error("Error in retrieving node statistics");
            throw new RuntimeException("Error in retrieving node statistics");
        }
    }

    /**
     * Retrieves the desired cluster stats and uploads them to AppDynamics
     * Controller
     *
     * @param jsonString
     * @throws Exception
     */
    private void populateClusterStats(String jsonString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode clusterNode = mapper.readValue(jsonString.getBytes(), JsonNode.class);
            String clusterName = clusterNode.path("cluster_name").asText();
            int number_of_nodes = clusterNode.path("number_of_nodes").asInt();
            int number_of_data_nodes = clusterNode.path("number_of_data_nodes").asInt();
            int active_primary_shards = clusterNode.path("active_primary_shards").asInt();
            int active_shards = clusterNode.path("active_shards").asInt();
            int relocating_shards = clusterNode.path("relocating_shards").asInt();
            int initializing_shards = clusterNode.path("initializing_shards").asInt();
            int unassigned_shards = clusterNode.path("unassigned_shards").asInt();
            int status = defineStatus(clusterNode.path("status").asText());

            printMetric(clusterName + "|", "status", status);
            printMetric(clusterName + "|", "number of nodes", number_of_nodes);
            printMetric(clusterName + "|", "number of data nodes", number_of_data_nodes);
            printMetric(clusterName + "|", "active primary shards", active_primary_shards);
            printMetric(clusterName + "|", "active shards", active_shards);
            printMetric(clusterName + "|", "relocating shards", relocating_shards);
            printMetric(clusterName + "|", "initializing shards", initializing_shards);
            printMetric(clusterName + "|", "unassigned shards", unassigned_shards);
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved cluster statistics successfully");
            }
        } catch (Exception e) {
            logger.error("Error in retrieving cluster statistics");
            throw new RuntimeException("Error in retrieving cluster statistics");
        }
    }

    private void printMetric(String metricPath, String metricName, Object metricValue) {
        printMetric(getMetricPrefix() + metricPath, metricName, metricValue, MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
    }

    private void printMetric(String metricPath, String metricName, Object metricValue, String aggregation, String timeRollup, String cluster) {
        MetricWriter metricWriter = super.getMetricWriter(metricPath + metricName, aggregation, timeRollup, cluster);
        metricWriter.printMetric(String.valueOf(metricValue));
    }

    /**
     * Construct the REST Url for Index Stats
     *
     * @return
     */
    private String getIndexStatsResourcePath() {
        return constructUrl() + indexStatsResource;

    }

    /**
     * Construct the REST Url for Node Stats
     *
     * @return
     */
    private String getNodeStatsResourcePath() {
        return constructUrl() + nodeStatsResource;

    }

    /**
     * Construct the REST Url for cluster Stats
     *
     * @return
     */
    private String getClusterStatsResourcePath() {
        return constructUrl() + clusterStatsResource;

    }

    private String constructUrl() {
        return new StringBuilder().append("http://").append(host).append(":").append(port).append("/").toString();
    }

    private String getMetricPrefix() {
        return metricPathPrefix;
    }

    /**
     * Utility function to convert bytes to kilobytes
     *
     * @param bytes
     * @return
     */
    private int convertBytesToKB(int bytes) {
        return (int) Math.round(bytes / 1024.0);
    }

    /**
     * Assigns an integer value to show the cluster status in AppDynamics
     * controller
     *
     * @param status
     * @return
     */
    private int defineStatus(String status) {
        if (status.equals("green")) {
            return 2;
        } else if (status.equals("yellow")) {
            return 1;
        } else {
            return 0;
        }
    }
}

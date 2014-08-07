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

import java.util.Iterator;
import java.util.Map;

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

    private static final String METRIC_SEPARATOR = "|";

	private static final Logger LOGGER = Logger.getLogger("com.singularity.extensions.ElasticSearchMonitor");

    private static final String METRIC_PATH_PREFIX = "Custom Metrics|Elastic Search|";

    private static final String INDEX_STATS_RESOURCE = "_stats";
    private static final String NODE_STATS_RESOURCE_v090 = "_cluster/nodes/stats?all=true";
    private static final String NODE_STATS_RESOURCE_v100 = "_nodes/stats";
    private static final String CLUSTER_STATS_RESOURCE = "_cluster/health";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String host;
    private String port;
    private String elasticSearchVersion;

    public ElasticSearchMonitor() {
        String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
        LOGGER.info(msg);
        System.out.println(msg);
    }

    /*
     * Main execution method that uploads the metrics to AppDynamics Controller
     *
     * @see
     * com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map,
     * com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
     */
    public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext arg1) throws TaskExecutionException {
        try {
        	LOGGER.info("Starting Elastic Search Monitoring Task");
            // checks for arguments in monitor.xml (host and port)
            extractArguments(taskArguments);

            determineElasticSearchVersion();

            populateIndexStats();

            populateNodeStats();

            populateClusterStats();
            
            LOGGER.info("Elastic Search Monitoring Task completed");
            return new TaskOutput("Elastic Search Monitoring Task completed");
        } catch (Exception e) {
            LOGGER.error("Elastic Search Monitoring Task failed", e);
            return new TaskOutput("Elastic Search Monitoring Task failed");
        }
    }

    private void determineElasticSearchVersion() {
        try {
            String baseEsInfo = getJsonResponseString(constructUrl());
            LOGGER.info("Monitoring ElasticSearch: " + baseEsInfo.replaceAll("\\s+", " "));
            JsonNode node = MAPPER.readValue(baseEsInfo.getBytes(), JsonNode.class);
            elasticSearchVersion = node.path("version").path("number").asText();

        } catch (Exception e) {
            throw new RuntimeException("Error getting base Elasticsearch info: ", e);
        }
    }

    /**
     * Checks if the arguments supplied from monitor.xml are valid and extracts required host and port arguments.
     *
     * @param taskArguments Argument map
     * @throws java.lang.IllegalArgumentException if the host and port are either not provided or are null or empty strings
     */
    private void extractArguments(Map<String, String> taskArguments) throws IllegalArgumentException {
        if (argumentInvalid(taskArguments, "host") || argumentInvalid(taskArguments, "port")) {
            throw new IllegalArgumentException("Required task arguments are missing in monitor.xml, Please provide elastic search host and port");
        }
        host = taskArguments.get("host");
        port = taskArguments.get("port");
    }

    private boolean argumentInvalid(Map<String, String> taskArguments, String argumentKey) {
        String value = taskArguments.get(argumentKey);
        return value == null || "".equals(value.trim());
    }

    /**
     * Connects to the provided web resource and returns the JSON response string
     *
     * @param resource The URL for the resource
     * @return The JSON response string
     */
    private String getJsonResponseString(String resource) {
        IHttpClientWrapper httpClient = HttpClientWrapper.getInstance();
        HttpExecutionRequest request = new HttpExecutionRequest(resource, "", HttpOperation.GET);
        HttpExecutionResponse response = httpClient.executeHttpOperation(request, new Log4JLogger(LOGGER));
        if (response.isExceptionHappened() || response.getStatusCode() >= 400) {
            throw new RuntimeException("Elastic search instance down OR URL " + resource + " not supported");
        }
        return response.getResponseBody();
    }

    /**
     * Retrieves the desired index stats and uploads them to AppDynamics Controller
     *
     * @throws Exception
     */
    private void populateIndexStats() throws Exception {
        try {
            String jsonString = getJsonResponseString(getIndexStatsResourcePath());
            JsonNode indicesRootNode = MAPPER.readValue(jsonString.getBytes(), JsonNode.class).path("indices");
            if (indicesRootNode != null && indicesRootNode.size() <= 0) {
                indicesRootNode = MAPPER.readValue(jsonString.getBytes(), JsonNode.class).path("_all").path("indices");
            }
            if (indicesRootNode != null) {
                Iterator<String> nodes = indicesRootNode.fieldNames();
                while (nodes.hasNext()) {
                    String indexName = nodes.next();
                    JsonNode node = indicesRootNode.path(indexName);
                    int primarySize = convertBytesToKB(node.path("primaries").path("store").path("size_in_bytes").asInt());
                    int size = convertBytesToKB(node.path("total").path("store").path("size_in_bytes").asInt());
                    int num_docs = node.path("primaries").path("docs").path("count").asInt();

                    String indexMetricPath = "Indices|" + indexName + METRIC_SEPARATOR;

                    printMetric(indexMetricPath, "primary size", primarySize);
                    printMetric(indexMetricPath, "size", size);
                    printMetric(indexMetricPath, "num docs", num_docs);
                }
                LOGGER.debug("No of indices: " + MAPPER.readValue(jsonString.getBytes(), JsonNode.class).findValue("indices").size());
                LOGGER.debug("Retrieved Index statistics successfully");
            }

        } catch (Exception e) {
            LOGGER.error("Error in retrieving index statistics: ", e);
        }
    }

    /**
     * Retrieves the desired node stats and uploads them to AppDynamics
     * Controller
     *
     * @throws Exception
     */
    private void populateNodeStats() throws Exception {
        try {
            String jsonString = getJsonResponseString(getNodeStatsResourcePath());
            JsonNode nodesRootNode = MAPPER.readValue(jsonString.getBytes(), JsonNode.class).path("nodes");
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

                String nodeMetricPath = "Nodes|" + nodeName + METRIC_SEPARATOR;

                printMetric(nodeMetricPath, "size of indices", indicesSize);
                printMetric(nodeMetricPath, "num docs", num_docs);
                printMetric(nodeMetricPath, "open file descriptors", open_file_descriptors);

                printMetric(nodeMetricPath, "heap used", heap_used);
                printMetric(nodeMetricPath, "heap committed", heap_committed);
                printMetric(nodeMetricPath, "non heap used", non_heap_used);
                printMetric(nodeMetricPath, "non heap committed", non_heap_committed);
                printMetric(nodeMetricPath, "threads count", threads_count);
            }
            LOGGER.debug("No of nodes: " + MAPPER.readValue(jsonString.getBytes(), JsonNode.class).findValue("nodes").size());
            LOGGER.debug("Retrieved Node statistics successfully");
        } catch (Exception e) {
        	LOGGER.error("Error in retrieving node statistics: ", e);
        }
    }

    /**
     * Retrieves the desired cluster stats and uploads them to AppDynamics
     * Controller
     *
     * @throws Exception
     */
    private void populateClusterStats() throws Exception {
        try {
            String jsonString = getJsonResponseString(getClusterStatsResourcePath());
            JsonNode clusterNode = MAPPER.readValue(jsonString.getBytes(), JsonNode.class);
            String clusterName = clusterNode.path("cluster_name").asText();
            if ("".equals(clusterName)) {
                LOGGER.error("Cluster not configured, so no cluster stats available");
            } else {
                int number_of_nodes = clusterNode.path("number_of_nodes").asInt();
                int number_of_data_nodes = clusterNode.path("number_of_data_nodes").asInt();
                int active_primary_shards = clusterNode.path("active_primary_shards").asInt();
                int active_shards = clusterNode.path("active_shards").asInt();
                int relocating_shards = clusterNode.path("relocating_shards").asInt();
                int initializing_shards = clusterNode.path("initializing_shards").asInt();
                int unassigned_shards = clusterNode.path("unassigned_shards").asInt();
                int status = defineStatus(clusterNode.path("status").asText());

                String clusterMetricPath = clusterName + METRIC_SEPARATOR;
				printMetric(clusterMetricPath, "status", status);
                printMetric(clusterMetricPath, "number of nodes", number_of_nodes);
                printMetric(clusterMetricPath, "number of data nodes", number_of_data_nodes);
                printMetric(clusterMetricPath, "active primary shards", active_primary_shards);
                printMetric(clusterMetricPath, "active shards", active_shards);
                printMetric(clusterMetricPath, "relocating shards", relocating_shards);
                printMetric(clusterMetricPath, "initializing shards", initializing_shards);
                printMetric(clusterMetricPath, "unassigned shards", unassigned_shards);
                LOGGER.debug("Retrieved cluster statistics successfully");
            }
        } catch (Exception e) {
        	LOGGER.error("Error in retrieving cluster statistics: ", e);
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

    private String getIndexStatsResourcePath() {
        return constructUrl() + INDEX_STATS_RESOURCE;
    }

    private String getNodeStatsResourcePath() {
        if (elasticSearchVersion.startsWith("1")) {
            return constructUrl() + NODE_STATS_RESOURCE_v100;
        } else {
            return constructUrl() + NODE_STATS_RESOURCE_v090;
        }
    }

    private String getClusterStatsResourcePath() {
        return constructUrl() + CLUSTER_STATS_RESOURCE;
    }

    private String constructUrl() {
        return "http://" + host + ":" + port + "/";
    }

    private String getMetricPrefix() {
        return METRIC_PATH_PREFIX;
    }

    private int convertBytesToKB(int bytes) {
        return (int) Math.round(bytes / 1024.0);
    }

    /**
     * Assigns an integer value to show the cluster status in AppDynamics controller
     * green -> 2
     * yellow -> 1
     * red -> 0
     *
     * @param status Status string (green, yellow, or red)
     * @return corresponding integer value (2, 1, or 0)
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

    public static String getImplementationVersion() {
        return ElasticSearchMonitor.class.getPackage().getImplementationTitle();
    }
}

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

import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


public class MetricCheckIT {
    private MetricAPIService metricAPIService;
    private CustomDashboardAPIService customDashboardAPIService;
    private static final String METRIC_NAME = "${metric}";
    private String metricPathEndpoint;

    @Before
    public void setup() {
        metricAPIService = IntegrationTestUtils.initializeMetricAPIService();
        customDashboardAPIService = IntegrationTestUtils.initializeCustomDashboardAPIService();
        metricPathEndpoint = "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application" +
                "%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CElasticsearch%7C" + METRIC_NAME + "&time" +
                "-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON";
    }

    @Test
    public void whenInstanceIsUpThenHeartBeatIs1ForServer() throws UnsupportedEncodingException {
        assertThat(metricAPIService, is(notNullValue()));
        String encodedMetricName = URLEncoder.encode("Server1|HeartBeat", StandardCharsets.UTF_8.toString());
        String endpoint = StringUtils.replace(metricPathEndpoint, METRIC_NAME, encodedMetricName);
        JsonNode jsonNode = metricAPIService.getMetricData("", endpoint);
        assertThat(jsonNode, is(notNullValue()));
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
        int heartBeat = (valueNode == null || valueNode.size() == 0) ? 0 : valueNode.get(0).asInt();
        assertThat(heartBeat, is(equalTo(1)));
    }

    @Test
    public void checkTotalNumberOfMetricsReportedIsGreaterThan1() throws UnsupportedEncodingException {
        assertThat(metricAPIService, is(notNullValue()));
        String encodedMetricName = URLEncoder.encode("Metrics Uploaded", StandardCharsets.UTF_8.toString());
        String endpoint = StringUtils.replace(metricPathEndpoint, METRIC_NAME, encodedMetricName);
        JsonNode jsonNode = metricAPIService.getMetricData("", endpoint);
        assertThat(jsonNode, is(notNullValue()));
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
        int metricsUploaded = (valueNode == null || valueNode.size() == 0) ? 0 : valueNode.get(0).asInt();
        assertThat("Metrics uploaded is not greater than 1. Metrics Uploaded = " + metricsUploaded,
                metricsUploaded > 1, is(true));
    }

    @Test
    public void whenAliasIsAppliedThenCheckMetricName() throws UnsupportedEncodingException {
        assertThat(metricAPIService, is(notNullValue()));
        String encodedMetricName = URLEncoder.encode("Server1|Cluster Stats|docker-cluster|Nodes (count)", StandardCharsets.UTF_8.toString());
        String endpoint = StringUtils.replace(metricPathEndpoint, METRIC_NAME, encodedMetricName);
        JsonNode jsonNode = metricAPIService.getMetricData("", endpoint);
        assertThat(jsonNode, is(notNullValue()));
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
        String metricName = (valueNode == null || valueNode.size() == 0) ? "" : valueNode.get(0).toString().replace("\"", "");
        assertThat(metricName,is(equalTo("Custom Metrics|Elasticsearch|Server1|Cluster Stats|docker-cluster|Nodes (count)")));
    }

    @Test
    public void checkDashboardsUploaded() {
        assertThat(customDashboardAPIService, is(notNullValue()));
        JsonNode allDashboardsNode = customDashboardAPIService.getAllDashboards();
        assertThat("No dashboard found", allDashboardsNode, is(notNullValue()));
        String dashboardName = "Elasticsearch BTD Dashboard";
        boolean found = false;
        for (JsonNode dashNode : allDashboardsNode) {
            if (dashboardName.equals(JsonUtils.getTextValue(dashNode.get("name")))) {
                found = true;
                break;
            }
        }
        assertThat("Dashboard " + dashboardName + " does not exist", found, is(true));
    }

    @Test
    public void checkMetricCharReplaced() throws UnsupportedEncodingException {
        assertThat(metricAPIService, is(notNullValue()));
        String encodedMetricName = URLEncoder.encode("Server1|Cluster Stats|docker-cluster|pending tasks", StandardCharsets.UTF_8.toString());
        String endpoint = StringUtils.replace(metricPathEndpoint, METRIC_NAME, encodedMetricName);
        JsonNode jsonNode = metricAPIService.getMetricData("", endpoint);
        assertThat(jsonNode, is(notNullValue()));
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
        String metricName = (valueNode == null || valueNode.size() == 0) ? "" : valueNode.get(0).toString().replace("\"", "");
        assertThat(metricName,is(equalTo("Custom Metrics|Elasticsearch|Server1|Cluster Stats|docker-cluster|pending tasks")));
    }
}

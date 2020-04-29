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

package com.appdynamics.extensions.elasticsearch.endpoints;

import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.elasticsearch.util.Constants.CAT_ENDPOINTS;
import static com.appdynamics.extensions.elasticsearch.util.Constants.DISPLAY_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author pradeep.nair
 */
public class CatEndpointsUtilTest {

    @Test
    public void testGetCatEndpoints() {
        Map<String, ?> conf = YmlReader.readFromFileAsMap(new File("src/test/resources/conf/config.yml"));
        List<Map<String, ?>> catEndpointsFromConfig = (List<Map<String, ?>>) conf.get(CAT_ENDPOINTS);
        Map<String, ?> expectedFirstMetric = ((List<Map<String, ?>>) catEndpointsFromConfig.get(0).get("metrics")).get(0);
        List<CatEndpoint> catEndpoints = CatEndpointsUtil.getCatEndpoints(catEndpointsFromConfig);
        assertThat(catEndpoints.size(), is(equalTo(catEndpointsFromConfig.size())));
        CatEndpoint endpoint = catEndpoints.get(0);
        String expectedEndpoint = (String) catEndpointsFromConfig.get(0).get("endpoint");
        assertThat(endpoint.getEndpoint(), is(equalTo(expectedEndpoint)));
        String expectedDisplayName = (String) catEndpointsFromConfig.get(0).get(DISPLAY_NAME);
        assertThat(endpoint.getDisplayName(), is(equalTo(expectedDisplayName)));
        List<String> expectedMetricPathKeys = (List<String>) catEndpointsFromConfig.get(0).get("metricPathKeys");
        assertThat(endpoint.getMetricPathKeys(), is(equalTo(expectedMetricPathKeys)));
        Map<String, ?> actualMetric = endpoint.getMetrics().get(0);
        assertThat(actualMetric, is(equalTo(expectedFirstMetric)));
    }

    @Test
    public void testGetURL() {
        String uri = "http://localhost:9200";
        String endpoint = "/_cat/health?v&h=cluster,st,nodeTotal,nodeData,shardsTotal,shardsPrimary,shardsRelocating," +
                "shardsInitializing,shardsUnassigned,pendingTasks";
        String url = CatEndpointsUtil.getURL(uri, endpoint);
        assertThat(url, is(equalTo("http://localhost:9200/_cat/health?v&h=cluster,st,nodeTotal,nodeData,shardsTotal," +
                "shardsPrimary,shardsRelocating,shardsInitializing,shardsUnassigned,pendingTasks&v&format=text")));
    }
}
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

package com.appdynamics.extensions.elasticsearch.metrics;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.appdynamics.extensions.elasticsearch.util.Constants.CAT_ENDPOINTS;
import static com.appdynamics.extensions.elasticsearch.util.Constants.METRIC_PATH_KEYS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author pradeep.nair
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClientUtils.class, CatMetricsClient.class})
public class CatMetricsClientTest {
    private MetricWriteHelper metricWriteHelper;
    private String metricPrefix;
    private ArgumentCaptor<List> pathCaptor;
    private Map<String, Object> catEndpoint;
    private AtomicBoolean heartBeat;
    private CloseableHttpClient httpClient;
    private final Phaser phaser = new Phaser();

    @Before()
    public void setup() {
        Map<String, ?> conf = YmlReader.readFromFileAsMap(new File("src/test/resources/conf/config.yml"));
        heartBeat = new AtomicBoolean();
        ABaseMonitor baseMonitor = mock(ABaseMonitor.class);
        MonitorContextConfiguration monitorConfiguration = mock(MonitorContextConfiguration.class);
        MonitorContext context = mock(MonitorContext.class);
        httpClient = mock(CloseableHttpClient.class);
        when(baseMonitor.getContextConfiguration()).thenReturn(monitorConfiguration);
        when(monitorConfiguration.getContext()).thenReturn(context);
        MetricPathUtils.registerMetricCharSequenceReplacer(baseMonitor);
        MetricCharSequenceReplacer replacer = MetricCharSequenceReplacer.createInstance(conf);
        when(context.getMetricCharSequenceReplacer()).thenReturn(replacer);
        MetricWriter metricWriter = mock(MetricWriter.class);
        when(baseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
        pathCaptor = ArgumentCaptor.forClass(List.class);
        metricPrefix = "Custom Metrics|Elasticsearch|Cluster1";
        metricWriteHelper = mock(MetricWriteHelper.class);
        PowerMockito.mockStatic(HttpClientUtils.class);
        catEndpoint = ((List<Map<String, Object>>) conf.get(CAT_ENDPOINTS)).get(0);
    }

    @Test
    public void whenResponseIsNullThenDoNothing() {
        PowerMockito.when(HttpClientUtils.getResponseAsLines(any(CloseableHttpClient.class), anyString())).thenReturn(null);
        CatMetricsClient client = new CatMetricsClient(metricPrefix, "http://localhost:9200", phaser, heartBeat,
                httpClient, metricWriteHelper, catEndpoint);
        client.run();
        verify(metricWriteHelper, never()).transformAndPrintMetrics(pathCaptor.capture());
        assertThat(heartBeat.get(), is(false));
    }

    @Test
    public void whenResponseIsEmptyMetricsAreNotPrintedAndHeartBeatIsSetToTrue() {
        PowerMockito.when(HttpClientUtils.getResponseAsLines(any(CloseableHttpClient.class), anyString()))
                .thenReturn(new ArrayList<>());
        CatMetricsClient client = new CatMetricsClient(metricPrefix, "http://localhost:9200", phaser, heartBeat,
                httpClient, metricWriteHelper, catEndpoint);
        client.run();
        verify(metricWriteHelper, never()).transformAndPrintMetrics(pathCaptor.capture());
        assertThat(heartBeat.get(), is(true));
    }

    @Test
    public void canParseMetricsSuccessfullyForValidResponse() {
        PowerMockito.when(HttpClientUtils.getResponseAsLines(any(CloseableHttpClient.class), any()))
                .thenAnswer(invocationOnMock -> Files.readAllLines(Paths.get("src/test/resources/cat_health.txt")));
        CatMetricsClient client = new CatMetricsClient(metricPrefix, "http://localhost:9200", phaser, heartBeat,
                httpClient, metricWriteHelper, catEndpoint);
        client.run();
        verify(metricWriteHelper).transformAndPrintMetrics(pathCaptor.capture());
        // these are metrics before transformations are applied
        List<Metric> actualMetrics = pathCaptor.getValue();
        assertThat(heartBeat.get(), is(true));
        assertThat(actualMetrics.size(), is(3));
        assertThat(actualMetrics.get(0).getMetricPath(), is(equalTo("Custom Metrics|Elasticsearch|Cluster1" +
                "|elasticsearch|status")));
        assertThat(actualMetrics.get(0).getMetricProperties().getConversionValues().get("yellow"), is(1));
        assertThat(actualMetrics.get(1).getMetricPath(), is((equalTo("Custom Metrics|Elasticsearch|Cluster1" +
                "|elasticsearch|shards"))));
        assertThat(actualMetrics.get(1).getMetricValue(), is("5"));
        assertThat(actualMetrics.get(2).getMetricPath(), is((equalTo("Custom Metrics|Elasticsearch|Cluster1" +
                "|elasticsearch|asp"))));
        assertThat(actualMetrics.get(2).getMetricProperties().getAlias(), is(equalTo("Active Shards Percent")));
        assertThat(actualMetrics.get(2).getMetricValue(), is("50.0"));
    }

    @Test
    public void canParseMetricsSuccessfullyForValidResponseWithEmptyMetricKeys() {
        PowerMockito.when(HttpClientUtils.getResponseAsLines(any(CloseableHttpClient.class), any()))
                .thenAnswer(invocationOnMock -> Files.readAllLines(Paths.get("src/test/resources/cat_health.txt")));
        List<String> paths = new ArrayList<>();
        catEndpoint.put(METRIC_PATH_KEYS, paths);
        CatMetricsClient client = new CatMetricsClient(metricPrefix, "http://localhost:9200", phaser, heartBeat,
                httpClient, metricWriteHelper, catEndpoint);
        client.run();
        verify(metricWriteHelper).transformAndPrintMetrics(pathCaptor.capture());
        // these are metrics before transformations are applied
        List<Metric> actualMetrics = pathCaptor.getValue();
        assertThat(heartBeat.get(), is(true));
        assertThat(actualMetrics.size(), is(3));
        assertThat(actualMetrics.get(0).getMetricPath(), is(equalTo("Custom Metrics|Elasticsearch|Cluster1|status")));
        assertThat(actualMetrics.get(0).getMetricProperties().getConversionValues().get("yellow"), is(1));
        assertThat(actualMetrics.get(1).getMetricPath(), is((equalTo("Custom Metrics|Elasticsearch|Cluster1|shards"))));
        assertThat(actualMetrics.get(1).getMetricValue(), is("5"));
        assertThat(actualMetrics.get(2).getMetricPath(), is((equalTo("Custom Metrics|Elasticsearch|Cluster1|asp"))));
        assertThat(actualMetrics.get(2).getMetricProperties().getAlias(), is(equalTo("Active Shards Percent")));
        assertThat(actualMetrics.get(2).getMetricValue(), is("50.0"));
    }

    @Test
    public void whenMetricsConfiguredAndResponseMismatchThenDoNotPrintMetrics() {
        PowerMockito.when(HttpClientUtils.getResponseAsLines(any(CloseableHttpClient.class), any()))
                .thenAnswer(invocationOnMock -> Files.readAllLines(Paths.get("src/test/resources/cat_health_mismatch_header.txt")));
        CatMetricsClient client = new CatMetricsClient(metricPrefix, "http://localhost:9200", phaser, heartBeat,
                httpClient, metricWriteHelper, catEndpoint);
        client.run();
        verify(metricWriteHelper).transformAndPrintMetrics(pathCaptor.capture());
        List<Metric> actualMetrics = pathCaptor.getValue();
        assertThat(actualMetrics.size(), is(2));
        assertThat(heartBeat.get(), is(true));
    }
}
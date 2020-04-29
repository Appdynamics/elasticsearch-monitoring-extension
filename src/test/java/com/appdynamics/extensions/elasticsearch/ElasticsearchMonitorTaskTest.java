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
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.elasticsearch.endpoints.CatEndpoint;
import com.appdynamics.extensions.elasticsearch.metrics.CatMetricsClient;
import com.appdynamics.extensions.executorservice.MonitorExecutorService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.elasticsearch.util.Constants.SERVERS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author pradeep.nair
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CatMetricsClient.class, ElasticsearchMonitorTask.class})
public class ElasticsearchMonitorTaskTest {
    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration monitorConfiguration;
    private ArgumentCaptor<List> pathCaptor;
    private CatEndpoint catEndpoint;
    private String serverName;
    private Map<String, ?> conf;

    @Before
    public void setup() throws Exception {
        serverName = "Cluster1";
        conf = YmlReader.readFromFileAsMap(new File("src/test/resources/conf/config.yml"));
        ABaseMonitor baseMonitor = mock(ABaseMonitor.class);
        monitorConfiguration = mock(MonitorContextConfiguration.class);
        MonitorContext context = mock(MonitorContext.class);
        MonitorExecutorService executorService = mock(MonitorExecutorService.class);
        when(baseMonitor.getContextConfiguration()).thenReturn(monitorConfiguration);
        when(monitorConfiguration.getContext()).thenReturn(context);
        when(monitorConfiguration.getMetricPrefix()).thenReturn("Custom Metrics|Elasticsearch");
        when(context.getExecutorService()).thenReturn(executorService);
        doNothing().when(executorService).execute(anyString(), any(CatMetricsClient.class));
        when(context.getHttpClient()).thenReturn(mock(CloseableHttpClient.class));
        MetricPathUtils.registerMetricCharSequenceReplacer(baseMonitor);
        MetricCharSequenceReplacer replacer = MetricCharSequenceReplacer.createInstance(conf);
        when(context.getMetricCharSequenceReplacer()).thenReturn(replacer);
        MetricWriter metricWriter = mock(MetricWriter.class);
        when(baseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
        pathCaptor = ArgumentCaptor.forClass(List.class);
        metricWriteHelper = mock(MetricWriteHelper.class);
        catEndpoint = mock(CatEndpoint.class);
        when(catEndpoint.getEndpoint()).thenReturn(serverName);
        when(catEndpoint.getDisplayName()).thenReturn("stub");
        CatMetricsClient catMetricsClient = PowerMockito.mock(CatMetricsClient.class);
        PowerMockito.whenNew(CatMetricsClient.class).withAnyArguments().thenReturn(catMetricsClient);
    }

    @Test
    public void whenDoneHeartBeatMetricShouldBePrinted() {
        Map<String, ?> server = ((List<Map<String, ?>>) conf.get(SERVERS)).get(0);
        List<CatEndpoint> catEndpoints = new ArrayList<>();
        catEndpoints.add(catEndpoint);
        ElasticsearchMonitorTask task = new ElasticsearchMonitorTask(serverName, monitorConfiguration,
                metricWriteHelper, server, catEndpoints);
        task.run();
        verify(metricWriteHelper).transformAndPrintMetrics(pathCaptor.capture());
        List<Metric> metrics = pathCaptor.getValue();
        assertThat(metrics.size(), is(1));
        assertThat(metrics.get(0).getMetricPath(), is(equalTo("Custom Metrics|Elasticsearch|Cluster1|HeartBeat")));
    }
}
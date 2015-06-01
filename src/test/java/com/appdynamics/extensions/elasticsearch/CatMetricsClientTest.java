package com.appdynamics.extensions.elasticsearch;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CatMetricsClientTest {

    CatMetricsClient catMetricsClient = new CatMetricsClient();

    @Test
    public void canParseMetricsSuccessfully() throws IOException {
        String payload = Files.toString(new File("src/test/resources/cat_allocation.txt"), Charsets.UTF_8);
        Map<String,String> metrics = catMetricsClient.extractMetrics(payload, Arrays.asList("node"),"Allocation");
        Assert.assertTrue(metrics.containsKey("Allocation|prdaes03data09|shards"));
        Assert.assertTrue(metrics.containsKey("Allocation|prdaes03data09|diskUsed"));
        Assert.assertTrue(metrics.get("Allocation|prdaes03data09|shards").equals("36"));
        Assert.assertTrue(metrics.get("Allocation|prdaes03data09|diskUsed").equals("449976676352"));
    }


    @Test
    public void canParseMetricsSuccessfullyForCatShards() throws IOException {
        String payload = Files.toString(new File("src/test/resources/cat_shards.txt"), Charsets.UTF_8);
        Map<String,String> metrics = catMetricsClient.extractMetrics(payload, Arrays.asList("node","index"),"Shards");
        Assert.assertTrue(!metrics.isEmpty());
    }
    @Test(expected = NullPointerException.class)
    public void doNothingIfPayloadIsNull() throws IOException {
        String payload = null;
        Map<String,String> metrics = catMetricsClient.extractMetrics(payload, Arrays.asList("node"),"");
    }


    @Test(expected = NullPointerException.class)
    public void doNothingIfPayloadIsEmpty() throws IOException {
        String payload = "";
        Map<String,String> metrics = catMetricsClient.extractMetrics(payload, Arrays.asList("node"),"");
    }


    @Test(expected = NullPointerException.class)
    public void canGetMetricsWithNullMetricKeys() throws IOException {
        String payload = Files.toString(new File("src/test/resources/cat_allocation.txt"), Charsets.UTF_8);;
        Map<String,String> metrics = catMetricsClient.extractMetrics(payload, null,"");
    }


    @Test
    public void canGetMetricsWithUnmatchedMetricKeys() throws IOException {
        String payload = Files.toString(new File("src/test/resources/cat_allocation.txt"), Charsets.UTF_8);;
        Map<String,String> metrics = catMetricsClient.extractMetrics(payload, Arrays.asList("nodesd"),"");
        Assert.assertTrue(metrics.isEmpty());
    }

}

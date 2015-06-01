package com.appdynamics.extensions.elasticsearch.config;


import java.util.List;

public class CatApiConfig {
    private String endPoint;
    private String metricPrefix;
    private List<String> metricKeys;

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    public List<String> getMetricKeys() {
        return metricKeys;
    }

    public void setMetricKeys(List<String> metricKeys) {
        this.metricKeys = metricKeys;
    }
}

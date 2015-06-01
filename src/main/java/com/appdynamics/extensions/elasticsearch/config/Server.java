package com.appdynamics.extensions.elasticsearch.config;


public class Server {

    private String host;
    private int port;
    private String displayName;
    private boolean enableJsonMetrics;
    private CatApiConfig[] catEndPoints;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnableJsonMetrics() {
        return enableJsonMetrics;
    }

    public void setEnableJsonMetrics(boolean enableJsonMetrics) {
        this.enableJsonMetrics = enableJsonMetrics;
    }

    public CatApiConfig[] getCatEndPoints() {
        return catEndPoints;
    }

    public void setCatEndPoints(CatApiConfig[] catEndPoints) {
        this.catEndPoints = catEndPoints;
    }
}

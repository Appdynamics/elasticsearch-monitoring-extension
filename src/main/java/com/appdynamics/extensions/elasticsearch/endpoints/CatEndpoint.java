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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * @author pradeep.nair
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatEndpoint {
    private String endpoint;
    private String displayName;
    private List<String> metricPathKeys;
    private List<Map<String, ?>> metrics;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getMetricPathKeys() {
        return metricPathKeys;
    }

    public void setMetricPathKeys(List<String> metricPathKeys) {
        this.metricPathKeys = metricPathKeys;
    }

    public List<Map<String, ?>> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Map<String, ?>> metrics) {
        this.metrics = metrics;
    }
}

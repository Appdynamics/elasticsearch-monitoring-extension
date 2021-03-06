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

package com.appdynamics.extensions.elasticsearch.util;

/**
 * @author pradeep.nair
 */
public class Constants {
    public static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|Elasticsearch";
    public static final String MONITOR_NAME = "ElasticsearchMonitor";
    /**
     *  config params
     */
    public static final String SERVERS = "servers";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ENDPOINT = "endpoint";
    public static final String METRIC_PATH_KEYS = "metricPathKeys";
    public static final String METRICS = "metrics";
    public static final String CAT_ENDPOINTS = "catEndpoints";
    public static final String NAME = "name";
    public static final String PROPERTIES = "properties";
    public static final String ENCRYPTION_KEY = "encryptionKey";
}
/*
 * Copyright (c) 2019 AppDynamics,Inc.
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

import com.appdynamics.extensions.elasticsearch.ElasticsearchMonitor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.elasticsearch.util.Constants.CAT_ENDPOINTS;
import static com.appdynamics.extensions.util.StringUtils.trimLeading;
import static com.appdynamics.extensions.util.StringUtils.trimTrailing;

/**
 * @author pradeep.nair
 */
public class CatEndpointsUtil {

    public static List<CatEndpoint> getCatEndpoints(List<Map<String, ?>> endpoints) {
        final ObjectMapper mapper = ElasticsearchMonitor.getObjectMapper();
        final Map<String, List<Map<String, ?>>> catEndpointsMap = new HashMap<>();
        catEndpointsMap.put(CAT_ENDPOINTS, endpoints);
        final CatEndpoints catEndpoints = mapper.convertValue(catEndpointsMap, CatEndpoints.class);
        return catEndpoints.getCatEndpoints();
    }

    public static String getURL(String uri, String endpoint) {
        final String slash = "/";
        final String verbose = "&v";
        final String format = "&format=text";
        return trimTrailing(uri, slash) + slash + trimLeading(endpoint, slash) + verbose + format;
    }
}

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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author pradeep.nair
 */
public class CatEndpointsUtilTest {

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
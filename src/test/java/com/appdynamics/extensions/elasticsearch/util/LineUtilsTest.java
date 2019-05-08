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

package com.appdynamics.extensions.elasticsearch.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author pradeep.nair
 */
public class LineUtilsTest {

    @Test
    public void testParseLineToList() {
        String lineToBeTested = "This    is a test line";
        List<String> expected = Arrays.asList("This", "is", "a", "test", "line");
        List<String> actual = LineUtils.parseLineToList(lineToBeTested);
        assertEquals(expected, actual);
    }

    @Test
    public void testTo2DList() {
        List<String> listToBeTested = Arrays.asList("Test line 1", "Test    line 2", "test line 3");
        List<List<String>> expected = Arrays.asList(Arrays.asList("Test", "line", "1"), Arrays.asList("Test", "line",
                "2"), Arrays.asList("test", "line", "3"));
        List<List<String>> actual = LineUtils.to2DList(listToBeTested);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetInvertedIndex() {
        String lineToBeTested = "Test line this is";
        Map<String, Integer> expected = new HashMap<>();
        expected.put("Test", 0);
        expected.put("line", 1);
        expected.put("this", 2);
        expected.put("is", 3);
        Map<String, Integer> actual = LineUtils.getInvertedIndex(lineToBeTested);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMetricKeyOffsets() {
        Map<String, Integer> invertedIndex = new HashMap<>();
        invertedIndex.put("Test", 0);
        invertedIndex.put("line", 1);
        invertedIndex.put("this", 2);
        invertedIndex.put("is", 3);
        List<String> offsets = Arrays.asList("line", "Test");
        List<Integer> expected = Arrays.asList(1, 0);
        List<Integer> actual = LineUtils.getMetricKeyOffsets(invertedIndex, offsets);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMetricTokensFromOffsets() {
        List<String> listToBeTested = Arrays.asList("This", "is", "line", "test");
        List<Integer> offsetToBeTested = Arrays.asList(3, 2);
        LinkedList<String> expected = new LinkedList<>(Arrays.asList("test", "line"));
        LinkedList<String> actual = LineUtils.getMetricTokensFromOffsets(listToBeTested, offsetToBeTested);
        assertEquals(expected, actual);
    }
}
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

import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pradeep.nair
 */
public class LineUtils {
    private static final Splitter splitter;

    static {
        splitter = Splitter.on(" ")
                .omitEmptyStrings()
                .trimResults();
    }

    public static List<String> parseLineToList(String line) {
        return splitter.splitToList(line);
    }

    /**
     * converts a list 1D of string to a 2D list of words
     * @param   list A {@code List} of {@code String}
     * @return  2D {@code List} of {@code String}
     */
    public static List<List<String>> to2DList(List<String> list) {
        // mapper function parseLineToList maps each element of list to an ArrayList
        return list.stream().map(LineUtils::parseLineToList).collect(Collectors.toList());
    }

    /**
     * Creates a mapping of word to index
     * @param line A {@code String}
     * @return {@code Map} with word as key and index as value
     */
    public static Map<String, Integer> getInvertedIndex(String line) {
        final Map<String, Integer> invertedMap = new HashMap<>();
        int index = 0;
        for (String word : splitter.split(line)) {
            invertedMap.put(word, index++);
        }
        return invertedMap;
    }

    /**
     *
     * @param   invertedIndex
     * @param   keys
     * @return
     */
    public static List<Integer> getMetricKeyOffsets(Map<String, Integer> invertedIndex, List<String> keys) {
        List<Integer> keyOffsets = new ArrayList<>();
        for (String key : keys) {
            int index = invertedIndex.getOrDefault(key, -1);
            if (index != -1) {
                keyOffsets.add(index);
            }
        }
        return keyOffsets;
    }

    /**
     * returns a new linked list containing only Strings at indexes represented by offsets
     * @param line - list of String
     * @param offsets - list of indices
     * @return
     */
    public static LinkedList<String> getMetricTokensFromOffsets(List<String> line, List<Integer> offsets) {
        return offsets.stream().map(line::get).collect(Collectors.toCollection(LinkedList::new));
    }
}

/*
 * Copyright 2012 Brian Matthews
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.btmatthews.maven.plugins.crx;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Verify the behaviour of the {@link ParameterUtils} helper methods.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.1
 */
public class TestParameterUtils {

    /**
     * Verify that calling {@link ParameterUtils#splitParameter(String)} with {@code null} returns {@code null}.
     */
    @Test
    public void testNull() {
        final String[] result = ParameterUtils.splitParameter(null);
        assertNull(result);
    }

    /**
     * Verify that calling {@link ParameterUtils#splitParameter(String)} with an empty string returns {@code null}.
     */
    @Test
    public void testEmpty() {
        final String[] result = ParameterUtils.splitParameter("");
        assertNull(result);
    }

    /**
     * Verify that calling {@link ParameterUtils#splitParameter(String)} with a blank string returns {@code null}.
     */
    @Test
    public void testBlank() {
        final String[] result = ParameterUtils.splitParameter(" \t\n");
        assertNull(result);
    }

    /**
     * Verify that calling {@link ParameterUtils#splitParameter(String)} when the parameter string has a single item
     * will return an string array with just that item.
     */
    @Test
    public void testOneItem() {
        final String[] result = ParameterUtils.splitParameter("manifest.json");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("manifest.json", result[0]);
    }

    /**
     * Verify that calling {@link ParameterUtils#splitParameter(String)} when the parameter string has multiple items
     * will return an string array with those items.
     */
    @Test
    public void testMultipleItems() {
        final String[] result = ParameterUtils.splitParameter("manifest.json,popup.html,popup.js,icon.png");
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("manifest.json", result[0]);
        assertEquals("popup.html", result[1]);
        assertEquals("popup.js", result[2]);
        assertEquals("icon.png", result[3]);
    }

    /**
     * Verify that calling {@link ParameterUtils#splitParameter(String)} when the parameter string has multiple items
     * and whitespace will return an string array with those items stripped of the leading and trailing whitespace.
     */
    @Test
    public void testMultipleItemsWithWhitespace() {
        final String[] result = ParameterUtils.splitParameter(" manifest.json, popup.html, popup.js ,\nicon.png\t");
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("manifest.json", result[0]);
        assertEquals("popup.html", result[1]);
        assertEquals("popup.js", result[2]);
        assertEquals("icon.png", result[3]);
    }
}

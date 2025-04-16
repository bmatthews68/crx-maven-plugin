package com.buralotech.oss.maven.plugins.crx;

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

import org.codehaus.plexus.util.StringUtils;

/**
 * Static helper methods for processing parameters.
 *
 * @author <a href="mailto:bmatthews68@gmail.com">Brian Matthews</a>
 * @since 1.1.1
 */
public class ParameterUtils {
    /**
     * This static helper method splits comma separated lists of directory inclusion and exclusion rules returning
     * the as a string array.
     *
     * @param parameter A comma separated list of directory inclusion and exclusion rules.
     * @return A string array of the individual inclusion/exclusion rules or {@code null} if the {@code parameter}
     *         was null or blank.
     */
    public static String[] splitParameter(final String parameter) {
        if (StringUtils.isNotBlank(parameter)) {
            return StringUtils.stripAll(StringUtils.split(parameter, ","));
        } else {
            return null;
        }
    }
}

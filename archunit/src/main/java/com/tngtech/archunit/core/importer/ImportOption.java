/*
 * Copyright 2017 TNG Technology Consulting GmbH
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
package com.tngtech.archunit.core.importer;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.tngtech.archunit.PublicAPI;

import static com.tngtech.archunit.PublicAPI.Usage.INHERITANCE;

/**
 * Will be evaluated for every class location, to determine if the class should be imported.<br><br>
 * <b>IMPORTANT</b>: For things like caching to work, it's important, that the behavior of any implementation
 * is constant, i.e. throughout the whole run of a test suite, for any {@link Location} x, the result of
 * <code>importOption.includes(x)</code> must return the same value on consecutive calls for EVERY instance
 * of the custom implementation of {@link ImportOption}.<br>
 * In other words, if you for example create a custom implementation of {@link ImportOption},
 * where you look at some test specific file, if a certain class should be imported, this will
 * cause wrong caching (i.e. the second run will assume, the classes are already cached, because it can't
 * be determined, that the {@link ImportOption} would choose different classes to be selected for this run)
 */
@PublicAPI(usage = INHERITANCE)
public interface ImportOption {
    boolean includes(Location location);

    enum Predefined implements ImportOption {
        /**
         * @see DontIncludeTests
         */
        DONT_INCLUDE_TESTS {
            private final DontIncludeTests dontIncludeTests = new DontIncludeTests();

            @Override
            public boolean includes(Location location) {
                return dontIncludeTests.includes(location);
            }
        },
        DONT_INCLUDE_JARS {
            private DontIncludeJars dontIncludeJars = new DontIncludeJars();

            @Override
            public boolean includes(Location location) {
                return dontIncludeJars.includes(location);
            }
        }
    }

    /**
     * NOTE: This excludes all class files residing in some directory
     * ../target/test-classes/.. or ../build/classes/test/.. (Maven/Gradle standard).
     * Thus it is just a best guess, how tests can be identified,
     * in other environments, it might be necessary, to implement the correct {@link ImportOption} yourself.
     */
    final class DontIncludeTests implements ImportOption {
        private static final String MAVEN_INFIX = "/target/test-classes/";
        private static final String GRADLE_INFIX = "/build/classes/test/";

        private static final Set<String> EXCLUDED_INFIXES = ImmutableSet.of(MAVEN_INFIX, GRADLE_INFIX);

        @Override
        public boolean includes(Location location) {
            for (String infix : EXCLUDED_INFIXES) {
                if (location.contains(infix)) {
                    return false;
                }
            }
            return true;
        }
    }

    final class DontIncludeJars implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.isJar();
        }
    }
}

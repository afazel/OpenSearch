/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.geometry;

import org.opensearch.geometry.utils.GeographyValidator;
import org.opensearch.geometry.utils.GeometryValidator;
import org.opensearch.geometry.utils.WellKnownText;
import org.opensearch.test.OpenSearchTestCase;

public class GeometryValidatorTests extends OpenSearchTestCase {

    public static class NoopValidator implements GeometryValidator {

        @Override
        public void validate(Geometry geometry) {

        }
    }

    public static class OneValidator extends GeographyValidator {
        /**
         * Minimum longitude value.
         */
        private static final double MIN_LON_INCL = -1D;

        /**
         * Maximum longitude value.
         */
        private static final double MAX_LON_INCL = 1D;

        /**
         * Minimum latitude value.
         */
        private static final double MIN_LAT_INCL = -1D;

        /**
         * Maximum latitude value.
         */
        private static final double MAX_LAT_INCL = 1D;

        /**
         * Minimum altitude value.
         */
        private static final double MIN_ALT_INCL = -1D;

        /**
         * Maximum altitude value.
         */
        private static final double MAX_ALT_INCL = 1D;

        public OneValidator() {
            super(true);
        }

        @Override
        protected void checkLatitude(double latitude) {
            if (Double.isNaN(latitude) || latitude < MIN_LAT_INCL || latitude > MAX_LAT_INCL) {
                throw new IllegalArgumentException(
                    "invalid latitude " + latitude + "; must be between " + MIN_LAT_INCL + " and " + MAX_LAT_INCL);
            }
        }

        @Override
        protected void checkLongitude(double longitude) {
            if (Double.isNaN(longitude) || longitude < MIN_LON_INCL || longitude > MAX_LON_INCL) {
                throw new IllegalArgumentException(
                    "invalid longitude " + longitude + "; must be between " + MIN_LON_INCL + " and " + MAX_LON_INCL);
            }
        }

        @Override
        protected void checkAltitude(double zValue) {
            if (Double.isNaN(zValue) == false && (zValue < MIN_ALT_INCL || zValue > MAX_ALT_INCL)) {
                throw new IllegalArgumentException(
                    "invalid altitude " + zValue + "; must be between " + MIN_ALT_INCL + " and " + MAX_ALT_INCL);
            }
        }
    }

    public void testNoopValidator() throws Exception {
        WellKnownText parser = new WellKnownText(true, new NoopValidator());
        parser.fromWKT("CIRCLE (10000 20000 30000)");
        parser.fromWKT("POINT (10000 20000)");
        parser.fromWKT("LINESTRING (10000 20000, 0 0)");
        parser.fromWKT("POLYGON ((300 100, 400 200, 500 300, 300 100), (50 150, 250 150, 200 100))");
        parser.fromWKT("MULTIPOINT (10000 20000, 20000 30000)");
    }

    public void testOneValidator() throws Exception {
        WellKnownText parser = new WellKnownText(true, new OneValidator());
        parser.fromWKT("POINT (0 1)");
        parser.fromWKT("POINT (0 1 0.5)");
        IllegalArgumentException ex;
        ex = expectThrows(IllegalArgumentException.class, () -> parser.fromWKT("CIRCLE (1 2 3)"));
        assertEquals("invalid latitude 2.0; must be between -1.0 and 1.0", ex.getMessage());
        ex = expectThrows(IllegalArgumentException.class, () -> parser.fromWKT("POINT (2 1)"));
        assertEquals("invalid longitude 2.0; must be between -1.0 and 1.0", ex.getMessage());
        ex = expectThrows(IllegalArgumentException.class, () -> parser.fromWKT("LINESTRING (1 -1 0, 0 0 2)"));
        assertEquals("invalid altitude 2.0; must be between -1.0 and 1.0", ex.getMessage());
        ex = expectThrows(IllegalArgumentException.class, () -> parser.fromWKT("POLYGON ((0.3 0.1, 0.4 0.2, 5 0.3, 0.3 0.1))"));
        assertEquals("invalid longitude 5.0; must be between -1.0 and 1.0", ex.getMessage());
        ex = expectThrows(IllegalArgumentException.class, () -> parser.fromWKT(
            "POLYGON ((0.3 0.1, 0.4 0.2, 0.5 0.3, 0.3 0.1), (0.5 1.5, 2.5 1.5, 2.0 1.0))"));
        assertEquals("invalid latitude 1.5; must be between -1.0 and 1.0", ex.getMessage());
        ex = expectThrows(IllegalArgumentException.class, () -> parser.fromWKT("MULTIPOINT (0 1, -2 1)"));
        assertEquals("invalid longitude -2.0; must be between -1.0 and 1.0", ex.getMessage());
    }


}

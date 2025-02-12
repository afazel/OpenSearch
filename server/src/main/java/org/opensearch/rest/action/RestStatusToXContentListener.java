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

package org.opensearch.rest.action;

import org.opensearch.common.xcontent.StatusToXContentObject;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.rest.BytesRestResponse;
import org.opensearch.rest.RestChannel;
import org.opensearch.rest.RestResponse;
import org.opensearch.rest.RestStatus;

import java.util.function.Function;

/**
 * Content listener that extracts that {@link RestStatus} from the response.
 */
public class RestStatusToXContentListener<Response extends StatusToXContentObject> extends RestToXContentListener<Response> {
    private final Function<Response, String> extractLocation;

    /**
     * Build an instance that doesn't support responses with the status {@code 201 CREATED}.
     */
    public RestStatusToXContentListener(RestChannel channel) {
        this(channel, r -> {
            assert false: "Returned a 201 CREATED but not set up to support a Location header";
            return null;
        });
    }

    /**
     * Build an instance that does support responses with the status {@code 201 CREATED}.
     */
    public RestStatusToXContentListener(RestChannel channel, Function<Response, String> extractLocation) {
        super(channel);
        this.extractLocation = extractLocation;
    }

    @Override
    public RestResponse buildResponse(Response response, XContentBuilder builder) throws Exception {
        assert response.isFragment() == false; //would be nice if we could make default methods final
        response.toXContent(builder, channel.request());
        RestResponse restResponse = new BytesRestResponse(response.status(), builder);
        if (RestStatus.CREATED == restResponse.status()) {
            final String location = extractLocation.apply(response);
            if (location != null) {
                restResponse.addHeader("Location", location);
            }
        }
        return restResponse;
    }
}

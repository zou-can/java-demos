package org.example.flink.connector.victoria.http;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.jupiter.api.Assertions.*;
import static org.example.flink.connector.victoria.http.HttpClientTestUtils.*;
import static org.example.flink.connector.victoria.http.HttpResponseType.*;

import org.apache.hc.core5.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpResponseClassifierTest {
    @Test
    void shouldClassify100AsUnhandled() {
        HttpResponse response = httpResponse(100);
        Assertions.assertEquals(UNHANDLED, HttpResponseClassifier.classify(response));
    }

    @Test
    void shouldClassify200AsSuccess() {
        HttpResponse response = httpResponse(200);

        Assertions.assertEquals(
            HttpResponseType.SUCCESS, HttpResponseClassifier.classify(response));
    }

    @Test
    void shouldClassify400AsNonRetryableError() {
        HttpResponse response = httpResponse(400);

        assertEquals(NON_RETRYABLE_ERROR, HttpResponseClassifier.classify(response));
    }

    @Test
    void shouldClassify403AsFatal() {
        HttpResponse response = httpResponse(403);

        assertEquals(FATAL_ERROR, HttpResponseClassifier.classify(response));
    }

    @Test
    void shouldClassify404AsFatal() {
        HttpResponse response = httpResponse(404);

        assertEquals(FATAL_ERROR, HttpResponseClassifier.classify(response));
    }

    @Test
    void shouldClassify429AsRetryableError() {
        HttpResponse response = httpResponse(429);

        assertEquals(RETRYABLE_ERROR, HttpResponseClassifier.classify(response));
    }

    @Test
    void shouldClassify500AsRetryableError() {
        HttpResponse response = httpResponse(500);

        assertEquals(RETRYABLE_ERROR, HttpResponseClassifier.classify(response));
    }
}

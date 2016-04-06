/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
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

package org.wso2.msf4j.internal.router;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;

/**
 * Tests handler interceptor.
 */
public class HandlerInterceptorTest2 extends BaseHandlerInterceptorTest {
    private static final TestInterceptor interceptor1 = new TestInterceptor();
    private static final TestInterceptor interceptor2 = new TestInterceptor();

    private static final TestHandler testHandler = new TestHandler();

    private static String hostname = Constants.HOSTNAME;
    private static final int port = Constants.PORT + 0;

    private static final MicroservicesRunner microservicesRunner = new MicroservicesRunner(port);

    @BeforeClass
    public void setup() throws Exception {
        microservicesRunner
                .deploy(testHandler)
                .addInterceptor(interceptor1)
                .addInterceptor(interceptor2)
                .start();
        baseURI = URI.create(String.format("http://%s:%d", hostname, port));
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
    }

    @BeforeTest
    public void reset() {
        interceptor1.reset();
        interceptor2.reset();
    }

    @Test
    public void testPreException() throws Exception {
        int status = doGet("/test/v1/resource", "X-Request-Type", "PreException");
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), status);

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);
        Assert.assertEquals(1, interceptor1.getNumPreCalls());

        // The second pre-call should not have happened due to exception in the first pre-call
        // None of the post calls should have happened.
        Assert.assertEquals(0, interceptor1.getNumPostCalls());
        Assert.assertEquals(0, interceptor2.getNumPreCalls());
        Assert.assertEquals(0, interceptor2.getNumPostCalls());
    }

    @Test
    public void testPostException() throws Exception {
        int status = doGet("/test/v1/resource", "X-Request-Type", "PostException");
        Assert.assertEquals(Response.Status.OK.getStatusCode(), status);

        Assert.assertEquals(1, interceptor1.getNumPreCalls());
        Assert.assertEquals(1, interceptor1.getNumPostCalls());

        Assert.assertEquals(1, interceptor2.getNumPreCalls());
        Assert.assertEquals(1, interceptor2.getNumPostCalls());
    }

    @Test
    public void testUnknownPath() throws Exception {
        int status = doGet("/unknown/path/test/v1/resource");
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), status);

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);
        Assert.assertEquals(0, interceptor1.getNumPreCalls());
        Assert.assertEquals(0, interceptor1.getNumPostCalls());

        Assert.assertEquals(0, interceptor2.getNumPreCalls());
        Assert.assertEquals(0, interceptor2.getNumPostCalls());
    }
}

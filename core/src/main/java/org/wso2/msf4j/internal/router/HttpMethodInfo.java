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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * HttpMethodInfo is a helper class having state information about the http handler method to be invoked, the handler
 * and arguments required for invocation by the Dispatcher. RequestRouter populates this class and stores in its
 * context as attachment.
 */
public class HttpMethodInfo {

    private final Method method;
    private final Object handler;
    private final Object[] args;
    private HttpStreamHandler httpStreamHandler;
    private static final Logger log = LoggerFactory.getLogger(ChannelChunkResponder.class);

    public HttpMethodInfo(Method method,
                          Object handler,
                          Object[] args) {
        this.method = method;
        this.handler = handler;

        // The actual arguments list to invoke handler method
        this.args = args;
    }

    public HttpMethodInfo(Method method,
                          Object handler,
                          Object[] args,
                          Response responder,
                          HttpStreamer httpStreamer) throws HandlerException {
        this(method, handler, args);

        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method should be void if it accepts chunked requests");
        }
        try {
            method.invoke(handler, args);
        } catch (InvocationTargetException e) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method invocation failed", e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method invocation access failed", e);
        }
        httpStreamHandler = httpStreamer.getHttpStreamHandler();
        if (httpStreamHandler == null) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Streaming unsupported");
        }
        httpStreamHandler.init(responder);
    }

    /**
     * Calls the http resource method.
     */
    public Object invoke() throws Exception {
        try {
            return method.invoke(handler, args);
        } catch (InvocationTargetException e) {
            log.error("Resource method threw an exception", e);
            throw e;
        } catch (Throwable e) {
            log.error("Exception while invoking resource method", e);
            throw e;
        }
    }

    /**
     * If chunk handling is supported provide chunks directly.
     *
     * @param chunk chunk content
     * @param isEnd tru if this is the last chunk
     */
    public void chunk(ByteBuffer chunk, boolean isEnd) {
        if (httpStreamHandler == null) {
            // If the handler method doesn't want to handle chunk request, the httpStreamHandler will be null.
            return;
        }
        try {
            httpStreamHandler.chunk(chunk, isEnd);
        } catch (Throwable e) {
            log.error("Exception while invoking streaming handlers", e);
            httpStreamHandler.error(e);
        }
    }

    public boolean isStreamingSupported() {
        return httpStreamHandler != null;
    }
}

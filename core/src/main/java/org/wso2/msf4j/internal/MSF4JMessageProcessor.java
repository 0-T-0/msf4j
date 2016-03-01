/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.msf4j.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.msf4j.InterceptorException;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.internal.router.HandlerException;
import org.wso2.msf4j.internal.router.HttpMethodInfo;
import org.wso2.msf4j.internal.router.HttpMethodInfoBuilder;
import org.wso2.msf4j.internal.router.HttpResourceModel;
import org.wso2.msf4j.internal.router.PatternPathRouter;

import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 * Process carbon messages for MSF4J.
 */
public class MSF4JMessageProcessor implements CarbonMessageProcessor {


    private static final Logger log = LoggerFactory.getLogger(MSF4JMessageProcessor.class);
    private MicroservicesRegistry microservicesRegistry;
    private static final String MSF4J_MSG_PROC_ID = "MSF4J-CM-PROCESSOR";

    public MSF4JMessageProcessor(MicroservicesRegistry microservicesRegistry) {
        this.microservicesRegistry = microservicesRegistry;
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        Request request = new Request(carbonMessage);
        Response response = new Response(carbonCallback);
        try {
            PatternPathRouter.RoutableDestination<HttpResourceModel> destination = microservicesRegistry
                    .getHttpResourceHandler()
                    .getDestinationMethod(request.getUri(),
                            request.getHttpMethod(),
                            request.getContentType(),
                            request.getAcceptTypes());
            HttpResourceModel resourceModel = destination.getDestination();
            InterceptorExecutor interceptorExecutor = InterceptorExecutor
                    .instance(resourceModel,
                            request,
                            response,
                            microservicesRegistry.getInterceptors());
            if (interceptorExecutor.execPreCalls()) { // preCalls can throw exceptions

                HttpMethodInfoBuilder httpMethodInfoBuilder = HttpMethodInfoBuilder
                        .getInstance()
                        .httpResourceModel(resourceModel)
                        .httpRequest(request)
                        .httpResponder(response)
                        .requestInfo(destination.getGroupNameValues());

                HttpMethodInfo httpMethodInfo = httpMethodInfoBuilder.build();

                if (request.isEomAdded()) {
                    if (httpMethodInfo.isStreamingSupported()) {
                        // TODO: send whole content as a chunk List/Combined
                    } else {
                        Object returnVal = httpMethodInfo.invoke();
                        response.setEntity(returnVal);
                    }
                } else {
                    if (httpMethodInfo.isStreamingSupported()) {
                        // TODO: call chunks
                    } else {
                        // TODO: aggregate chunks
                        Object returnVal = httpMethodInfo.invoke();
                        response.setEntity(returnVal);
                    }
                }
                response.setMediaType(getResponseType(request.getAcceptTypes(),
                        resourceModel.getProducesMediaTypes())); // find an appropriate media type for the response
                response.send();
                interceptorExecutor.execPostCalls(0); // postCalls can throw exceptions
            }
        } catch (HandlerException e) {
            carbonCallback.done(e.getFailureResponse());
        } catch (InterceptorException e) {
            log.warn("Interceptors threw an exception", e);
        } catch (Throwable t) {
            log.warn("Unmapped exception", t);
        }
        return true;
    }

    /**
     * Process accept type considering the produce type and the
     * accept types of the request header.
     *
     * @param acceptTypes accept types of the request.
     * @return processed accept type
     */
    private String getResponseType(List<String> acceptTypes, List<String> producesMediaTypes) {
        String acceptType = MediaType.WILDCARD;
        if (!producesMediaTypes.contains(MediaType.WILDCARD) && acceptTypes != null) {
            acceptType =
                    (acceptTypes.contains(MediaType.WILDCARD)) ? producesMediaTypes.get(0) :
                            producesMediaTypes.stream().filter(acceptTypes::contains).findFirst().get();
        }
        return acceptType;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {

    }

    @Override
    public String getId() {
        return MSF4J_MSG_PROC_ID;
    }
}

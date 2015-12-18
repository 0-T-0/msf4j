/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.mss.example.service;

import org.wso2.carbon.metrics.annotation.Counted;
import org.wso2.carbon.metrics.annotation.Metered;
import org.wso2.carbon.metrics.annotation.Timed;
import org.wso2.carbon.mss.httpmonitoring.HTTPMonitoring;
import org.wso2.carbon.mss.httpmonitoring.HTTPMonitoringDataPublisher;
import org.wso2.carbon.mss.metrics.MetricReporter;
import org.wso2.carbon.mss.metrics.Metrics;

import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * TestService class.
 */
@Path("/test")
@HTTPMonitoring
public class TestService {

    private final Random random = new Random();

    private long total = 0L;

    @PostConstruct
    public void init() {
        Metrics.init(MetricReporter.CONSOLE, MetricReporter.JMX, MetricReporter.DAS);
        HTTPMonitoringDataPublisher.init();
    }

    @PreDestroy
    public void destroy() {
        Metrics.destroy();
        HTTPMonitoringDataPublisher.destroy();
    }

    @GET
    @Path("/rand/{bound}")
    @Metered
    public int getRandomInt(@PathParam("bound") int bound) {
        return random.nextInt(bound);
    }

    @GET
    @Path("/echo/{string}")
    @Timed
    public String echo(@PathParam("string") String string) {
        try {
            Thread.sleep(random.nextInt(5000));
        } catch (InterruptedException e) {
        }
        return string;
    }

    @GET
    @Path("/total/{number}")
    @Counted(monotonic = true)
    public long getTotal(@PathParam("number") int number) {
        return total = total + number;
    }

}

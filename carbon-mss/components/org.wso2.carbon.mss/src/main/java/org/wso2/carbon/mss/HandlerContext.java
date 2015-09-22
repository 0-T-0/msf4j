/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mss;

import org.wso2.carbon.mss.internal.router.HttpResourceHandler;

import java.util.Map;

/**
 * Place holder for information about the environment. Will be passed in during lifecycle management calls of
 * HttpHandlers. Currently has methods to get RunTimeArguments.
 */
public interface HandlerContext {

    /**
     * @return Key Value pairs of runtime arguments.
     */
    Map<String, String> getRuntimeArguments();

    /**
     * @return the {@link org.wso2.carbon.mss.internal.router.HttpResourceHandler} associated with this context,
     * used to let one handler call another internally.
     */
    HttpResourceHandler getHttpResourceHandler();
}

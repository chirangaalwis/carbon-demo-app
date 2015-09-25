/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.strategy.carbon5;

public class CarbonKernelHandlerConstants {
    protected static final String ARTIFACT_NAME = "carbon";
    protected static final String DOCKERFILE_PATH = "/artifact/Dockerfile";
    protected static final String CARBON_KERNEL_ARTIFACT = "wso2carbon-kernel-5.0.0-SNAPSHOT";
    protected static final String CARBON_KERNEL_ARTIFACT_VERSION = "5.0.0-SNAPSHOT";
    protected static final int IMAGE_BUILD_DELAY_IN_MILLISECONDS = 5000;
    protected static final int POD_SCALE_DELAY_IN_MILLISECONDS = 3000;
}

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
package org.wso2.strategy.docker.configuration;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;

public class DockerClientBuilder {
    private static DockerClient DOCKER_CLIENT;

    private static final Logger LOG = LogManager.getLogger(DockerClientBuilder.class);

    public static DockerClient buildDockerClient(String dockerEndPointUrl) throws CarbonKernelHandlerException {
        /*
        implements the singleton design pattern on the DockerClient instance
         */
        try {
            if (DOCKER_CLIENT == null) {
                if (dockerEndPointUrl == null) {
                    DOCKER_CLIENT = DefaultDockerClient.fromEnv().build();
                } else {
                    DOCKER_CLIENT = DefaultDockerClient.builder().uri(dockerEndPointUrl).build();
                }
            }
        } catch (DockerCertificateException exception) {
            String message = "Could not create the Docker Client instance.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }

        return DOCKER_CLIENT;
    }
}

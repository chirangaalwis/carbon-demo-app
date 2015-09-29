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
package org.wso2.strategy.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;

import java.util.*;

public class ContainerStatusChecker {
    private final DockerClient dockerClient;

    private static final int CONTAINER_STARTUP_WAIT_TIME_IN_MILLISECONDS = 1000;
    private static final Logger LOG = LogManager.getLogger(ContainerStatusChecker.class);

    public ContainerStatusChecker(DockerClient client) {
        dockerClient = client;
    }

    public boolean checkContainerExistence(Map<String, String> keyValuePairs) throws CarbonKernelHandlerException {
        Iterator iterator = keyValuePairs.entrySet().iterator();
        boolean exists;
        try {
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) (iterator.next());
                exists = false;
                while (!exists) {
                    for (Container container : dockerClient.listContainers()) {
                        if ((container.command().contains((String) pair.getKey())) && (container.image()
                                .equals(pair.getValue()))) {
                            exists = true;
                            iterator.remove();
                            break;
                        }
                    }
                    if (!exists) {
                        Thread.sleep(CONTAINER_STARTUP_WAIT_TIME_IN_MILLISECONDS);
                    }
                }
            }
            return keyValuePairs.isEmpty();
        } catch (DockerException exception) {
            String message = "Could not check the existence of the Kubernetes-Docker Containers.";
            LOG.error(message);
            throw new CarbonKernelHandlerException(message);
        } catch (InterruptedException exception) {
            String message = "Could not check the existence of the Kubernetes-Docker Containers.";
            LOG.error(message);
            throw new CarbonKernelHandlerException(message);
        }
    }
}

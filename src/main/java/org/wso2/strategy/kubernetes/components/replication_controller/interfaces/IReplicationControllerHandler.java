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
package org.wso2.strategy.kubernetes.components.replication_controller.interfaces;

import io.fabric8.kubernetes.api.model.ReplicationController;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;

/**
 * A Java interface for replication controller handling operations
 */
public interface IReplicationControllerHandler {
    /**
     * creates a replication controller
     *
     * @param controllerName        name of the replication controller
     * @param podLabel              value for pod label
     * @param tomcatDockerImageName Apache Tomcat based Docker Image name
     * @param numberOfReplicas      number of pod replicas to be created
     * @throws CarbonKernelHandlerException
     */
    void createReplicationController(String controllerName, String podLabel, String tomcatDockerImageName,
            int numberOfReplicas) throws CarbonKernelHandlerException;

    /**
     * returns a replication controller corresponding to the controller name
     *
     * @param controllerName name of the replication controller
     * @return a replication controller corresponding to the controller name
     */
    ReplicationController getReplicationController(String controllerName);

    /**
     * returns the number of replica pods that has been already deployed
     *
     * @param controllerName name of the replication controller
     * @return the number of replica pods that has been already deployed
     * @throws CarbonKernelHandlerException
     */
    int getNoOfReplicas(String controllerName) throws CarbonKernelHandlerException;

    /**
     * set a new number of pod replicas to a specified replication controller
     *
     * @param controllerName name of the replication controller
     * @param newReplicas    new number of replicas
     * @throws CarbonKernelHandlerException
     */
    void updateNoOfReplicas(String controllerName, int newReplicas) throws CarbonKernelHandlerException;

    /**
     * set a new Docker image to a specified replication controller
     *
     * @param controllerName name of the replication controller
     * @param dockerImage    new Docker image
     * @throws CarbonKernelHandlerException
     */
    void updateImage(String controllerName, String dockerImage) throws CarbonKernelHandlerException;

    /**
     * deletes the specified replication controller
     *
     * @param controllerName name of the replication controller
     * @return the replication controller that was deleted
     * @throws CarbonKernelHandlerException
     */
    ReplicationController deleteReplicationController(String controllerName) throws CarbonKernelHandlerException;
}
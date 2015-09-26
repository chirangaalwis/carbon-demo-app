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
package org.wso2.strategy.carbon5.interfaces;

import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;

import java.nio.file.Path;

public interface ICarbonKernelHandler {
    /**
     * deploys WSO2 Carbon 5 kernel instances
     *
     * @param tenant       name of the tenant
     * @param replicas     number of deployed replicas of the kernel
     * @param kernelPath   file system path to the WSO2 Carbon kernel to be deployed
     * @param buildVersion build version of the WSO2 Carbon kernel deployed
     * @return true if successfully deployed, else false
     * @throws CarbonKernelHandlerException
     */
    boolean deploy(String tenant, Path kernelPath, String buildVersion, int replicas)
            throws CarbonKernelHandlerException;

    /**
     * make a roll update to the newly deployed kernel build
     *
     * @param tenant       name of the tenant
     * @param kernelPath   file system path to the WSO2 Carbon kernel to be deployed
     * @param buildVersion build version of the WSO2 Carbon kernel deployed
     * @return true if successfully updated, else false
     * @throws CarbonKernelHandlerException
     */
    boolean rollUpdate(String tenant, Path kernelPath, String buildVersion) throws CarbonKernelHandlerException;

    /**
     * scale the number of kernel replicas running
     *
     * @param tenant       name of the tenant
     * @param noOfReplicas latest number of replicas to be deployed
     * @return true if successfully scaled, else false
     * @throws CarbonKernelHandlerException
     */
    boolean scale(String tenant, int noOfReplicas) throws CarbonKernelHandlerException;

    /**
     * returns the number of kernel replicas a particular tenant is running, currently
     *
     * @param tenant name of the tenant
     * @return the number of kernel replicas a particular tenant is running, currently
     * @throws CarbonKernelHandlerException
     */
    int getNoOfReplicas(String tenant) throws CarbonKernelHandlerException;

    /**
     * returns a String message of access IPs for the currently running service by the specified tenant
     *
     * @param tenant tenant which deploys the web artifact
     * @return a String message of access IPs for the currently running service by the specified tenant
     * @throws CarbonKernelHandlerException
     */
    String getServiceAccessIPs(String tenant) throws CarbonKernelHandlerException;

    /**
     * removes the deployed kernel replicas and service of the specified tenant
     *
     * @param tenant name of the tenant
     * @return true if successfully removed, else false
     * @throws CarbonKernelHandlerException
     */
    boolean remove(String tenant) throws CarbonKernelHandlerException;
}

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

import org.wso2.strategy.carbon5.interfaces.ICarbonKernelHandler;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;

import java.util.List;

public class CarbonKernelHandler implements ICarbonKernelHandler {
    public boolean deploy(String tenant, String buildVersion, int replicas) throws CarbonKernelHandlerException {
        return false;
    }

    public int getNoOfReplicas(String tenant) throws CarbonKernelHandlerException {
        return 0;
    }

    public List<String> listExistingBuildArtifacts(String tenant) throws CarbonKernelHandlerException {
        return null;
    }

    public String getServiceAccessIPs(String tenant)
            throws CarbonKernelHandlerException {
        return null;
    }

    public boolean scale(String tenant, int noOfReplicas) throws CarbonKernelHandlerException {
        return false;
    }

    public boolean remove(String tenant) throws CarbonKernelHandlerException {
        return false;
    }
}

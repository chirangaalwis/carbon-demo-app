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
package org.wso2.strategy.kubernetes.configuration;

import io.fabric8.kubernetes.api.KubernetesClient;
import io.fabric8.kubernetes.api.KubernetesFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;

public class KubernetesClientBuilder {
    private static KubernetesClient KUBERNETES_CLIENT;

    private static final Logger LOG = LogManager.getLogger(KubernetesClientBuilder.class);

    public static KubernetesClient buildDockerClient(String kubernetesEndPointUrl) throws CarbonKernelHandlerException {
        /*
        implements the singleton design pattern on the KubernetesClient instance
         */
        if (KUBERNETES_CLIENT == null) {
            if (kubernetesEndPointUrl != null) {
                KUBERNETES_CLIENT = new KubernetesClient(new KubernetesFactory(kubernetesEndPointUrl));
            } else {
                String message = "Could not create the Kubernetes Client instance.";
                LOG.error(message);
                throw new CarbonKernelHandlerException(message);
            }
        }

        return KUBERNETES_CLIENT;
    }
}

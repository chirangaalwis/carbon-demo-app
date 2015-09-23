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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.wso2.strategy.carbon5.interfaces.ICarbonKernelHandler;
import org.wso2.strategy.docker.JavaDockerImageHandler;
import org.wso2.strategy.docker.interfaces.IDockerImageHandler;
import org.wso2.strategy.kubernetes.components.replication_controller.ReplicationControllerHandler;
import org.wso2.strategy.kubernetes.components.replication_controller.interfaces.IReplicationControllerHandler;
import org.wso2.strategy.kubernetes.components.service.ServiceHandler;
import org.wso2.strategy.kubernetes.components.service.interfaces.IServiceHandler;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;
import org.wso2.strategy.miscellaneous.helper.CarbonKernelHandlerHelper;
import org.wso2.strategy.miscellaneous.io.FileOutputThread;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CarbonKernelHandler implements ICarbonKernelHandler {
    private final IDockerImageHandler imageBuilder;
    private final IReplicationControllerHandler replicationControllerHandler;
    private final IServiceHandler serviceHandler;

    private static final Log LOG = LogFactory.getLog(CarbonKernelHandler.class);

    public CarbonKernelHandler(String dockerEndpointURL, String kubernetesEndpointURL) {
        imageBuilder = new JavaDockerImageHandler(dockerEndpointURL);
        replicationControllerHandler = new ReplicationControllerHandler(kubernetesEndpointURL);
        serviceHandler = new ServiceHandler(kubernetesEndpointURL);
    }

    public boolean deploy(String tenant, int replicas) throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper.generateKubernetesComponentIdentifier(tenant,
                CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT_VERSION);
        try {
            if (replicationControllerHandler.getReplicationController(componentName) == null) {
                if (imageBuilder.getExistingImages(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME,
                        CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT_VERSION).size() == 0) {
                    String dockerImageName = buildCarbonDockerImage(tenant,
                            CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT_VERSION);
                    Thread.sleep(CarbonKernelHandlerConstants.IMAGE_BUILD_DELAY_IN_MILLISECONDS);
                    replicationControllerHandler
                            .createReplicationController(componentName, componentName, dockerImageName, replicas);
                    serviceHandler.createService(componentName, componentName);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception exception) {
            String message = "Failed to deploy WSO2-Carbon kernel.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
    }

    public boolean rollUpdate(String tenant) throws CarbonKernelHandlerException {
        try {
            String componentName = CarbonKernelHandlerHelper
                    .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
            boolean artifactsExist = (imageBuilder.getExistingImages(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME,
                    CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT_VERSION).size() > 0);
            boolean deployed = (replicationControllerHandler.getReplicationController(componentName) != null);
            if (artifactsExist && deployed) {
                String dockerImageName = buildCarbonDockerImage(tenant,
                        CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT_VERSION);
                replicationControllerHandler.updateImage(componentName, dockerImageName);
                int currentPodNumber = replicationControllerHandler.getNoOfReplicas(componentName);
                scale(tenant, 0);
                Thread.sleep(CarbonKernelHandlerConstants.POD_SCALE_DELAY_IN_MILLISECONDS);
                scale(tenant, currentPodNumber);
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            String message = "Failed to update the running WSO2-Carbon kernel.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
    }

    public int getNoOfReplicas(String tenant) throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        return replicationControllerHandler.getNoOfReplicas(componentName);
    }

    public String getServiceAccessIPs(String tenant) throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        String ipMessage;
        ipMessage = String.format("Cluster IP: %s\nPublic IP: %s\n\n", serviceHandler.getClusterIP(componentName),
                serviceHandler.getNodePortIP(componentName));
        return ipMessage;
    }

    public boolean scale(String tenant, int noOfReplicas) throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        if (replicationControllerHandler.getReplicationController(componentName) != null) {
            replicationControllerHandler.updateNoOfReplicas(componentName, noOfReplicas);
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(String tenant) throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        try {
            if (replicationControllerHandler.getReplicationController(componentName) != null) {
                final int noPods = 0;
                scale(tenant, noPods);
                replicationControllerHandler.deleteReplicationController(componentName);
                serviceHandler.deleteService(componentName);
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            String message = "Failed to remove the running WSO2-Carbon kernel.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
    }

    private String buildCarbonDockerImage(String tenant, String version) throws CarbonKernelHandlerException {
        List<String> dockerFileContent = setDockerFileContent();
        FileOutputThread outputThread = new FileOutputThread(CarbonKernelHandlerConstants.DOCKERFILE_PATH,
                dockerFileContent);
        outputThread.run();
        DateTime dateTime = new DateTime();
        String now =
                dateTime.getYear() + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + "-" + dateTime
                        .getMillisOfDay();
        version += ("-" + now);
        return imageBuilder.buildImage(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME, version,
                Paths.get(CarbonKernelHandlerConstants.DOCKERFILE_PATH));
    }

    private List<String> setDockerFileContent() throws CarbonKernelHandlerException {
        List<String> dockerFileContent = new ArrayList<>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File kernelArtifact = new File(
                    classLoader.getResource(CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT + ".zip").getFile());
            dockerFileContent.add("FROM java:openjdk-8");
            dockerFileContent.add("MAINTAINER dev@wso2.org");
            //        dockerFileContent.add("ENV WSO2_SOFT_VER=" + CarbonKernelHandlerConstants.CARBON_KERNEL_VERSION);
            dockerFileContent.add("ADD " + kernelArtifact + " /opt/");
            dockerFileContent.add("RUN  \\\n\tmkdir -p /opt && \\\nunzip /opt/"
                    + CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT + ".zip -d /opt && \\\nrm /opt/"
                    + CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT + ".zip");
            dockerFileContent.add("# Carbon https port\nEXPOSE 9443");
            dockerFileContent.add("ENV JAVA_HOME=/usr");
            dockerFileContent.add("CMD [\"/opt/" + CarbonKernelHandlerConstants.CARBON_KERNEL_ARTIFACT
                    + "/bin/wso2server.sh\"]");
        } catch (Exception exception) {
            String message = "Failed to load the WSO2-Carbon kernel artifact zip.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }

        return dockerFileContent;
    }
}

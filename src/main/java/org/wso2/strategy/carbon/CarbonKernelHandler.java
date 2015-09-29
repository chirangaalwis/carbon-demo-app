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
package org.wso2.strategy.carbon;

import com.google.common.collect.ImmutableList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.wso2.strategy.carbon.interfaces.ICarbonKernelHandler;
import org.wso2.strategy.docker.configuration.DockerClientBuilder;
import org.wso2.strategy.docker.JavaDockerImageHandler;
import org.wso2.strategy.docker.ContainerStatusChecker;
import org.wso2.strategy.docker.interfaces.IDockerImageHandler;
import org.wso2.strategy.kubernetes.components.replication_controller.ReplicationControllerHandler;
import org.wso2.strategy.kubernetes.components.replication_controller.interfaces.IReplicationControllerHandler;
import org.wso2.strategy.kubernetes.components.service.ServiceHandler;
import org.wso2.strategy.kubernetes.components.service.interfaces.IServiceHandler;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;
import org.wso2.strategy.miscellaneous.helper.CarbonKernelHandlerHelper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarbonKernelHandler implements ICarbonKernelHandler {
    private final IDockerImageHandler imageBuilder;
    private final IReplicationControllerHandler replicationControllerHandler;
    private final IServiceHandler serviceHandler;

    private static final Log LOG = LogFactory.getLog(CarbonKernelHandler.class);

    public CarbonKernelHandler(String dockerEndpointURL, String kubernetesEndpointURL)
            throws CarbonKernelHandlerException {
        final ContainerStatusChecker statusChecker = new ContainerStatusChecker(
                DockerClientBuilder.buildDockerClient(dockerEndpointURL));
        if (statusChecker.checkContainerExistence(getContainerCmdImagePairs())) {
            imageBuilder = new JavaDockerImageHandler(DockerClientBuilder.buildDockerClient(dockerEndpointURL));
            replicationControllerHandler = new ReplicationControllerHandler(kubernetesEndpointURL);
            serviceHandler = new ServiceHandler(kubernetesEndpointURL);
        } else {
            String message = "Cannot start the application. Failed to start Docker Containers required for running Kubernetes.";
            LOG.error(message);
            throw new CarbonKernelHandlerException(message);
        }
    }

    public boolean deploy(String tenant, Path kernelPath, String buildVersion, int replicas)
            throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        try {
            boolean notDeployed = (replicationControllerHandler.getReplicationController(componentName) == null);
            if (notDeployed) {
                String dockerImageName = buildCarbonDockerImage(tenant, kernelPath, buildVersion);
                Thread.sleep(CarbonKernelHandlerConstants.IMAGE_BUILD_DELAY_IN_MILLISECONDS);
                replicationControllerHandler
                        .createReplicationController(componentName, componentName, dockerImageName, replicas);
                serviceHandler.createService(componentName, componentName);
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            String message = "Failed to deploy WSO2-Carbon kernel.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
    }

    public boolean rollUpdate(String tenant, Path kernelPath, String buildVersion) throws CarbonKernelHandlerException {
        try {
            String componentName = CarbonKernelHandlerHelper
                    .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
            boolean artifactsExist = (
                    imageBuilder.getExistingImages(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME, buildVersion)
                            .size() > 0);
            boolean deployed = (replicationControllerHandler.getReplicationController(componentName) != null);
            if (artifactsExist && deployed) {
                String dockerImageName = buildCarbonDockerImage(tenant, kernelPath, buildVersion);
                replicationControllerHandler.updateImage(componentName, dockerImageName);
                replicationControllerHandler
                        .deleteReplicaPods(componentName, tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
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

    public boolean rollBack(String tenant, String buildVersion, String olderVersion)
            throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        if ((imageBuilder.getExistingImages(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME, buildVersion).size()
                > 0)) {
            replicationControllerHandler.updateImage(componentName, olderVersion);
            replicationControllerHandler
                    .deleteReplicaPods(componentName, tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
            return true;
        } else {
            return false;
        }
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

    public int getNoOfReplicas(String tenant) throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        return replicationControllerHandler.getNoOfReplicas(componentName);
    }

    public String getServiceAccessIPs(String tenant) throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        String ipMessage;
        ipMessage = String.format("Cluster IP: %s/%s\nPublic IP: %s/%s\n\n", serviceHandler.getClusterIP(componentName),
                CarbonKernelHandlerConstants.INDEX_PAGE, serviceHandler.getNodePortIP(componentName),
                CarbonKernelHandlerConstants.INDEX_PAGE);
        return ipMessage;
    }

    public List<String> listExistingBuildArtifacts(String tenant, String buildVersion)
            throws CarbonKernelHandlerException {
        List<String> artifactList = new ArrayList<>();
        ImmutableList<String> repoTags;
        for (int count = 0;
             count < imageBuilder.getExistingImages(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME, buildVersion)
                     .size(); count++) {
            repoTags = imageBuilder.getExistingImages(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME, buildVersion)
                    .get(count).repoTags();
            for (String tag : repoTags) {
                if (tag.contains(tenant + "/" + CarbonKernelHandlerConstants.ARTIFACT_NAME + ":" + buildVersion)) {
                    artifactList.add(tag);
                }
            }
        }
        return artifactList;
    }

    public List<String> listLowerBuildArtifactVersions(String tenant, String buildVersion)
            throws CarbonKernelHandlerException {
        String componentName = CarbonKernelHandlerHelper
                .generateKubernetesComponentIdentifier(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME);
        List<String> minorArtifactList = new ArrayList<>();
        final int singleImageIndex = 0;
        if (replicationControllerHandler.getReplicationController(componentName) != null) {
            String upperLimitVersion = replicationControllerHandler.getReplicationController(componentName).getSpec()
                    .getTemplate().getSpec().getContainers().get(singleImageIndex).getImage();
            List<String> artifactList = listExistingBuildArtifacts(tenant, buildVersion);
            minorArtifactList = new ArrayList<>();
            for (String artifactImageBuild : artifactList) {
                if (CarbonKernelHandlerHelper.compareBuildVersions(upperLimitVersion, artifactImageBuild) > 0) {
                    minorArtifactList.add(artifactImageBuild);
                }
            }
        }
        return minorArtifactList;
    }

    private String buildCarbonDockerImage(String tenant, Path artifact, String version)
            throws CarbonKernelHandlerException {
        String kernelArtifact = artifact.getFileName().toString();
        List<String> dockerFileContent = setDockerFileContent(kernelArtifact);

        Path parentDirectory = artifact.getParent();
        File dockerFile;
        if (parentDirectory != null) {
            String parentDirectoryPath = parentDirectory.toString();
            dockerFile = new File(parentDirectoryPath + File.separator + "Dockerfile");
        } else {
            dockerFile = new File("Dockerfile");
        }
        CarbonKernelHandlerHelper.writeToFile(dockerFile.getAbsolutePath(), dockerFileContent);
        DateTime dateTime = new DateTime();
        String now =
                dateTime.getYear() + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + "-" + dateTime
                        .getMillisOfDay();
        version += ("-" + now);
        return imageBuilder.buildImage(tenant, CarbonKernelHandlerConstants.ARTIFACT_NAME, version,
                Paths.get(dockerFile.getAbsolutePath()));
    }

    private List<String> setDockerFileContent(String kernelArtifact) throws CarbonKernelHandlerException {
        List<String> dockerFileContent = new ArrayList<>();
        try {
            dockerFileContent.add("FROM java:openjdk-8");
            dockerFileContent.add("MAINTAINER dev@wso2.org");
            //        dockerFileContent.add("ENV WSO2_SOFT_VER=" + CarbonKernelHandlerConstants.CARBON_KERNEL_VERSION);
            dockerFileContent.add("ADD " + kernelArtifact + " /opt/");
            dockerFileContent
                    .add("RUN  \\\n\tmkdir -p /opt && \\\nunzip /opt/" + kernelArtifact + " -d /opt && \\\nrm /opt/"
                            + kernelArtifact + "");
            dockerFileContent.add("# Carbon https port\nEXPOSE 9443");
            dockerFileContent.add("ENV JAVA_HOME=/usr");
            dockerFileContent.add("ENTRYPOINT [\"/opt/" + kernelArtifact.substring(0, kernelArtifact.length() - 4)
                    + "/bin/wso2server.sh\"]");
        } catch (Exception exception) {
            String message = "Failed to create the WSO2-Carbon kernel artifact Docker Image.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
        return dockerFileContent;
    }

    private Map<String, String> getContainerCmdImagePairs() {
        Map<String, String> containerCmdImagePairs = new HashMap<>();

        containerCmdImagePairs.put("/hyperkube scheduler", "gcr.io/google_containers/hyperkube:v1.0.1");
        containerCmdImagePairs.put("/hyperkube apiserver", "gcr.io/google_containers/hyperkube:v1.0.1");
        containerCmdImagePairs.put("/hyperkube controlle", "gcr.io/google_containers/hyperkube:v1.0.1");
        containerCmdImagePairs.put("/pause", "gcr.io/google_containers/pause:0.8.0");
        containerCmdImagePairs.put("/usr/bin/cadvisor", "google/cadvisor:latest");
        containerCmdImagePairs.put("/hyperkube proxy --m", "gcr.io/google_containers/hyperkube:v1.0.1");
        containerCmdImagePairs.put("/hyperkube kubelet -", "gcr.io/google_containers/hyperkube:v1.0.1");
        containerCmdImagePairs.put("/usr/local/bin/etcd ", "gcr.io/google_containers/etcd:2.0.9");

        return containerCmdImagePairs;
    }
}

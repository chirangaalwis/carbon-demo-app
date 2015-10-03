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
package org.wso2.strategy.kubernetes.components.replication_controller;

import io.fabric8.kubernetes.api.KubernetesClient;
import io.fabric8.kubernetes.api.KubernetesFactory;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.strategy.kubernetes.components.replication_controller.interfaces.IReplicationControllerHandler;
import org.wso2.strategy.kubernetes.configuration.KubernetesClientBuilder;
import org.wso2.strategy.kubernetes.constants.KubernetesConstantsExtended;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Java class which implements the IReplicationControllerHandler interface
 */
public class ReplicationControllerHandler implements IReplicationControllerHandler {
    private final KubernetesClient client;
    private static final Logger LOG = LogManager.getLogger(ReplicationControllerHandler.class);

    public ReplicationControllerHandler(String kubernetesURI) throws CarbonKernelHandlerException {
        client = KubernetesClientBuilder.buildDockerClient(kubernetesURI);
    }

    public void createReplicationController(String controllerName, String podLabel, String dockerImageName,
            int numberOfReplicas) throws CarbonKernelHandlerException {
        try {
            if ((controllerName != null) && (podLabel != null) && (dockerImageName != null)) {
                ReplicationController controller = client.getReplicationController(controllerName);
                if (controller == null) {
                    if (LOG.isDebugEnabled()) {
                        String message = String.format("Creating Kubernetes replication controller"
                                        + " [controller-name] %s [pod-label] %s " + "[pod-Docker-image-name] %s",
                                controllerName, podLabel, dockerImageName);
                        LOG.debug(message);
                    }
                    ReplicationController replicationController = new ReplicationController();

                    replicationController.setApiVersion(ReplicationController.ApiVersion.V_1);
                    replicationController.setKind(KubernetesConstantsExtended.REPLICATION_CONTROLLER_COMPONENT_KIND);

                    ObjectMeta metadata = new ObjectMeta();
                    metadata.setName(controllerName);
                    replicationController.setMetadata(metadata);

                    ReplicationControllerSpec replicationControllerSpec = new ReplicationControllerSpec();
                    replicationControllerSpec.setReplicas(numberOfReplicas);

                    PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
                    PodSpec podSpec = new PodSpec();

                    List<Container> podContainers = new ArrayList<>();
                    Container container = new Container();
                    container.setImage(dockerImageName);
                    container.setName(podLabel);
                    podContainers.add(container);
                    podSpec.setContainers(podContainers);

                    podTemplateSpec.setSpec(podSpec);

                    Map<String, String> selectors = new HashMap<>();
                    selectors.put(KubernetesConstantsExtended.LABEL_NAME, podLabel);

                    ObjectMeta tempMeta = new ObjectMeta();
                    tempMeta.setLabels(selectors);
                    podTemplateSpec.setMetadata(tempMeta);

                    replicationControllerSpec.setTemplate(podTemplateSpec);
                    replicationControllerSpec.setSelector(selectors);
                    replicationController.setSpec(replicationControllerSpec);

                    /*
                        creates a replication controller using the specified
                        replication controller entity
                     */
                    client.createReplicationController(replicationController, "default");
                    if (LOG.isDebugEnabled()) {
                        String message = String.format("Created Kubernetes replication controller"
                                        + " [controller-name] %s [pod-label] %s " + "[pod-Docker-image-name] %s",
                                controllerName, podLabel, dockerImageName);
                        LOG.debug(message);
                    }
                }
            } else {
                String message = "Could not create the replication controller. Replication controller id, "
                        + "pod label and Docker Image name cannot be null.";
                LOG.error(message);
                throw new CarbonKernelHandlerException(message);
            }
        } catch (Exception exception) {
            String message = String
                    .format("Could not create the replication controller[rc-identifier]: " + "%s", controllerName);
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
    }

    public ReplicationController getReplicationController(String controllerName) {
        if (controllerName != null) {
            return client.getReplicationController(controllerName);
        } else {
            return null;
        }
    }

    public int getNoOfReplicas(String controllerName) throws CarbonKernelHandlerException {
        if (controllerName != null) {
            ReplicationController replicationController = client.getReplicationController(controllerName);
            if (replicationController != null) {
                return replicationController.getSpec().getReplicas();
            } else {
                return 0;
            }
        } else {
            String message = "Replication controller id cannot be null.";
            throw new CarbonKernelHandlerException(message);
        }
    }

    public void updateNoOfReplicas(String controllerName, int newReplicas) throws CarbonKernelHandlerException {
        if (controllerName != null) {
            ReplicationController replicationController = client.getReplicationController(controllerName);
            try {
                if (replicationController != null) {
                    ReplicationControllerSpec spec = replicationController.getSpec();
                    if (spec.getReplicas() != newReplicas) {
                        spec.setReplicas(newReplicas);
                        client.updateReplicationController(controllerName, replicationController);
                    }
                }
            } catch (Exception exception) {
                String message = String
                        .format("Could not update the replication controller[rc-identifier]: " + "%s", controllerName);
                LOG.error(message, exception);
                throw new CarbonKernelHandlerException(message, exception);
            }
        } else {
            String message = "Replication controller id cannot be null.";
            throw new CarbonKernelHandlerException(message);
        }
    }

    public void updateImage(String controllerName, String dockerImageName) throws CarbonKernelHandlerException {
        if (controllerName != null) {
            ReplicationController replicationController = client.getReplicationController(controllerName);
            final int imageIndex = 0;
            try {
                if (replicationController != null) {
                    if (dockerImageName != null) {
                        List<Container> podContainers = replicationController.getSpec().getTemplate().getSpec()
                                .getContainers();
                        if ((podContainers != null) && (podContainers.size() > 0)) {
                            podContainers.get(imageIndex).setImage(dockerImageName);
                        }
                        client.updateReplicationController(controllerName, replicationController);
                    }
                }
            } catch (Exception exception) {
                String message = String
                        .format("Could not update the replication controller[rc-identifier]: " + "%s", controllerName);
                LOG.error(message, exception);
                throw new CarbonKernelHandlerException(message, exception);
            }
        } else {
            String message = "Replication controller id cannot be null.";
            throw new CarbonKernelHandlerException(message);
        }
    }

    public void deleteReplicaPods(String controllerName, String creator, String podArtifactName)
            throws CarbonKernelHandlerException {
        try {
            ReplicationController controller = getReplicationController(controllerName);
            if (controller != null) {
                if ((creator != null) && (podArtifactName != null)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Deleting Kubernetes replica pods.");
                    }
                    List<Pod> replicaPods = KubernetesHelper
                            .getPodsForReplicationController(controller, client.getPods().getItems());
                    for (Pod pod : replicaPods) {
                        client.deletePod(pod);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Deleted Kubernetes replica pods.");
                    }
                } else {
                    String message = "Could not delete the replica pods. Arguments for creator or/and cannot be null.";
                    LOG.error(message);
                    throw new CarbonKernelHandlerException(message);
                }
            } else {
                String message = "Could not delete the replica pods. No such replication controller.";
                LOG.error(message);
                throw new CarbonKernelHandlerException(message);
            }
        } catch (Exception exception) {
            String message = "Could not delete the replica pods.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
    }

    public ReplicationController deleteReplicationController(String controllerName)
            throws CarbonKernelHandlerException {
        if (controllerName != null) {
            ReplicationController replicationController = client.getReplicationController(controllerName);
            try {
                if (replicationController != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Deleting Kubernetes replication controller" + " [rc-name] %s",
                                controllerName));
                    }
                    client.deleteReplicationController(controllerName);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Deleted Kubernetes replication controller" + " [rc-name] %s",
                                controllerName));
                    }
                }
            } catch (Exception exception) {
                String message = String
                        .format("Could not delete the replication controller[rc-identifier]: " + "%s", controllerName);
                LOG.error(message, exception);
                throw new CarbonKernelHandlerException(message, exception);
            }
            return replicationController;
        } else {
            String message = "Replication controller id cannot be null.";
            throw new CarbonKernelHandlerException(message);
        }
    }
}

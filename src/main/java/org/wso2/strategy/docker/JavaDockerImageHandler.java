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
import com.spotify.docker.client.messages.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.strategy.docker.interfaces.IDockerImageHandler;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;
import org.wso2.strategy.miscellaneous.helper.CarbonKernelHandlerHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * a Java class which implements IDockerImageHandler Java interface
 */
public class JavaDockerImageHandler implements IDockerImageHandler {
    private final DockerClient dockerClient;
    private static final Logger LOG = LogManager.getLogger(JavaDockerImageHandler.class);

    public JavaDockerImageHandler(DockerClient client) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating a new DockerClient.");
        }
        dockerClient = client;
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Created a new DockerClient[docker-client]: %s.", dockerClient));
        }
    }

    public String buildImage(String creator, String dockerArtifactName, String version, Path dockerFilePath)
            throws CarbonKernelHandlerException {
        String dockerImageName = CarbonKernelHandlerHelper
                .generateImageIdentifier(creator, dockerArtifactName, version);
        try {
            if (dockerImageName != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Creating a new Docker image %s.", dockerImageName));
                }
                String freshImageId = dockerClient.build(dockerFilePath.getParent(), dockerImageName);
                if (freshImageId != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                String.format("Created a new Docker image %s [id]:%s", dockerImageName, freshImageId));
                    }
                } else {
                    String message = String
                            .format("Could not create the Docker image [docker-image]: %s.", dockerImageName);
                    LOG.error(message);
                    throw new CarbonKernelHandlerException(message);
                }
            }
        } catch (Exception exception) {
            String message = String.format("Could not create the Docker image [docker-image]: %s.", dockerImageName);
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
        return dockerImageName;
    }

    public List<Image> getExistingImages(String creator, String dockerArtifactName, String version)
            throws CarbonKernelHandlerException {
        List<Image> matchingImageList = new ArrayList<>();
        String imageIdentifier = CarbonKernelHandlerHelper
                .generateImageIdentifier(creator, dockerArtifactName, version);
        try {
            if (imageIdentifier != null) {
                List<Image> tempImages = dockerClient.listImages();
                for (Image image : tempImages) {
                    for (String tag : image.repoTags()) {
                        if (tag.contains(imageIdentifier)) {
                            if (!matchingImageList.contains(image)) {
                                matchingImageList.add(image);
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            String message = "Could not load the repo images.";
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
        return matchingImageList;
    }

    public String removeImage(String creator, String dockerArtifactName, String version)
            throws CarbonKernelHandlerException {
        String dockerImageName = CarbonKernelHandlerHelper
                .generateImageIdentifier(creator, dockerArtifactName, version);
        try {
            List<Image> existingImages = getExistingImages(creator, dockerArtifactName, version);
            if ((dockerImageName != null) && (existingImages.size() > 0)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Removing the Docker image [docker-image]: %s.", dockerImageName));
                }
                dockerClient.removeImage(dockerImageName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Removed the Docker image [docker-image]: %s.", dockerImageName));
                }
            }
        } catch (Exception exception) {
            String message = String.format("Could not remove the docker image [docker-image]: %s.", dockerImageName);
            LOG.error(message, exception);
            throw new CarbonKernelHandlerException(message, exception);
        }
        return dockerImageName;
    }
}

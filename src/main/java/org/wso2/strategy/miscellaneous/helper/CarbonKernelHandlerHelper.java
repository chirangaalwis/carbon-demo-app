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
package org.wso2.strategy.miscellaneous.helper;

import org.wso2.strategy.miscellaneous.io.FileOutputThread;

import java.io.File;
import java.util.List;

/**
 * a Java class which consists of various application specific utility methods
 * which are reusable by more than one class
 */
public class CarbonKernelHandlerHelper {
    /**
     * returns an application specific Kubernetes component identifier
     *
     * @param artifactCreator name of the web artifact creator
     * @param artifactName    name of the web artifact
     * @return an application specific Kubernetes component identifier
     */
    public static String generateKubernetesComponentIdentifier(String artifactCreator, String artifactName) {
        if ((artifactCreator != null) && (artifactName != null)) {
            return artifactName + "-" + artifactCreator;
        } else {
            return null;
        }
    }

    /**
     * utility method which generates a Docker image name
     *
     * @param creator              creator of the Docker image
     * @param deployedArtifactName name of the artifact deployed
     * @param version              deployed version of the image
     * @return Docker image identifier based on the data provided, if either one or both the
     * creator or imageName is/are null, null is returned
     */
    public static String generateImageIdentifier(String creator, String deployedArtifactName, String version) {
        String imageIdentifier;
        if ((creator != null) && (deployedArtifactName != null)) {
            if ((version == null) || (version.equals(""))) {
                imageIdentifier = creator + "/" + deployedArtifactName + ":latest";
            } else {
                imageIdentifier = creator + "/" + deployedArtifactName + ":" + version;
            }
        } else {
            imageIdentifier = null;
        }
        return imageIdentifier;
    }

    /**
     * utility method which writes content to an external file
     *
     * @param filePath path to the file to which content are to be written
     * @param data     content to be written to the file
     */
    public static void writeToFile(File filePath, List<String> data) {
        FileOutputThread outputThread = new FileOutputThread(filePath.getAbsolutePath(), data);
        outputThread.run();
    }
}

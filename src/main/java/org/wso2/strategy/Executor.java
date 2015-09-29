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
package org.wso2.strategy;

import org.wso2.strategy.carbon.CarbonKernelHandler;
import org.wso2.strategy.carbon.interfaces.ICarbonKernelHandler;
import org.wso2.strategy.miscellaneous.exception.CarbonKernelHandlerException;
import org.wso2.strategy.miscellaneous.helper.CarbonKernelHandlerHelper;
import org.wso2.strategy.miscellaneous.io.FileInputKeyValueDataThread;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Executor {
    //    private static final int KUBERNETES_CONTAINER_CREATION_DELAY_IN_MILLISECONDS = 10000;
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String CONFIGURATION_FILE = "client_configuration.txt";

    public static void main(String[] args) {
        try {
            //            Thread.sleep(KUBERNETES_CONTAINER_CREATION_DELAY_IN_MILLISECONDS);
            Map<String, String> configurationData = getClientConfigurationData();
            final ICarbonKernelHandler kernelHandlerHandler = new CarbonKernelHandler(
                    configurationData.get("docker-url"), configurationData.get("kubernetes-url"));
            final String welcomeMessage = "***WELCOME TO WSO2 CARBON-KERNEL HANDLER APP***\n\n";
            final String mainMenuContent = "1 - Deploy\n2 - Rolling update\n3 - Rollback\n4 - Scale\n"
                    + "5 - Un-deploy\n6 - Exit\nEnter your choice: ";
            showMenu(welcomeMessage);
            while (true) {
                int userChoice;
                String tempUserChoice;
                do {
                    showMenu(mainMenuContent);
                    tempUserChoice = SCANNER.next();
                    SCANNER.nextLine();
                    userChoice = getUserChoice(tempUserChoice);
                } while ((userChoice < 1) || (userChoice > 6));
                process(userChoice, kernelHandlerHandler);
            }
        } catch (Exception exception) {
            System.exit(1);
        }
    }

    private static void process(int choice, ICarbonKernelHandler kernelHandler) {
        Map<String, Object> inputs;
        String tenant;
        Path artifactPath;
        String buildVersion;
        int replicas;
        try {
            switch (choice) {
            case 1:
                inputs = gatherDeploymentData();
                tenant = (String) (inputs.get("tenant"));
                artifactPath = (Path) (inputs.get("artifact"));
                buildVersion = (String) (inputs.get("version"));
                replicas = (Integer) (inputs.get("replicas"));
                boolean deployed = kernelHandler.deploy(tenant, artifactPath, buildVersion, replicas);
                if (deployed) {
                    showMenu(kernelHandler.getServiceAccessIPs(tenant));
                } else {
                    showMenu("This tenant has deployed a kernel artifact, already. Please use a rolling update "
                            + "to make an updated deployment.\n");
                }
                break;
            case 2:
                inputs = gatherUpdateData();
                tenant = (String) (inputs.get("tenant"));
                artifactPath = (Path) (inputs.get("artifact"));
                buildVersion = (String) (inputs.get("version"));
                deployed = kernelHandler.rollUpdate(tenant, artifactPath, buildVersion);
                if (deployed) {
                    showMenu(kernelHandler.getServiceAccessIPs(tenant));
                } else {
                    showMenu("Cannot make an update. This tenant may not have deployed a kernel artifact, "
                            + "currently or a previous build version of the above artifact may not "
                            + "have been deployed.\n");
                }
                break;
            case 3:
                inputs = gatherIdentifierData();
                tenant = (String) inputs.get("tenant");
                buildVersion = (String) inputs.get("version");
                List<String> displayLowerList = kernelHandler.
                        listLowerBuildArtifactVersions(tenant, buildVersion);
                if ((displayLowerList != null) && (displayLowerList.size() > 0)) {
                    displayList(displayLowerList);
                    int userChoice;
                    String tempUserChoice;
                    do {
                        showMenu("Enter your choice: ");
                        tempUserChoice = SCANNER.next();
                        SCANNER.nextLine();
                        userChoice = getUserChoice(tempUserChoice);
                    } while ((userChoice < 1) || (userChoice > displayLowerList.size()));
                    kernelHandler.rollBack(tenant, buildVersion, getListChoice(displayLowerList, userChoice));
                } else {
                    showMenu("No lower kernel build versions.\n");
                }
                break;
            case 4:
                inputs = gatherScalingData(kernelHandler);
                tenant = (String) (inputs.get("tenant"));
                replicas = (Integer) (inputs.get("replicas"));
                if (replicas > 0) {
                    kernelHandler.scale(tenant, replicas);
                }
                break;
            case 5:
                tenant = gatherTenantData();
                boolean removed = kernelHandler.remove(tenant);
                if (!removed) {
                    showMenu("No such kernel is currently running.\n");
                }
                break;
            case 6:
                System.exit(0);
                break;
            }
        } catch (CarbonKernelHandlerException exception) {
            showMenu("The program has encountered an error.\n");
        }
    }

    private static void showMenu(String menuContent) {
        System.out.print(menuContent);
    }

    private static String gatherTenantData() {
        showMenu("Tenant name: ");
        String tenant = SCANNER.next();
        SCANNER.nextLine();
        return tenant;
    }

    private static Path gatherArtifactData() {
        Path artifactPath;
        String path;
        boolean exists = false;
        do {
            showMenu("Kernel artifact path: ");
            path = SCANNER.nextLine();
            artifactPath = Paths.get(path);
            if (!Files.isDirectory(artifactPath)) {
                exists = Files.exists(artifactPath);
            } else {
                showMenu("The path should not refer to a directory.\n");
                continue;
            }
            if (!exists) {
                showMenu("This file path does not exist.\n");
            }
        } while (!exists);

        return artifactPath;
    }

    private static Map<String, Object> gatherIdentifierData() {
        Map<String, Object> inputs = new HashMap<>();
        String tenant = gatherTenantData();

        showMenu("Build version: ");
        String version = SCANNER.nextLine();

        // Add to list of inputs
        inputs.put("tenant", tenant);
        inputs.put("version", version);
        return inputs;
    }

    private static Map<String, Object> gatherUpdateData() {
        Map<String, Object> inputs = gatherIdentifierData();
        Path artifactPath = gatherArtifactData();

        // Add to list of inputs
        inputs.put("artifact", artifactPath);
        return inputs;
    }

    private static Map<String, Object> gatherDeploymentData() {
        Map<String, Object> inputs = gatherIdentifierData();
        Path artifactPath = gatherArtifactData();

        int replicas;
        String tempUserChoice;
        do {
            showMenu("Number of deployment replicas: ");
            tempUserChoice = SCANNER.next();
            SCANNER.nextLine();
            replicas = getUserChoice(tempUserChoice);
        } while ((replicas < 1));

        // Add to list of inputs
        inputs.put("artifact", artifactPath);
        inputs.put("replicas", replicas);
        return inputs;
    }

    private static Map<String, Object> gatherScalingData(ICarbonKernelHandler kernelHandler)
            throws CarbonKernelHandlerException {
        Map<String, Object> inputs = new HashMap<>();
        String tenant = gatherTenantData();
        inputs.put("tenant", tenant);

        final int podLess = 0;
        int noOfReplicas = kernelHandler.getNoOfReplicas(tenant);
        if (noOfReplicas > 0) {
            showMenu("Current no. of kernel artifact replicas running: " + noOfReplicas + "\n");
            showMenu("Enter new no. of replicas: ");
            String tempUserChoice = SCANNER.next();
            SCANNER.nextLine();
            int replicas = getUserChoice(tempUserChoice);
            // Add to list of inputs
            inputs.put("replicas", replicas);
        } else {
            showMenu("This kernel artifact has not been deployed yet.\n");
            inputs.put("replicas", podLess);
        }
        return inputs;
    }

    private static void displayList(List<String> data) {
        if (data != null) {
            for (int count = 0; count < data.size(); count++) {
                System.out.print((count + 1) + ". " + data.get(count) + "\n");
            }
        }
    }

    private static String getListChoice(List<String> choices, int choice) {
        if ((choices != null) && (choice > 0 && choice <= choices.size())) {
            return choices.get(choice - 1);
        } else {
            return null;
        }
    }

    private static int getUserChoice(String input) {
        boolean allDigits = true;
        char tempCharacter;
        for (int characterCount = 0; characterCount < input.length(); characterCount++) {
            tempCharacter = input.charAt(characterCount);
            if (!((tempCharacter >= '0') && (tempCharacter <= '9'))) {
                allDigits = false;
                break;
            }
        }
        if (allDigits) {
            return Integer.parseInt(input);
        } else {
            return -1;
        }
    }

    private static void setClientConfigurationData() {
        List<String> configurationData = new ArrayList<>();
        // sets the default client configuration data
        configurationData.add("docker-url=unix:///var/run/docker.sock");
        configurationData.add("kubernetes-url=http://127.0.0.1:8080");
        CarbonKernelHandlerHelper.writeToFile(CONFIGURATION_FILE, configurationData);
    }

    private static Map<String, String> getClientConfigurationData() {
        Path configFilePath = Paths.get(CONFIGURATION_FILE);
        if (!Files.exists(configFilePath)) {
            setClientConfigurationData();
        }
        Map<String, String> clientConfigData;
        FileInputKeyValueDataThread inputThread = new FileInputKeyValueDataThread(CONFIGURATION_FILE);
        inputThread.run();
        clientConfigData = inputThread.getFileContent();
        return clientConfigData;
    }
}

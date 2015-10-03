# wso2-carbon5-demo-app
A Java application which deploys WSO2 Carbon-5 kernel Docker Containers orchestrated by Google Kubernetes.

Uses the https://github.com/spotify/docker-client repo for Docker image handling and https://github.com/fabric8io/fabric8/tree/master/components/kubernetes-api repo for Google Kubernetes orchestration.

This Java IntelliJ IDEA project has been packaged using the maven-assembly-plugin.

Before running the application make sure you have Docker and Google Kubernetes set up in your machine.

Follow the steps below to run the application:

1. Pull the wso2-carbon5-demo-app repo and perform a Maven build (jdk version 1.7)
2. Obtain the wso2-carbon5-demo-app/target/carbon5-poc-1.0-SNAPSHOT.zip file
3. Unzip the carbon5-poc-1.0-SNAPSHOT.zip.
4. Run /bin/carbon-kernel-handler-extension.sh.

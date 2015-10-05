# wso2-carbon5-demo-app
A Java application which deploys WSO2 Carbon-5 kernel Docker Containers orchestrated by Google Kubernetes.

Uses the https://github.com/spotify/docker-client repo for Docker image handling and https://github.com/fabric8io/fabric8/tree/master/components/kubernetes-api repo for Google Kubernetes orchestration.

This Java IntelliJ IDEA project has been packaged using the maven-assembly-plugin.

Note: Currently, this application has only been tested in Ubuntu Trusty 14.04 (LTS) OS version.



Before running the application make sure you have Docker, Google Kubernetes set up in your machine.

The Docker API versions > 1.15 would be sufficient.

Kubernetes release 1.0.1 has been used for testing.

Installing the latest Docker using docker.io script

1. curl command: sudo curl -sSL https://get.docker.io/ | sh

2. wget command: sudo wget -qO- https://get.docker.io/ | sh

For further information on configuring Docker in different platforms, you can check https://docs.docker.com/
link.



To setup Google Kubernetes to run locally via Docker, the following commands can be run in a terminal:

1. Run etcd -

docker run --net=host -d gcr.io/google_containers/etcd:2.0.9 /usr/local/bin/etcd --addr=127.0.0.1:4001 --bind-addr=0.0.0.0:4001 --data-dir=/var/etcd/data

2. Run the master -

docker run --net=host -d -v /var/run/docker.sock:/var/run/docker.sock  gcr.io/google_containers/hyperkube:v1.0.1 /hyperkube kubelet --api_servers=http://localhost:8080 --v=2 --address=0.0.0.0 --enable_server --hostname_override=127.0.0.1 --config=/etc/kubernetes/manifests

3. Run the service proxy -

docker run -d --net=host --privileged gcr.io/google_containers/hyperkube:v1.0.1 /hyperkube proxy --master=http://127.0.0.1:8080 --v=2

4. Obtain the Kubectl (Kubernetes Commandline client)

wget https://storage.googleapis.com/kubernetes-release/release/v1.0.1/bin/linux/amd64/kubectl

For further information on running Kubernetes locally via Docker, refer http://kubernetes.io/v1.0/docs/getting-started-guides/docker.html
and for further information about Kubernetes concepts, refer http://kubernetes.io/v1.0/ documentation.



Follow the steps below to run the application:

1. Pull the wso2-carbon5-demo-app repo and perform a Maven build (jdk version 1.7)
2. Obtain the wso2-carbon5-demo-app/target/carbon5-poc-1.0-SNAPSHOT.zip file
3. Unzip the carbon5-poc-1.0-SNAPSHOT.zip.
4. Run /bin/carbon-kernel-handler-extension.sh.

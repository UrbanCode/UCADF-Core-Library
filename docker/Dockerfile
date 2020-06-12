FROM ubuntu:latest

ENV DEBIAN_FRONTEND=noninteractive
RUN apt -yqq update && apt -yqq install maven openjdk-8-jdk vim net-tools iputils-ping curl git unzip
ADD .inputrc /root/
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64

# Define the version of the plugin when it is installed into an UrbanCode Deploy instance.
ENV UCADF_PLUGIN_INSTALL_VERSION=50

# Define the version of the plugin as it will be deployed to Maven.
ENV UCADF_CORE_VERSION=1.0.0
ENV UCADF_PLUGIN_VERSION=1.0.50
ENV UCADF_STORE=/ucadf/UCADF-Store
ENV UCD_SERVER_URL=https://172.17.0.1:8443
ENV ABC_PACKAGEDIR=$UCADF_STORE/Development/ABC-UCADF/UCADF-Package
ENV ABC_UCADF_VERSION=1.0.0

#Clone project
RUN mkdir /ucadf
WORKDIR /ucadf
RUN git clone https://github.com/UrbanCode/UCADF-Core-Library.git \
  && git clone https://github.com/UrbanCode/UCADF-Core-Plugin.git \
  && git clone https://github.com/UrbanCode/UCADF-Store.git 

# Build Core Library
WORKDIR UCADF-Core-Library/
RUN # Define the version number. If building with Jenkins the last digit could be the Jenkins ${BUILD_NUMBER}. \
# Set the version number in the pom.xml file. \
mvn versions:set -DnewVersion="$UCADF_CORE_VERSION" \
mvn -U clean deploy

# Build Plugin
RUN mvn -U clean install
WORKDIR /ucadf/UCADF-Core-Plugin/

# Set the version number in the pom.xml file.
RUN mvn versions:set -DnewVersion="$UCADF_PLUGIN_VERSION"
RUN mkdir /ucadf/UCADF-Core-Plugin/license
RUN mvn -U clean package -DUCADF_PLUGIN_VERSION="$UCADF_PLUGIN_VERSION" -DUCADF_PLUGIN_INSTALL_VERSION="$UCADF_PLUGIN_INSTALL_VERSION"
RUN mkdir /opt/ucadfclient \
  && cp releases/UCADF-Core-Plugin-v1.0.50.zip /opt/ucadfclient

#Copy config files and plugin binary
#Can be overridden using env vars UCD_AUTH_TOKEN and UCD_SERVER_URL
WORKDIR /ucadf/UCADF-Store
RUN git checkout msamanoibm-patch-1
WORKDIR /ucadf/UCADF-Store/Instances/example/
ADD instance.properties /ucadf/src/UCADF-Store/Instances/example/instance.properties
ADD instance.secure.properties /ucadf/src/UCADF-Store/Instances/example/
ADD ABC-UCADF-Package.secure.properties /ucadf/src/UCADF-Store/Instances/example/
ADD udclient.jar /opt/ucadf/

#Move dependencies
RUN cp /ucadf/UCADF-Core-Library/target/classes/ucadfclient /opt/ucadfclient/ && chmod 755 /opt/ucadfclient/ucadfclient
ENV PATH=/opt/ucadfclient:$PATH
RUN cp /ucadf/UCADF-Core-Plugin/target/UCADF-Core-Plugin-1.0.50.jar /opt/ucadfclient/ \
  && cp /ucadf/UCADF-Core-Library/target/classes/log4j2.xml /opt/ucadfclient \
  && cp /ucadf/UCADF-Core-Library/target/UCADF-Core-Library.jar /opt/ucadfclient

WORKDIR ${UCADF_STORE}/Development/ABC-UCADF
ADD entrypoint.sh /
CMD [ "/entrypoint.sh" ]
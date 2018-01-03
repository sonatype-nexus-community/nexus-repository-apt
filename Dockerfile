ARG NEXUS_VERSION=3.7.1

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.7.1
ARG NEXUS_BUILD=02

COPY . /nexus-repository-apt/
RUN cd /nexus-repository-apt/; sed -i "s/3.7.1-02/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.7.1
ARG NEXUS_BUILD=02
USER root
RUN mkdir /opt/sonatype/nexus/system/net/staticsnow/ /opt/sonatype/nexus/system/net/staticsnow/nexus-repository-apt/ /opt/sonatype/nexus/system/net/staticsnow/nexus-repository-apt/1.0.3/; \
    sed -i 's@nexus-repository-npm</feature>@nexus-repository-npm</feature>\n        <feature prerequisite="false" dependency="false">nexus-repository-apt</feature>@g' /opt/sonatype/nexus/system/com/sonatype/nexus/assemblies/nexus-oss-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-oss-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml; \
    sed -i 's@<feature name="nexus-repository-npm"@<feature name="nexus-repository-apt" description="net.staticsnow:nexus-repository-apt" version="1.0.3">\n        <details>net.staticsnow:nexus-repository-apt</details>\n        <bundle>mvn:net.staticsnow/nexus-repository-apt/1.0.3</bundle>\n    </feature>\n    <feature name="nexus-repository-npm"@g' /opt/sonatype/nexus/system/com/sonatype/nexus/assemblies/nexus-oss-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-oss-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml;
COPY --from=build /nexus-repository-apt/target/nexus-repository-apt-1.0.3.jar /opt/sonatype/nexus/system/net/staticsnow/nexus-repository-apt/1.0.3/
USER nexus

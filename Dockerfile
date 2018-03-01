ARG NEXUS_VERSION=3.9.0

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.9.0
ARG NEXUS_BUILD=01

COPY . /nexus-repository-apt/
RUN cd /nexus-repository-apt/; sed -i "s/3.9.0-01/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.9.0
ARG NEXUS_BUILD=01
# Will not seem to work in sed without some magick
ARG APT_VERSION=1.0.4
ARG APT_TARGET=/opt/sonatype/nexus/system/net/staticsnow/nexus-repository-apt/${APT_VERSION}/
USER root
RUN mkdir -p ${APT_TARGET}; \
    sed -i 's@nexus-repository-npm</feature>@nexus-repository-npm</feature>\n        <feature prerequisite="false" dependency="false">nexus-repository-apt</feature>@g' /opt/sonatype/nexus/system/com/sonatype/nexus/assemblies/nexus-oss-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-oss-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml; \
    sed -i 's@<feature name="nexus-repository-npm"@<feature name="nexus-repository-apt" description="net.staticsnow:nexus-repository-apt" version="1.0.4">\n        <details>net.staticsnow:nexus-repository-apt</details>\n        <bundle>mvn:net.staticsnow/nexus-repository-apt/1.0.4</bundle>\n    </feature>\n    <feature name="nexus-repository-npm"@g' /opt/sonatype/nexus/system/com/sonatype/nexus/assemblies/nexus-oss-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-oss-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml;
COPY --from=build /nexus-repository-apt/target/nexus-repository-apt-${APT_VERSION}.jar ${APT_TARGET}
USER nexus

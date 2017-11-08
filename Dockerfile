FROM maven:3-jdk-8-alpine

COPY . /nexus-repository-apt/
RUN cd /nexus-repository-apt/; sed -i 's/3.5.0-02/3.6.0-02/g' pom.xml; \
    mvn;

FROM sonatype/nexus3:3.6.0
USER root
RUN mkdir /opt/sonatype/nexus/system/net/staticsnow/ /opt/sonatype/nexus/system/net/staticsnow/nexus-repository-apt/ /opt/sonatype/nexus/system/net/staticsnow/nexus-repository-apt/1.0.2/; \
    sed -i 's@nexus-repository-npm</feature>@nexus-repository-npm</feature>\n        <feature prerequisite="false" dependency="false">nexus-repository-apt</feature>@g' /opt/sonatype/nexus/system/com/sonatype/nexus/assemblies/nexus-oss-feature/3.6.0-02/nexus-oss-feature-3.6.0-02-features.xml; \
    sed -i 's@<feature name="nexus-repository-npm"@<feature name="nexus-repository-apt" description="net.staticsnow:nexus-repository-apt" version="1.0.2">\n        <details>net.staticsnow:nexus-repository-apt</details>\n        <bundle>mvn:net.staticsnow/nexus-repository-apt/1.0.2</bundle>\n    </feature>\n    <feature name="nexus-repository-npm"@g' /opt/sonatype/nexus/system/com/sonatype/nexus/assemblies/nexus-oss-feature/3.6.0-02/nexus-oss-feature-3.6.0-02-features.xml;
#COPY target/nexus-repository-apt-1.0.2.jar /opt/sonatype/nexus/system/net/staticsnow/nexus-repository-apt/1.0.2/
COPY --from=0 /nexus-repository-apt/target/nexus-repository-apt-1.0.2.jar /opt/sonatype/nexus/system/net/staticsnow/nexus-repository-apt/1.0.2/
USER nexus

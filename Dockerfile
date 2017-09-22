FROM sonatype/nexus3

COPY target/nexus-repository-apt-*.jar ${NEXUS_HOME}/system/net/staticsnow/nexus-repository-apt/1.0.2/
COPY docker/nexus-oss-feature-3.5.2-01-features.xml ${NEXUS_HOME}/system/com/sonatype/nexus/assemblies/nexus-oss-feature/3.5.2-01/nexus-oss-feature-3.5.2-01-features.xml
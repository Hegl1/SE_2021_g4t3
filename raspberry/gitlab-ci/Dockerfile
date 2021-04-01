FROM debian:buster-slim

ENV TZ=UTC
ENV DEBIAN_FRONTEND=noninteractive

ARG MAVEN_VERSION=3.6.1
ARG JDK_VERSION=11
ARG PMD_VERSION=6.31.0

WORKDIR /opt

# Install basic dependencies and utility packages
RUN \
  apt-get update && apt-get -y --no-install-recommends install \
    apt-utils \
    ca-certificates \
    apt-transport-https \
    git \
    zip \
    unzip \
    curl \
    make \
    wget

# Install Python 3 (not essential, but quite useful)
RUN \
  apt-get update && apt-get -y --no-install-recommends install \
    python3-software-properties \
    python3-pip
    
# Install Python package(s)
RUN pip3 install anybadge

# Install OpenJDK (See: https://adoptopenjdk.net/)
RUN \
  wget -O jdk-${JDK_VERSION}.tar.gz https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.10_9.tar.gz \
  && mkdir jdk-${JDK_VERSION} && tar zxvf jdk-${JDK_VERSION}.tar.gz -C jdk-${JDK_VERSION} --strip-components 1 \
  && mv jdk-${JDK_VERSION}/ /usr/local/ \
  && rm jdk-${JDK_VERSION}.tar.gz

# Install maven
RUN \
  wget --no-verbose -O /tmp/apache-maven-${MAVEN_VERSION}.tar.gz http://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && tar xzf /tmp/apache-maven-${MAVEN_VERSION}.tar.gz -C /opt/ \
  && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven \
  && ln -s /opt/maven/bin/mvn /usr/local/bin \
  && rm -f /tmp/apache-maven-${MAVEN_VERSION}.tar.gz

# Set environment variables
ENV MAVEN_HOME /opt/maven
ENV JAVA_HOME=/usr/local/jdk-${JDK_VERSION}
ENV PATH=$PATH:$JAVA_HOME/bin

# Install Code Quality tool(s)
RUN \
  wget https://github.com/pmd/pmd/releases/download/pmd_releases%2F${PMD_VERSION}/pmd-bin-${PMD_VERSION}.zip && \
  unzip pmd-bin-${PMD_VERSION}.zip && rm pmd-bin-${PMD_VERSION}.zip
  
RUN apt-get clean




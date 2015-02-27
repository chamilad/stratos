FROM chamilad/base-image:4.1.0-beta
MAINTAINER dev@stratos.apache.org

ENV DEBIAN_FRONTEND noninteractive
ENV WSO2_IS_VERSION 5.0.0

# ----------------------
# Install prerequisites
# ----------------------
RUN \
    echo "===> add webupd8 repository..."  && \
    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list  && \
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list  && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886  && \
    apt-get update  && \
    \
    \
    echo "===> install Java"  && \
    echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections  && \
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections  && \
    DEBIAN_FRONTEND=noninteractive  apt-get install -y --force-yes oracle-java7-installer oracle-java7-set-default  && \
    \
    \
    echo "===> clean up..."  && \
    rm -rf /var/cache/oracle-jdk7-installer  && \
    apt-get clean  && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /usr/lib/jvm/java-7-oracle

# -----------------------------
# Install WSO2 Identity Server
# -----------------------------
ADD packs/wso2is-${WSO2_IS_VERSION}-patched.zip /opt/wso2is-${WSO2_IS_VERSION}-patched.zip
# ADD packs/WSO2-IS-${WSO2_IS_VERSION}-SP01.zip /opt/WSO2-IS-${WSO2_IS_VERSION}-SP01.zip
RUN unzip /opt/wso2is-${WSO2_IS_VERSION}-patched.zip -d /opt/
# RUN unzip /opt/WSO2-IS-${WSO2_IS_VERSION}-SP01.zip -d /opt/
# WORKDIR /opt/WSO2-IS-${WSO2_IS_VERSION}-SP01
# RUN chmod +x /opt/WSO2-IS-${WSO2_IS_VERSION}-SP01/install_sp.sh
# RUN sh /opt/WSO2-IS-${WSO2_IS_VERSION}-SP01/install_sp.sh <<-END \                      y \                     END
ENV CARBON_HOME /opt/wso2is-${WSO2_IS_VERSION}

# ADD files/run.sh /opt/run.sh
# RUN chmod +x /opt/run.sh

#------------------------
# Copy Tomcat related PCA plugins
#-----------------------
ADD packs/plugins /mnt/plugins


EXPOSE 9443

# -----------------------
# Define entry point
# -----------------------
ENTRYPOINT /usr/local/bin/run | /usr/sbin/sshd -D

FROM centos:8
MAINTAINER siewer

# Required package installation
RUN yum update -y
RUN yum install java-11-openjdk-devel -y
RUN yum install maven -y
RUN yum install git -y
RUN yum install epel-release -y
RUN yum install python3-pip -y
RUN yum install nodejs -y
RUN yum install php-cli php-zip wget unzip php-json -y
RUN php -r "copy('https://getcomposer.org/installer', 'composer-setup.php');"
RUN php composer-setup.php --install-dir=/usr/local/bin --filename=composer

# Download DTrack
RUN mkdir /opt/dtrack && wget https://github.com/DependencyTrack/dependency-track/releases/download/3.8.0/dependency-track-embedded.war -O /opt/dtrack/dependency-track-embedded.war

# Building Mixeway Scanner APP
WORKDIR /app
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY ./src ./src
RUN mvn package -DskipTests && cp target/*.jar app.jar

# Copy startup script
COPY ./startup.sh ./startup.sh

ENTRYPOINT ["/bin/bash", "/app/startup.sh"]
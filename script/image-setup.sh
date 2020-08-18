# JAVA MVN & GRADLE
dnf install java-1.8.0-openjdk -y
dnf install unzip -y
dnf install maven -y
dnf install wget -y
dnf install curl -y	
dnf install git -y
wget https://services.gradle.org/distributions/gradle-5.0-bin.zip -P /tmp
unzip -d /opt/gradle /tmp/gradle-5.0-bin.zip
echo "export GRADLE_HOME=/opt/gradle/gradle-5.0" > /etc/profile.d/gradle.sh
echo "export PATH=/opt/gradle/gradle-5.0/bin:${PATH}" >> /etc/profile.d/gradle.sh

# DEPENDENCY TRACK
mkdir -p /opt/dependency-track && wget -O /opt/dependency-track/dependency-track-embedded.war https://github.com/DependencyTrack/dependency-track/releases/download/3.8.0/dependency-track-embedded.war
nohup java -Xmx4G -jar /opt/dependency-track/dependency-track-embedded.war 2> /dev/null &

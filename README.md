# Mixeway Scanner Aggregator
<img src="https://mixeway.io/wp-content/uploads/2020/08/mixeway_scanner-1.png">

Mixeway Scanner Aggregator is Spring Boot application which aggregate and integrate the most popular OpenSurce Vulnrability scanners avaliable.

## Supported Scanners:
* OWASP Dependency Track - https://dependencytrack.org
* Spotbugs - https://spotbugs.github.io 
* Bandit - https://github.com/PyCQA/bandit


## Scope of integration
MixewayScanner can be run as REST API or standalone. In REST mode it listen for scan request which contains GIT URL
for repository to be scanned. Next it clone repo, create DTrack project and send SBOM. In next phase SAST scanner is executed.
Detected vulnerabilities are pushed into console or to Mixeway if integration is configured.

## Requirements
* Docker installed
* Sonatype OSS username and key (for projects other then NPM) - https://ossindex.sonatype.org 
* If Maven require to download some custom libraries, link them via `-v ~/.m2:/root/.m2`

### Running options
* In standalone mode, running container inside directory You want to scan
```shell script
docker run -e OSS_USERNAME=<OSS_USERNAME> -e OSS_KEY=<OSS_KEY> -e MODE=STANDALONE -v ${PWD}:/opt/sources  mixeway/scanner:latest
```
if source to be scaned is located in current direcory. Otherwise, use `-v <location of sources>/opt/sources`

* In REST API mode, container is running and listetning on port :8443
```shell script
docker run -e OSS_USERNAME=<OSS_USERNAME> -e OSS_KEY=<OSS_KEY> -e MODE=REST mixeway/scanner:latest
```
example usage:
```$xslt
GET http://localhost:8443/run
{"target":"https://github.com/mixeway/mixewaybackend", "branch":"master", "type":"SAST"}
```
where target is URL for repo, branch is branch name to be sanned and type is SAST (only this type is supported in current version)

## TLS support for REST API
By default Mixeway Scanner use self-signed TLS certifiate generated during `docker build` action. 
If You want to use Your own certificate mount it as `certificate.p12` to `/opt/pki` location (e.g. `-v /etc/pki:/opt/pki`) and then
during `docker run` pass `-e PKCS12_PASSWORD=<password to pkcs12>` with PKCS12 password.

## Supported Languages

| Scanner version  | Languages |
|---|---|
|v0.9.0| JAVA-MAVEN   |
|v0.9.1| JAVA-MAVEN, Python3|
|v0.9.2| JAVA-MAVEN, Python3, PHP|
# Mixeway Scanner Aggregator

// To be done... We are just starting.

### Running options
* In standalone mode, running container inside directory You want to scan
```shell script
docker run ...
```
* In REST API mode, container is running and listetning on port :8443
```shell script
docker run ...
```

### Requirements
* If OpenSource scan will be conducted on projects other then NPM - sonatype OSS username and sonatype OSS key is required
* If Maven require to download some custom libraries, link them via `-v ~/.m2:/root/.m2`
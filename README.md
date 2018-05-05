# Nexus repository APT plugin

[![Build Status](https://travis-ci.org/sonatype-nexus-community/nexus-repository-apt.svg?branch=master)](https://travis-ci.org/sonatype-nexus-community/nexus-repository-apt)

* Version 1.0.6 (master) is for Nexus Repo 3.11.0
* Version 1.0.5 requires Nexus Repo 3.9.0
* For older Nexus Repo versions use tag 1.0.2

### Build
* Clone the project:

  `git clone https://github.com/sonatype-nexus-community/nexus-repository-apt`
* Build the plugin:

  ```
  cd nexus-repository-apt
  mvn
  ```
### Build with docker and create an image based on nexus repository 3

``` docker build -t nexus-repository-apt:3.11.0 .```

### Run a docker container from that image

``` docker run -d -p 8081:8081 --name nexus-repository-apt:3.11.0 ```

For further information like how to persist volumes check out [the GitHub Repo for the official Nexus Repository 3 Docker image](https://github.com/sonatype/docker-nexus3).

The application will now be available from your browser at http://localhost:8081

### Install
* Stop Nexus:

  `service nexus stop`

  or

  ```
  cd <nexus_dir>/bin
  ./nexus stop
  ```

* Copy the bundle into `<nexus_dir>/system/net/staticsnow/nexus-repository-apt/1.0.6/nexus-repository-apt-1.0.6.jar`
* Make the following additions marked with + to `<nexus_dir>/system/org/sonatype/nexus/assemblies/nexus-core-feature/3.x.y/nexus-core-feature-3.x.y-features.xml`
   ```
         <feature prerequisite="false" dependency="false">nexus-repository-maven</feature>
   +     <feature prerequisite="false" dependency="false">nexus-repository-apt</feature>
     </feature>
   ```
   And
   ```
   + <feature name="nexus-repository-apt" description="net.staticsnow:nexus-repository-apt" version="1.0.6">
   +     <details>net.staticsnow:nexus-repository-apt</details>
   +     <bundle>mvn:net.staticsnow/nexus-repository-apt/1.0.6</bundle>
   +     <bundle>mvn:org.apache.commons/commons-compress/1.11</bundle>
   + </feature>
    </features>
   ```
This will cause the plugin to be loaded and started with each startup of Nexus Repository.

### Manually upload a package to a new created repo:
`curl -u user:pass -X POST -H "Content-Type: multipart/form-data" --data-binary "@package.deb"  http://nexus_url:8081/repository/repo_name/`

### Create a snapshot of the current package lists for the repo that can be pulled from:
`curl -u user:pass -X MKCOL http://nexus_url:8081/repository/repo_name/snapshots/$SNAPSHOT_ID`

## The Fine Print

It is worth noting that this is **NOT SUPPORTED** by Sonatype, and is a contribution of Mike Poindexter's
plus us to the open source community (read: you!)

Remember:

* Use this contribution at the risk tolerance that you have
* Do **NOT** file Sonatype support tickets related to APT support
* **DO** file issues here on GitHub, so that the community can pitch in

Phew, that was easier than I thought. Last but not least of all:

Have fun building and using this plugin on the Nexus platform, we are glad to have you here!

## Getting help

Looking to contribute to our code but need some help? There's a few ways to get information:

* Chat with us on [Gitter](https://gitter.im/sonatype/nexus-developers)
* Check out the [Nexus3](http://stackoverflow.com/questions/tagged/nexus3) tag on Stack Overflow
* Check out the [Nexus Repository User List](https://groups.google.com/a/glists.sonatype.com/forum/?hl=en#!forum/nexus-users)

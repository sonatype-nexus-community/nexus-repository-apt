# Nexus repository APT plugin

*Version 1.0.4 (master) requires Nexus 3.8.0 - for older Nexus versions use tag 1.0.2*

### Build
* Clone the project:

  `git clone https://github.com/sonatype-nexus-community/nexus-repository-apt`
* Build the pluguin:

  ```
  cd nexus-repository-apt
  mvn
  ```
### Build with docker and create an image based on nexus repository 3

``` docker build -t nexus-repository-apt:3.8.0 .```

### Run a docker container from that image

``` docker run -d -p 8081:8081 --name nexus-repository-apt:3.8.0 ```

For further information like how to persist volumes view https://github.com/sonatype/docker-nexus3
The application is now available from the browser on localhost:8081

### Install
* Stop Nexus:

  `service nexus stop`

  or

  ```
  cd <nexus_dir>/bin
  ./nexus stop
  ```

* Copy the bundle into `<nexus_dir>/system/net/staticsnow/nexus-repository-apt/1.0.4/nexus-repository-apt-1.0.4.jar`
* Make the following additions marked with + to `<nexus_dir>/system/com/sonatype/nexus/assemblies/nexus-oss-feature/3.x.y/nexus-oss-feature-3.x.y-features.xml` (or `<nexus_dir>/system/com/sonatype/nexus/assemblies/nexus-pro-feature/3.x.y/nexus-pro-feature-3.x.y-features.xml` if using the Professional version)
   ```
         <feature prerequisite="false" dependency="false">nexus-repository-rubygems</feature>
   +     <feature prerequisite="false" dependency="false">nexus-repository-apt</feature>
         <feature prerequisite="false" dependency="false">nexus-repository-gitlfs</feature>
     </feature>
   ```
   And
   ```
   + <feature name="nexus-repository-apt" description="net.staticsnow:nexus-repository-apt" version="1.0.4">
   +     <details>net.staticsnow:nexus-repository-apt</details>
   +     <bundle>mvn:net.staticsnow/nexus-repository-apt/1.0.4</bundle>
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

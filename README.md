# Nexus repository APT plugin

### Build
* Clone the project:

  `git clone https://github.com/mpoindexter/nexus-repository-apt`
* Build the pluguin:

  ```
  cd nexus-repository
  mvn
  ```

### Install
* Stop Nexus:

  `service nexus stop`

  or

  ```
  cd <nexus_dir>/bin
  ./nexus stop
  ```

* Enable Nexus console: edit `<nexus_dir>/bin/nexus.vmoptions` and change `karaf.startLocalConsole`  to `true`.

  More details here: http://books.sonatype.com/nexus-book/3.0/reference/bundle-development.html

* Run Nexus' console:
  ```
  # sudo su - nexus
  $ cd <nexus_dir>/bin
  $ ./nexus run
  > bundle:install file:///tmp/nexus-repository-apt-1.0.2.jar
  > bundle:list
  ```
  (look for net.staticsnow:nexus-repository-apt ID, should be the last one)
  ```
  > bundle:start <net.staticsnow:nexus-repository-apt ID>
  ```

### Manually upload a package to a new created repo:
`curl -u user:pass -X POST -H "Content-Type: multipart/form-data" --data-binary "@package.deb"  http://nexus_url:8081/repository/repo_name/`

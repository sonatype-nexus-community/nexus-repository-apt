<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2018-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
## APT Repositories

### Introduction

[apt-get](https://help.ubuntu.com/community/AptGet/Howto) is a package management format used for Debian packages. 

This allows the repository manager to take advantage of the packages in the official apt repositories and other
public apt repositories without incurring repeated downloads of packages.

### Proxying APT Repositories

You can set up an apt proxy repository to access a remote repository location.

To proxy a apt repository, you simply create a new 'apt (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/display/NXRM3/Configuration#Configuration-RepositoryManagement) in
details. Minimal configuration steps are:

- Define 'Name'
- Define URL for 'Remote storage' e.g. []()
- Select a 'Blob store' for 'Storage'

### Configuring AptGet

To configure apt-get to use Nexus Repository as a Proxy for remote apt sites 

TBD

### Browsing APT Repository Packages

You can browse apt repositories in the user interface inspecting the components and assets and their details, as
described in [Browsing Repositories and Repository Groups](https://help.sonatype.com/display/NXRM3/Browsing+Repositories+and+Repository+Groups).

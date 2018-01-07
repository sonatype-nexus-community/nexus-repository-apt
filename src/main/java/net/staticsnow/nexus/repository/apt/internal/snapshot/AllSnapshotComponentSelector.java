/*
 * Nexus APT plugin.
 * 
 * Copyright (c) 2016-Present Michael Poindexter.
 * 
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package net.staticsnow.nexus.repository.apt.internal.snapshot;

import java.util.List;

import net.staticsnow.nexus.repository.apt.internal.debian.Release;

public class AllSnapshotComponentSelector
    implements SnapshotComponentSelector
{

  @Override
  public List<String> getArchitectures(Release release) {
    return release.getArchitectures();
  }

  @Override
  public List<String> getComponents(Release release) {
    return release.getComponents();
  }

}

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

import java.io.IOException;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Content;

@Facet.Exposed
public interface AptSnapshotFacet
    extends Facet
{
  public boolean isSnapshotableFile(String path);

  public void createSnapshot(String id, SnapshotComponentSelector spec) throws IOException;

  public Content getSnapshotFile(String id, String path) throws IOException;

  public void deleteSnapshot(String id) throws IOException;
}

/*
 * Nexus APT plugin.
 * 
 * Copyright (c) 2016-Present Michael Poindexter.
 * 
 * This file is licensed under the terms of the GNU General Public License Version 2.0
 * https://www.gnu.org/licenses/gpl-2.0.en.html
 * with the following clarification:
 * 
 * Combining this software with other components in a form that allows this software
 * to be automatically loaded constitutes creation of a derived work.  Any distribution
 * of Nexus that includes this plugin must be licensed under the GPL or compatible
 * licenses.
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

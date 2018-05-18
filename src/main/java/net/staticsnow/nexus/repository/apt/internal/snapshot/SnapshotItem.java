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

import org.sonatype.nexus.repository.view.Content;

public class SnapshotItem
{
  public static enum Role
  {
    RELEASE_INDEX, RELEASE_SIG, RELEASE_INLINE_INDEX, PACKAGE_INDEX_GZ, PACKAGE_INDEX_BZ2, PACKAGE_INDEX_XZ, PACKAGE_INDEX_RAW,
  }

  public static class ContentSpecifier
  {
    public final String path;
    public final Role role;

    public ContentSpecifier(String path, Role role) {
      super();
      this.path = path;
      this.role = role;
    }
  }

  public final ContentSpecifier specifier;
  public final Content content;

  public SnapshotItem(ContentSpecifier specifier, Content content) {
    super();
    this.specifier = specifier;
    this.content = content;
  }
}

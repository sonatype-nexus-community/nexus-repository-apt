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

import org.sonatype.nexus.repository.view.Content;

public class SnapshotItem
{
  public static enum Role
  {
    RELEASE_INDEX, RELEASE_SIG, RELEASE_INLINE_INDEX, PACKAGE_INDEX_GZ, PACKAGE_INDEX_BZ2, PACKAGE_INDEX_RAW,
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

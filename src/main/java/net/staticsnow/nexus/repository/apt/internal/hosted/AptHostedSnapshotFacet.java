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

package net.staticsnow.nexus.repository.apt.internal.hosted;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.sonatype.nexus.repository.view.Content;

import net.staticsnow.nexus.repository.apt.AptFacet;
import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotFacetSupport;
import net.staticsnow.nexus.repository.apt.internal.snapshot.SnapshotItem;
import net.staticsnow.nexus.repository.apt.internal.snapshot.SnapshotItem.ContentSpecifier;

@Named
public class AptHostedSnapshotFacet
    extends AptSnapshotFacetSupport
{
  @Override
  protected List<SnapshotItem> fetchSnapshotItems(List<ContentSpecifier> specs) throws IOException {
    try {
      return specs.stream().map(spec -> getItem(spec)).filter(item -> item != null).collect(Collectors.toList());
    }
    catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private SnapshotItem getItem(ContentSpecifier spec) {
    try {
      AptFacet apt = getRepository().facet(AptFacet.class);
      Content content = apt.get(spec.path);
      if (content == null) {
        return null;
      }

      return new SnapshotItem(spec, content);
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

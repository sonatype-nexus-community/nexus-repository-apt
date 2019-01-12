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

package org.sonatype.nexus.plugins.apt.internal.hosted;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.sonatype.nexus.repository.view.Content;

import org.sonatype.nexus.plugins.apt.AptFacet;
import org.sonatype.nexus.plugins.apt.internal.snapshot.AptSnapshotFacetSupport;
import org.sonatype.nexus.plugins.apt.internal.snapshot.SnapshotItem;
import org.sonatype.nexus.plugins.apt.internal.snapshot.SnapshotItem.ContentSpecifier;

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

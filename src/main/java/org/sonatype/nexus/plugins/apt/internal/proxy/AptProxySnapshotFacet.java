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

package org.sonatype.nexus.plugins.apt.internal.proxy;

import java.io.IOException;
import java.util.List;

import javax.inject.Named;

import org.sonatype.nexus.plugins.apt.internal.snapshot.AptSnapshotFacet;
import org.sonatype.nexus.plugins.apt.internal.snapshot.AptSnapshotFacetSupport;
import org.sonatype.nexus.plugins.apt.internal.snapshot.SnapshotItem;
import org.sonatype.nexus.plugins.apt.internal.snapshot.SnapshotItem.ContentSpecifier;

@Named
public class AptProxySnapshotFacet
    extends AptSnapshotFacetSupport
    implements AptSnapshotFacet
{
  @Override
  protected List<SnapshotItem> fetchSnapshotItems(List<ContentSpecifier> specs) throws IOException {
    AptProxyFacet proxyFacet = getRepository().facet(AptProxyFacet.class);
    return proxyFacet.getSnapshotItems(specs);
  }
}

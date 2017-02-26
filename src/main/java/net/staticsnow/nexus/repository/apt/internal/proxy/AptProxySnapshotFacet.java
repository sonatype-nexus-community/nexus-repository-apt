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

package net.staticsnow.nexus.repository.apt.internal.proxy;

import java.io.IOException;
import java.util.List;

import javax.inject.Named;

import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotFacet;
import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotFacetSupport;
import net.staticsnow.nexus.repository.apt.internal.snapshot.SnapshotItem;
import net.staticsnow.nexus.repository.apt.internal.snapshot.SnapshotItem.ContentSpecifier;

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

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

package net.staticsnow.nexus.repository.apt.internal;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.browse.BrowseNodeGenerator;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;

import com.google.common.base.Splitter;

@Singleton
@Named(AptFormat.NAME)
public class AptBrowseNodeGenerator
    implements
    BrowseNodeGenerator
{

  @Override
  public List<String> computeAssetPath(Asset asset, Component component) {
    List<String> path;
    if (component != null) {
      path = computeComponentPath(asset, component);
      path.add(asset.name());
      return path;
    } else {
      path = new ArrayList<>();
      String name = asset.name();
      if (name.endsWith(".deb") || name.endsWith(".udeb")) {
        path.add("packages");
      } else {
        path.add("metadata");
      }
      path.addAll(newArrayList(Splitter.on('/').omitEmptyStrings().split(name)));
    }
    return path;
  }

  @Override
  public List<String> computeComponentPath(Asset asset, Component component) {
    List<String> path = new ArrayList<>();
    path.add("snapshots");
    path.add(component.name());
    return path;
  }
}

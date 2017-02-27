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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile;
import net.staticsnow.nexus.repository.apt.internal.debian.Release;

public class FilteredSnapshotComponentSelector
    implements SnapshotComponentSelector
{

  private final ControlFile settings;

  public FilteredSnapshotComponentSelector(ControlFile settings) {
    this.settings = settings;
  }

  @Override
  public List<String> getArchitectures(Release release) {
    Optional<Set<String>> settingsArchitectures = settings.getField("Architectures")
        .map(s -> s.listValue())
        .map(l -> new HashSet<>(l));
    if (settingsArchitectures.isPresent()) {
      Set<String> releaseArchitectures = new HashSet<>(release.getArchitectures());
      releaseArchitectures.retainAll(settingsArchitectures.get());
      return new ArrayList<>(releaseArchitectures);
    }
    else {
      return release.getArchitectures();
    }
  }

  @Override
  public List<String> getComponents(Release release) {
    Optional<Set<String>> settingsComponents = settings.getField("Components").map(s -> s.listValue())
        .map(l -> new HashSet<>(l));
    if (settingsComponents.isPresent()) {
      Set<String> releaseComponents = new HashSet<>(release.getComponents());
      releaseComponents.retainAll(settingsComponents.get());
      return new ArrayList<>(releaseComponents);
    }
    else {
      return release.getComponents();
    }
  }

}

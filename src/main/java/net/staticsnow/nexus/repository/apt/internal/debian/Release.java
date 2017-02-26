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

package net.staticsnow.nexus.repository.apt.internal.debian;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Release
{
  private final ControlFile index;

  public Release(ControlFile index) {
    super();
    this.index = index;
  }

  public Optional<String> getOrigin() {
    return getValue("Origin");
  }

  public Optional<String> getLabel() {
    return getValue("Label");
  }

  public Optional<String> getSuite() {
    return getValue("Suite");
  }

  public Optional<String> getVersion() {
    return getValue("Version");
  }

  public Optional<String> getCodename() {
    return getValue("Codename");
  }

  public List<String> getComponents() {
    return index.getField("Components").map(s -> s.listValue()).orElse(Collections.emptyList());
  }

  public List<String> getArchitectures() {
    return index.getField("Architectures").map(s -> s.listValue()).orElse(Collections.emptyList());
  }

  public Optional<String> getDescription() {
    return getValue("Description");
  }

  private Optional<String> getValue(String name) {
    return index.getField(name).map(e -> e.value);
  }

}

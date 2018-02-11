/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package net.staticsnow.nexus.repository.apt.internal.snapshot;

import java.util.List;
import java.util.Optional;

import org.sonatype.goodies.testsupport.TestSupport;

import com.google.common.collect.Lists;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.ControlField;
import net.staticsnow.nexus.repository.apt.internal.debian.Release;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class FilteredSnapshotComponentSelectorTest
    extends TestSupport
{
  @Mock
  ControlFile settings;

  @Mock
  Release release;

  FilteredSnapshotComponentSelector underTest;

  @Before
  public void setUp() {
    ControlField fieldArc = new ControlField("Architectures", "sparc");
    Optional<ControlField> optionalFieldArc = Optional.of(fieldArc);
    ControlField fieldComp = new ControlField("Components", "component1");
    Optional<ControlField> optionalFieldComp = Optional.of(fieldComp);
    when(settings.getField("Architectures")).thenReturn(optionalFieldArc);
    when(settings.getField("Components")).thenReturn(optionalFieldComp);
    underTest = new FilteredSnapshotComponentSelector(settings);
  }

  @Test
  public void getArchitecturesTest() {
    List<String> architectures = Lists.newArrayList("sparc", "amd64");
    when(release.getArchitectures()).thenReturn(architectures);
    List<String> list = underTest.getArchitectures(release);
    assertThat(list.size(), is(equalTo(1)));
    assertThat(list.get(0), is(equalTo("sparc")));
  }

  @Test
  public void getComponentsTest() {
    List<String> components = Lists.newArrayList("component1", "component2");
    when(release.getComponents()).thenReturn(components);
    List<String> list = underTest.getComponents(release);
    assertThat(list.size(), is(equalTo(1)));
    assertThat(list.get(0), is(equalTo("component1")));
  }
}

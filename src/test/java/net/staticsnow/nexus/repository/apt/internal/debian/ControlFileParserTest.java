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
package net.staticsnow.nexus.repository.apt.internal.debian;

import org.sonatype.goodies.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ControlFileParserTest
    extends TestSupport
{
  ControlFileParser underTest;

  ControlFile controlFile;

  @Test
  public void controlFileParserTest() throws Exception{
    underTest = new ControlFileParser();
    controlFile = underTest.parseControlFile(getClass().getResourceAsStream("control"));

    assertThat(controlFile.getFields().size(), is(11));
    assertThat(controlFile.getField("Package").get().value, is(equalTo("grep")));
    assertThat(controlFile.getField("Essential").get().value, is(equalTo("yes")));
    assertThat(controlFile.getField("Priority").get().value, is(equalTo("required")));
    assertThat(controlFile.getField("Section").get().value, is(equalTo("base")));
    assertThat(controlFile.getField("Maintainer").get().value, is(equalTo("Wichert Akkerman <wakkerma@debian.org>")));
    assertThat(controlFile.getField("Architecture").get().value, is(equalTo("sparc")));
    assertThat(controlFile.getField("Version").get().value, is(equalTo("2.4-1")));
    assertThat(controlFile.getField("Pre-Depends").get().value, is(equalTo("libc6 (>= 2.0.105)")));
    assertThat(controlFile.getField("Provides").get().value, is(equalTo("rgrep")));
    assertThat(controlFile.getField("Conflicts").get().value, is(equalTo("rgrep")));
    assertThat(controlFile.getField("Description").get().value.length(), is(equalTo(555)));
  }
}

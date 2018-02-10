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
  }
}

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
package net.staticsnow.nexus.repository.apt.internal.debian;

import org.sonatype.goodies.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class VersionTest
    extends TestSupport
{
  private static final String TEST_VERSION = "2:7.3.429-2ubuntu2.1";
  private static final String LOWER_EPOCH_TEST_VERSION = "1:7.3.429-2ubuntu2.1";
  private static final String HIGHER_EPOCH_TEST_VERSION = "3:7.3.429-2ubuntu2.1";
  Version underTest;

  // TODO: There are a plethora of Debian version permutations that merit testing beyond this set, do that

  @Test
  public void compareToTestIdenticalVersion() {
    underTest = setUpVersion(TEST_VERSION);
    Version identicalVersion = setUpVersion(TEST_VERSION);

    assertThat(underTest.compareTo(identicalVersion), is(0));
  }

  @Test
  public void compareToTestLowerEpochVersion() {
    underTest = setUpVersion(TEST_VERSION);
    Version lowerEpochVersion = setUpVersion(LOWER_EPOCH_TEST_VERSION);

    assertThat(underTest.compareTo(lowerEpochVersion), is(1));
  }

  @Test
  public void compareToTestHigherEpochVersion() {
    underTest = setUpVersion(TEST_VERSION);
    Version higherEpochVersion = setUpVersion(HIGHER_EPOCH_TEST_VERSION);

    assertThat(underTest.compareTo(higherEpochVersion), is(-1));
  }

  @Test
  public void toStringTest() {
    underTest = setUpVersion(TEST_VERSION);

    assertThat(underTest.toString(), is(equalTo(TEST_VERSION)));
  }

  public Version setUpVersion(final String version) {
    return new Version(version);
  }
}

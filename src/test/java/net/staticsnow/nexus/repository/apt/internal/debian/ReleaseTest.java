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

import java.util.Optional;

import org.sonatype.goodies.testsupport.TestSupport;

import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.ControlField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class ReleaseTest
    extends TestSupport
{
  private static final String CONTROL_FIELD_TEST_RETURN_VALUE = "test";
  private static final String CONTROL_FIELD_ARCHITECTURES_RETURN_VALUE = "amd64";
  private static final String CONTROL_FIELD_COMPONENTS_RETURN_VALUE = "components";
  private static final String CONTROL_FILE_ORIGIN = "Origin";
  private static final String CONTROL_FILE_LABEL = "Label";
  private static final String CONTROL_FILE_SUITE = "Suite";
  private static final String CONTROL_FILE_VERSION = "Version";
  private static final String CONTROL_FILE_CODENAME = "Codename";
  private static final String CONTROL_FILE_DESCRIPTION = "Description";
  private static final String CONTROL_FILE_ARCHITECTURES = "Architectures";
  private static final String CONTROL_FILE_COMPONENTS = "Components";

  Release underTest;

  @Mock
  ControlFile controlFile;

  @Before
  public void setUp() { underTest = new Release(controlFile); }

  @Test
  public void getOriginTest() {
    setupControlField(CONTROL_FILE_ORIGIN, CONTROL_FIELD_TEST_RETURN_VALUE);

    assertThat(underTest.getOrigin().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getLabelTest() {
    setupControlField(CONTROL_FILE_LABEL, CONTROL_FIELD_TEST_RETURN_VALUE);

    assertThat(underTest.getLabel().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getSuiteTest() {
    setupControlField(CONTROL_FILE_SUITE, CONTROL_FIELD_TEST_RETURN_VALUE);

    assertThat(underTest.getSuite().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getVersionTest() {
    setupControlField(CONTROL_FILE_VERSION, CONTROL_FIELD_TEST_RETURN_VALUE);

    assertThat(underTest.getVersion().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getCodenameTest() {
    setupControlField(CONTROL_FILE_CODENAME, CONTROL_FIELD_TEST_RETURN_VALUE);

    assertThat(underTest.getCodename().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getDescriptionTest() {
    setupControlField(CONTROL_FILE_DESCRIPTION, CONTROL_FIELD_TEST_RETURN_VALUE);

    assertThat(underTest.getDescription().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getArchitecturesTest() {
    setupControlField(CONTROL_FILE_ARCHITECTURES, CONTROL_FIELD_ARCHITECTURES_RETURN_VALUE);

    assertThat(underTest.getArchitectures().get(0), is(CONTROL_FIELD_ARCHITECTURES_RETURN_VALUE));
    assertThat(underTest.getArchitectures().size(), is(1));
  }

  @Test
  public void getComponentsTest() {
    setupControlField(CONTROL_FILE_COMPONENTS, CONTROL_FIELD_COMPONENTS_RETURN_VALUE);

    assertThat(underTest.getComponents().get(0), is(CONTROL_FIELD_COMPONENTS_RETURN_VALUE));
    assertThat(underTest.getComponents().size(), is(1));
  }

  private void setupControlField(final String controlFieldName,
                                 final String controlFieldReturnValue)
  {
    ControlField field = new ControlField(controlFieldName, controlFieldReturnValue);
    Optional<ControlField> optionalField = Optional.of(field);
    when(controlFile.getField(controlFieldName)).thenReturn(optionalField);
  }
}

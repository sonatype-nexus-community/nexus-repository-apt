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
  private static final String CONTROL_FILE_ORIGIN = "Origin";
  private static final String CONTROL_FILE_LABEL = "Label";
  private static final String CONTROL_FILE_SUITE = "Suite";
  private static final String CONTROL_FILE_VERSION = "Version";
  private static final String CONTROL_FILE_CODENAME = "Codename";
  private static final String CONTROL_FILE_DESCRIPTION = "Description";

  Release underTest;

  @Mock
  ControlFile controlFile;

  @Before
  public void setUp() { underTest = new Release(controlFile); }

  @Test
  public void getOriginTest() {
    ControlField field = new ControlField(CONTROL_FILE_ORIGIN, CONTROL_FIELD_TEST_RETURN_VALUE);
    Optional<ControlField> optionalField = Optional.of(field);
    when(controlFile.getField(CONTROL_FILE_ORIGIN)).thenReturn(optionalField);

    assertThat(underTest.getOrigin().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getLabelTest() {
    ControlField field = new ControlField(CONTROL_FILE_LABEL, CONTROL_FIELD_TEST_RETURN_VALUE);
    Optional<ControlField> optionalField = Optional.of(field);
    when(controlFile.getField(CONTROL_FILE_LABEL)).thenReturn(optionalField);

    assertThat(underTest.getLabel().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getSuiteTest() {
    ControlField field = new ControlField(CONTROL_FILE_SUITE, CONTROL_FIELD_TEST_RETURN_VALUE);
    Optional<ControlField> optionalField = Optional.of(field);
    when(controlFile.getField(CONTROL_FILE_SUITE)).thenReturn(optionalField);

    assertThat(underTest.getSuite().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getVersionTest() {
    ControlField field = new ControlField(CONTROL_FILE_VERSION, CONTROL_FIELD_TEST_RETURN_VALUE);
    Optional<ControlField> optionalField = Optional.of(field);
    when(controlFile.getField(CONTROL_FILE_VERSION)).thenReturn(optionalField);

    assertThat(underTest.getVersion().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getCodenameTest() {
    ControlField field = new ControlField(CONTROL_FILE_CODENAME, CONTROL_FIELD_TEST_RETURN_VALUE);
    Optional<ControlField> optionalField = Optional.of(field);
    when(controlFile.getField(CONTROL_FILE_CODENAME)).thenReturn(optionalField);

    assertThat(underTest.getCodename().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }

  @Test
  public void getDescriptionTest() {
    ControlField field = new ControlField(CONTROL_FILE_DESCRIPTION, CONTROL_FIELD_TEST_RETURN_VALUE);
    Optional<ControlField> optionalField = Optional.of(field);
    when(controlFile.getField(CONTROL_FILE_DESCRIPTION)).thenReturn(optionalField);

    assertThat(underTest.getDescription().get(), is(CONTROL_FIELD_TEST_RETURN_VALUE));
  }
}

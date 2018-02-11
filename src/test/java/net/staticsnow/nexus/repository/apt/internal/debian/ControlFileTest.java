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

import java.util.List;

import org.sonatype.goodies.testsupport.TestSupport;

import com.google.common.collect.Lists;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.ControlField;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.Paragraph;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ControlFileTest
    extends TestSupport
{
  private static final String TEST_KEY = "test";
  private static final String TEST_VALUE = "test";
  ControlFile underTest;
  ControlField controlField;

  public void setUp(final String key, final String value) {
    controlField = new ControlField(key, value);
    List<Paragraph> paragraphList = Lists.newArrayList(
        new Paragraph(Lists.newArrayList(controlField))
    );
    underTest = new ControlFile(paragraphList);
  }

  @Test
  public void getFieldTest() {
    setUp(TEST_KEY, TEST_VALUE);

    assertThat(underTest.getField(TEST_KEY).get(), is(equalTo(controlField)));
  }

  @Test
  public void getFieldsTest() {
    setUp(TEST_KEY, TEST_VALUE);

    assertThat(underTest.getFields().size(), is(equalTo(1)));
    assertThat(underTest.getFields().get(0), is(instanceOf(ControlField.class)));
  }

  @Test
  public void getParagraphsTest() {
    setUp(TEST_KEY, TEST_VALUE);

    assertThat(underTest.getParagraphs().size(), is(equalTo(1)));
    assertThat(underTest.getParagraphs().get(0), is(instanceOf(Paragraph.class)));
  }
}

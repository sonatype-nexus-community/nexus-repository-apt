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

import java.util.List;

import org.sonatype.goodies.testsupport.TestSupport;

import com.google.common.collect.Lists;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.ControlField;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.Paragraph;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class ControlFileTest
    extends TestSupport
{
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
    setUp("test", "test");

    assertThat(underTest.getField("test").get(), is(equalTo(controlField)));
  }

  @Test
  public void getFieldsTest() {
    setUp("test", "test");

    assertThat(underTest.getFields().size(), is(equalTo(1)));
    assertThat(underTest.getFields().get(0), is(instanceOf(ControlField.class)));
  }

  @Test
  public void getParagraphsTest() {
    setUp("test", "test");

    assertThat(underTest.getParagraphs().size(), is(equalTo(1)));
    assertThat(underTest.getParagraphs().get(0), is(instanceOf(Paragraph.class)));
  }
}

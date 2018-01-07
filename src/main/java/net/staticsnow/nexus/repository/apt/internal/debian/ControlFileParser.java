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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;

import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.ControlField;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.Paragraph;

public class ControlFileParser
{
  private static final Pattern FIELD_PATTERN = Pattern.compile("((?:[\\!-9]|[\\;-\\~])+):(.*)");
  private final List<Paragraph> paragraphs = new ArrayList<>();
  private final List<ControlField> fields = new ArrayList<>();
  private final StringBuilder valueBuilder = new StringBuilder();
  private final StringBuilder sigBuilder = new StringBuilder();
  private boolean inField = false;
  private String fieldName;

  public ControlFile parseControlFile(InputStream stream) throws IOException {
    paragraphs.clear();
    fields.clear();
    valueBuilder.setLength(0);
    sigBuilder.setLength(0);
    inField = false;

    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.trim().length() == 0) {
        finishField();
        finishParagraph();
        continue;
      }

      int first = line.codePointAt(0);
      if (first == '#') {
        continue;
      }
      if (Character.isWhitespace(first)) {
        valueBuilder.append('\n');
        valueBuilder.append(line);
      }
      else {
        finishField();
        beginField(line);
      }
    }

    finishField();
    finishParagraph();

    return new ControlFile(paragraphs);
  }

  private void finishParagraph() {
    if (fields.isEmpty()) {
      return;
    }
    paragraphs.add(new Paragraph(fields));
    fields.clear();
  }

  private void finishField() {
    if (!inField) {
      return;
    }
    fields.add(new ControlField(fieldName, valueBuilder.toString()));
    valueBuilder.setLength(0);
    inField = false;
  }

  private void beginField(String line) throws IOException {
    Matcher m = FIELD_PATTERN.matcher(line);
    if (!m.matches()) {
      throw new IOException("Invalid line: " + line);
    }
    fieldName = m.group(1);
    valueBuilder.append(m.group(2).trim());
    inField = true;
  }
}

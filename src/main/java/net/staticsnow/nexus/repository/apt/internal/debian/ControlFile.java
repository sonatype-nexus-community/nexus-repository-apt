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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControlFile
{
  public static class Builder
  {
    private List<Paragraph> paragraphs;

    private Builder(List<Paragraph> paragraphs) {
      super();
      this.paragraphs = paragraphs;
    }

    public Builder removeParagraphs(Predicate<Paragraph> p) {
      paragraphs = paragraphs.stream().filter(p).collect(Collectors.toList());
      return this;
    }

    public Builder addParagraph(Paragraph p) {
      paragraphs.add(p);
      return this;
    }

    public Builder replaceParagraph(Predicate<Paragraph> predicate, Paragraph p) {
      paragraphs = Stream
          .concat(paragraphs.stream().filter(predicate), Stream.of(p))
          .collect(Collectors.toList());
      return this;
    }

    public Builder transformParagraphs(Predicate<Paragraph> predicate, Function<Paragraph, Paragraph> transform) {
      paragraphs = paragraphs.stream()
          .filter(predicate)
          .map(transform)
          .collect(Collectors.toList());
      return this;
    }

    public ControlFile build() {
      return new ControlFile(paragraphs);
    }
  }

  public static Builder newBuilder() {
    return new Builder(new ArrayList<>());
  }

  private final List<Paragraph> paragraphs;

  public ControlFile(List<Paragraph> paragraphs) {
    super();
    this.paragraphs = new ArrayList<>(paragraphs);
  }

  public Builder builder() {
    return new Builder(new ArrayList<>(paragraphs));
  }

  public List<Paragraph> getParagraphs() {
    return paragraphs;
  }

  public Optional<ControlField> getField(String name) {
    if (paragraphs.isEmpty()) {
      return Optional.empty();
    }

    return paragraphs.get(0).getField(name);
  }

  public List<ControlField> getFields() {
    if (paragraphs.isEmpty()) {
      return Collections.emptyList();
    }

    return paragraphs.get(0).getFields();
  }

  public static class Paragraph
  {
    private final List<ControlField> fields;

    public Paragraph(List<ControlField> fields) {
      super();
      this.fields = new ArrayList<>(fields);
    }

    public Optional<ControlField> getField(String name) {
      for (ControlField f : fields) {
        if (f.key.equals(name)) {
          return Optional.of(f);
        }
      }

      return Optional.empty();
    }

    public List<ControlField> getFields() {
      return Collections.unmodifiableList(fields);
    }

    public Paragraph withFields(List<ControlField> updateFields) {
      Map<String, ControlField> index = updateFields.stream().collect(Collectors.toMap(f -> f.key, f -> f));
      return new Paragraph(Stream
          .concat(fields.stream().filter(f -> !index.containsKey(f.key)), updateFields.stream())
          .collect(Collectors.toList()));
    }

    @Override
    public String toString() {
      return fields.stream()
          .map(f -> f.key + ": " + f.value)
          .collect(Collectors.joining("\n"));
    }
  }

  public static class ControlField
  {
    public final String key;
    public final String value;

    public ControlField(String key, String value) {
      super();
      this.key = key;
      this.value = value;
    }

    public String foldedValue() {
      return Arrays.stream(value.split("\n"))
          .map(s -> s.trim())
          .collect(Collectors.joining());
    }

    public List<String> listValue() {
      return Arrays.asList(value.trim().split("\\s+"));
    }
  }
}

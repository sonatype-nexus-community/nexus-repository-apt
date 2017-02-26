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

package net.staticsnow.nexus.repository.apt.internal.hosted;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.bouncycastle.util.io.TeeOutputStream;
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

/**
 * Stores a set of temp files, automatically compressing each into a GZIP, BZ2 and plain format.
 *
 */
class CompressingTempFileStore
    implements AutoCloseable
{
  private final Map<String, FileHolder> holdersByKey = new HashMap<>();

  public Writer openOutput(String key) throws UncheckedIOException {
    try {
      if (holdersByKey.containsKey(key)) {
        throw new IllegalStateException("Output already opened");
      }
      FileHolder holder = new FileHolder();
      holdersByKey.put(key, holder);
      return new OutputStreamWriter(new TeeOutputStream(
          new TeeOutputStream(new GZIPOutputStream(Files.newOutputStream(holder.gzTempFile)),
              new BZip2CompressorOutputStream(Files.newOutputStream(holder.bzTempFile))),
          Files.newOutputStream(holder.plainTempFile)), Charsets.UTF_8);
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public Map<String, FileMetadata> getFiles() {
    return Maps.transformValues(holdersByKey, holder -> new FileMetadata(holder));
  }

  public void close() {
    for (FileHolder holder : holdersByKey.values()) {
      try {
        Files.delete(holder.bzTempFile);
      }
      catch (IOException e) {
      }
      try {
        Files.delete(holder.gzTempFile);
      }
      catch (IOException e) {
      }
    }
  }

  public static class FileMetadata
  {
    private final FileHolder holder;

    private FileMetadata(FileHolder holder) {
      this.holder = holder;
    }

    public long bzSize() {
      return holder.bzStream.getByteCount();
    }

    public InputStreamSupplier bzSupplier() {
      return () -> Files.newInputStream(holder.bzTempFile);
    }

    public long gzSize() {
      return holder.gzStream.getByteCount();
    }

    public InputStreamSupplier gzSupplier() {
      return () -> Files.newInputStream(holder.gzTempFile);
    }

    public long plainSize() {
      return holder.plainStream.getByteCount();
    }

    public InputStreamSupplier plainSupplier() {
      return () -> Files.newInputStream(holder.plainTempFile);
    }
  }

  private static class FileHolder
  {
    final CountingOutputStream plainStream;
    final Path plainTempFile;
    final CountingOutputStream gzStream;
    final Path gzTempFile;
    final CountingOutputStream bzStream;
    final Path bzTempFile;

    public FileHolder() throws IOException {
      super();
      this.plainTempFile = Files.createTempFile("", "");
      this.plainStream = new CountingOutputStream(Files.newOutputStream(plainTempFile));
      this.gzTempFile = Files.createTempFile("", "");
      this.gzStream = new CountingOutputStream(Files.newOutputStream(gzTempFile));
      this.bzTempFile = Files.createTempFile("", "");
      this.bzStream = new CountingOutputStream(Files.newOutputStream(bzTempFile));
    }
  }
}

package net.staticsnow.nexus.repository.apt.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;

import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFileParser;

public class AptPackageParser {
  public static ControlFile parsePackage(final Supplier<InputStream> supplier) throws IOException {
    try (ArArchiveInputStream is = new ArArchiveInputStream(supplier.get())) {
      ControlFile control = null;
      ArchiveEntry debEntry;
      while ((debEntry = is.getNextEntry()) != null) {
        InputStream controlStream;
        switch (debEntry.getName()) {
          case "control.tar":
            controlStream = new CloseShieldInputStream(is);
            break;
          case "control.tar.gz":
            controlStream = new GZIPInputStream(new CloseShieldInputStream(is));
            break;
          case "control.tar.xz":
            controlStream = new XZCompressorInputStream(new CloseShieldInputStream(is));
          default:
            continue;
        }

        try (TarArchiveInputStream controlTarStream = new TarArchiveInputStream(controlStream)) {
          ArchiveEntry tarEntry;
          while ((tarEntry = controlTarStream.getNextEntry()) != null) {
            if (tarEntry.getName().equals("control") || tarEntry.getName().equals("./control")) {
              control = new ControlFileParser().parseControlFile(controlTarStream);
            }
          }
        }
      }
      return control;
    }
  }
}
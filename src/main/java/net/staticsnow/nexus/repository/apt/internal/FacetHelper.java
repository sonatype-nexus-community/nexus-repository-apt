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

package net.staticsnow.nexus.repository.apt.internal;

import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256;

import java.util.Arrays;
import java.util.List;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;

import net.staticsnow.nexus.repository.apt.AptFacet;
import net.staticsnow.nexus.repository.apt.internal.snapshot.SnapshotItem;
import net.staticsnow.nexus.repository.apt.internal.snapshot.SnapshotItem.ContentSpecifier;

public class FacetHelper
{
  public static final List<HashAlgorithm> hashAlgorithms = Arrays.asList(MD5, SHA1, SHA256);

  public static List<ContentSpecifier> getReleaseIndexSpecifiers(AptFacet facet) {
    if (facet.isFlat()) {
      return Arrays.asList(
          new ContentSpecifier("Release", SnapshotItem.Role.RELEASE_INDEX),
          new ContentSpecifier("Release.gpg", SnapshotItem.Role.RELEASE_SIG),
          new ContentSpecifier("InRelease", SnapshotItem.Role.RELEASE_INLINE_INDEX));
    }
    else {
      String dist = facet.getDistribution();
      return Arrays.asList(
          new ContentSpecifier("dists/" + dist + "/Release", SnapshotItem.Role.RELEASE_INDEX),
          new ContentSpecifier("dists/" + dist + "/Release.gpg", SnapshotItem.Role.RELEASE_SIG),
          new ContentSpecifier("dists/" + dist + "/InRelease", SnapshotItem.Role.RELEASE_INLINE_INDEX));
    }

  }

  public static List<ContentSpecifier> getReleasePackageIndexes(AptFacet facet, String component, String arch) {
    if (facet.isFlat()) {
      return Arrays.asList(
          new ContentSpecifier("Packages", SnapshotItem.Role.PACKAGE_INDEX_RAW),
          new ContentSpecifier("Packages.gz", SnapshotItem.Role.PACKAGE_INDEX_GZ),
          new ContentSpecifier("Packages.bz2", SnapshotItem.Role.PACKAGE_INDEX_BZ2));
    }
    else {
      String dist = facet.getDistribution();
      return Arrays.asList(
          new ContentSpecifier("dists/" + dist + "/" + component + "/binary-" + arch + "/Packages", SnapshotItem.Role.PACKAGE_INDEX_RAW),
          new ContentSpecifier("dists/" + dist + "/" + component + "/binary-" + arch + "/Packages.gz", SnapshotItem.Role.PACKAGE_INDEX_GZ),
          new ContentSpecifier("dists/" + dist + "/" + component + "/binary-" + arch + "/Packages.bz2", SnapshotItem.Role.PACKAGE_INDEX_BZ2));
    }

  }

  public static String determineContentType(SnapshotItem item) {
    String ct = item.content.getContentType();
    if (ct == null) {
      switch (item.specifier.role) {
        case RELEASE_INDEX:
        case RELEASE_INLINE_INDEX:
        case PACKAGE_INDEX_RAW:
          return AptMimeTypes.TEXT;
        case RELEASE_SIG:
          return AptMimeTypes.SIGNATURE;
        case PACKAGE_INDEX_BZ2:
          return AptMimeTypes.BZIP;
        case PACKAGE_INDEX_GZ:
          return AptMimeTypes.GZIP;
      }
    }
    return ct;
  }

  public static Content toContent(final Asset asset, final Blob blob) {
    final Content content = new Content(new BlobPayload(blob, asset.requireContentType()));
    Content.extractFromAsset(asset, hashAlgorithms, content.getAttributes());
    return content;
  }

  private FacetHelper() {
    //empty
  }
}

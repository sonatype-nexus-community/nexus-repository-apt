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

import static java.util.Collections.singletonList;
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter;
import org.sonatype.nexus.repository.storage.Query;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;

import com.google.common.base.Supplier;
import net.staticsnow.nexus.repository.apt.AptFacet;
import net.staticsnow.nexus.repository.apt.internal.snapshot.SnapshotItem;
import net.staticsnow.nexus.repository.apt.internal.snapshot.SnapshotItem.ContentSpecifier;

public class FacetHelper
{
  public static final List<HashAlgorithm> hashAlgorithms = Arrays.asList(MD5, SHA1, SHA256);

  private FacetHelper() {
    // empty
  }

  public static List<ContentSpecifier> getReleaseIndexSpecifiers(AptFacet facet) {
    if (facet.isFlat()) {
      return Arrays.asList(
          new ContentSpecifier("Release", SnapshotItem.Role.RELEASE_INDEX),
          new ContentSpecifier("Release.gpg", SnapshotItem.Role.RELEASE_SIG),
          new ContentSpecifier("InRelease", SnapshotItem.Role.RELEASE_INLINE_INDEX)
      );
    }
    else {
      String dist = facet.getDistribution();
      return Arrays.asList(
          new ContentSpecifier("dists/" + dist + "/Release", SnapshotItem.Role.RELEASE_INDEX),
          new ContentSpecifier("dists/" + dist + "/Release.gpg", SnapshotItem.Role.RELEASE_SIG),
          new ContentSpecifier("dists/" + dist + "/InRelease", SnapshotItem.Role.RELEASE_INLINE_INDEX)
      );
    }

  }

  public static List<ContentSpecifier> getReleasePackageIndexes(AptFacet facet, String component, String arch) {
    if (facet.isFlat()) {
      return Arrays.asList(
          new ContentSpecifier("Packages", SnapshotItem.Role.PACKAGE_INDEX_RAW),
          new ContentSpecifier("Packages.gz", SnapshotItem.Role.PACKAGE_INDEX_GZ),
          new ContentSpecifier("Packages.bz2", SnapshotItem.Role.PACKAGE_INDEX_BZ2)
      );
    }
    else {
      String dist = facet.getDistribution();
      return Arrays.asList(
          new ContentSpecifier("dists/" + dist + "/" + component + "/binary-" + arch + "/Packages",
              SnapshotItem.Role.PACKAGE_INDEX_RAW),
          new ContentSpecifier("dists/" + dist + "/" + component + "/binary-" + arch + "/Packages.gz",
              SnapshotItem.Role.PACKAGE_INDEX_GZ),
          new ContentSpecifier("dists/" + dist + "/" + component + "/binary-" + arch + "/Packages.bz2",
              SnapshotItem.Role.PACKAGE_INDEX_BZ2)
      );
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

  /**
   * Find a component by it's name
   *
   * @return found component of null if not found
   */
  @Nullable
  public static Component findComponent(final StorageTx tx,
                                 final Repository repository,
                                 final String name)
  {
    Iterable<Component> components = tx.findComponents(
        Query.builder()
            .where(P_NAME).eq(name)
            .build(),
        singletonList(repository)
    );
    if (components.iterator().hasNext()) {
      return components.iterator().next();
    }
    return null;
  }

  /**
   * Find an asset by it's name.
   *
   * @return found asset or null if not found
   */
  @Nullable
  public static Asset findAsset(final StorageTx tx, final Bucket bucket, final String assetName) {
    return tx.findAssetWithProperty(MetadataNodeEntityAdapter.P_NAME, assetName, bucket);
  }

  /**
   * Save an asset and create a blob
   *
   * @return blob content
   */
  public static Content saveAsset(final StorageTx tx,
                                  final Asset asset,
                                  final Payload payload) throws IOException
  {
    AttributesMap contentAttributes = null;
    String contentType = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
      contentType = payload.getContentType();
    }
    InputStream is = payload.openInputStream();
    return saveAsset(tx, asset, () -> is, contentType, contentAttributes);
  }

  /**
   * Save an asset and create a blob
   *
   * @return blob content
   */
  public static Content saveAsset(final StorageTx tx,
                                  final Asset asset,
                                  final Supplier<InputStream> contentSupplier,
                                  final String contentType,
                                  @Nullable final AttributesMap contentAttributes) throws IOException
  {
    Content.applyToAsset(asset, Content.maintainLastModified(asset, contentAttributes));
    AssetBlob assetBlob = tx.setBlob(
        asset, asset.name(), contentSupplier, hashAlgorithms, null, contentType, false
    );

    asset.markAsAccessed();
    tx.saveAsset(asset);
    return toContent(asset, assetBlob.getBlob());
  }

  public static Content toContent(final Asset asset, final Blob blob) {
    final Content content = new Content(new BlobPayload(blob, asset.requireContentType()));
    Content.extractFromAsset(asset, hashAlgorithms, content.getAttributes());
    return content;
  }
}

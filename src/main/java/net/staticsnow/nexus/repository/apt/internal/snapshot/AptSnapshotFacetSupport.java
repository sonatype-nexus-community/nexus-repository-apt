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

package net.staticsnow.nexus.repository.apt.internal.snapshot;

import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.transaction.Transactional;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.orientechnologies.common.concur.ONeedRetryException;

import net.staticsnow.nexus.repository.apt.AptFacet;
import net.staticsnow.nexus.repository.apt.internal.FacetHelper;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFileParser;
import net.staticsnow.nexus.repository.apt.internal.debian.Release;

public abstract class AptSnapshotFacetSupport
    extends FacetSupport
    implements AptSnapshotFacet
{

  @Override
  public boolean isSnapshotableFile(String path) {
    return !path.endsWith(".deb") && !path.endsWith(".DEB");
  }

  @Transactional(retryOn = { ONeedRetryException.class })
  @Override
  public void createSnapshot(String id, SnapshotComponentSelector selector) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    StorageFacet storageFacet = facet(StorageFacet.class);
    Bucket bucket = tx.findBucket(getRepository());
    Component component = tx.createComponent(bucket, getRepository().getFormat()).name(id);
    tx.saveComponent(component);
    for (SnapshotItem item : collectSnapshotItems(selector)) {
      String assetName = createAssetPath(id, item.specifier.path);
      Asset asset = tx.createAsset(bucket, component).name(assetName);
      try (final TempBlob streamSupplier = storageFacet.createTempBlob(item.content.openInputStream(), FacetHelper.hashAlgorithms)) {
        AssetBlob blob = tx.createBlob(item.specifier.path, streamSupplier, FacetHelper.hashAlgorithms, null,
            FacetHelper.determineContentType(item), true);
        tx.attachBlob(asset, blob);
      }
      finally {
        item.content.close();
      }
      tx.saveAsset(asset);
    }
  }

  @Transactional(retryOn = { ONeedRetryException.class })
  @Override
  public Content getSnapshotFile(String id, String path) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());
    Component component = tx.findComponentWithProperty(P_NAME, id, bucket);
    if (component == null) {
      return null;
    }

    final Asset asset = tx.findAssetWithProperty(P_NAME, createAssetPath(id, path), component);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }

    final Blob blob = tx.requireBlob(asset.requireBlobRef());
    return FacetHelper.toContent(asset, blob);
  }

  @Transactional(retryOn = { ONeedRetryException.class })
  @Override
  public void deleteSnapshot(String id) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());
    Component component = tx.findComponentWithProperty(P_NAME, id, bucket);
    if (component == null) {
      return;
    }
    tx.deleteComponent(component);
  }

  protected Iterable<SnapshotItem> collectSnapshotItems(SnapshotComponentSelector selector) throws IOException {
    AptFacet aptFacet = getRepository().facet(AptFacet.class);

    List<SnapshotItem> result = new ArrayList<>();
    List<SnapshotItem> releaseIndexItems = fetchSnapshotItems(FacetHelper.getReleaseIndexSpecifiers(aptFacet));
    Map<SnapshotItem.Role, SnapshotItem> itemsByRole = new HashMap<>(
        releaseIndexItems.stream().collect(Collectors.toMap((SnapshotItem item) -> item.specifier.role, item -> item)));
    InputStream releaseStream = null;
    if (itemsByRole.containsKey(SnapshotItem.Role.RELEASE_INDEX)) {
      releaseStream = itemsByRole.get(SnapshotItem.Role.RELEASE_INDEX).content.openInputStream();
    }
    else {
      InputStream is = itemsByRole.get(SnapshotItem.Role.RELEASE_INLINE_INDEX).content.openInputStream();
      if (is != null) {
        ArmoredInputStream aIs = new ArmoredInputStream(is);
        releaseStream = new FilterInputStream(aIs)
        {
          boolean done = false;

          public int read() throws IOException {
            if (done) {
              return -1;
            }
            int c = aIs.read();
            if (c < 0 || !aIs.isClearText()) {
              done = true;
              return -1;
            }
            return c;
          }
        };
      }
    }

    if (releaseStream == null) {
      throw new IOException("Invalid upstream repository:  no release index present");
    }

    Release release;
    try {
      ControlFile index = new ControlFileParser().parseControlFile(releaseStream);
      release = new Release(index);
    }
    finally {
      releaseStream.close();
    }

    result.addAll(releaseIndexItems);

    if (aptFacet.isFlat()) {
      result.addAll(fetchSnapshotItems(FacetHelper.getReleasePackageIndexes(aptFacet, null, null)));
    }
    else {
      List<String> archs = selector.getArchitectures(release);
      List<String> comps = selector.getComponents(release);
      for (String arch : archs) {
        for (String comp : comps) {
          result.addAll(fetchSnapshotItems(FacetHelper.getReleasePackageIndexes(aptFacet, comp, arch)));
        }
      }
    }

    // TODO: Verify checksums and redownload if needed.
    return result;
  }

  private String createAssetPath(String id, String path) {
    String assetName = "snapshots/" + id + "/" + path;
    return assetName;
  }

  protected abstract List<SnapshotItem> fetchSnapshotItems(List<SnapshotItem.ContentSpecifier> specs)
      throws IOException;
}

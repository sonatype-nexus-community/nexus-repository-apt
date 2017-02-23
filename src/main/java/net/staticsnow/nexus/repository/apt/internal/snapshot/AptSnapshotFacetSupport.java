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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bouncycastle.bcpg.ArmoredInputStream;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalDeleteBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.transaction.UnitOfWork;

import net.staticsnow.nexus.repository.apt.AptFacet;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFileParser;
import net.staticsnow.nexus.repository.apt.internal.debian.Release;

import static net.staticsnow.nexus.repository.apt.internal.FacetHelper.hashAlgorithms;
import static net.staticsnow.nexus.repository.apt.internal.FacetHelper.findAsset;
import static net.staticsnow.nexus.repository.apt.internal.FacetHelper.saveAsset;
import static net.staticsnow.nexus.repository.apt.internal.FacetHelper.toContent;
import static net.staticsnow.nexus.repository.apt.internal.FacetHelper.getReleaseIndexSpecifiers;
import static net.staticsnow.nexus.repository.apt.internal.FacetHelper.getReleasePackageIndexes;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;

public abstract class AptSnapshotFacetSupport
    extends FacetSupport
    implements AptSnapshotFacet
{

  @Override
  public boolean isSnapshotableFile(String path) {
    return !path.endsWith(".deb") && !path.endsWith(".DEB");
  }

  @Override
  @TransactionalStoreBlob
  public void createSnapshot(String id, SnapshotComponentSelector selector) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());
    StorageFacet storageFacet = facet(StorageFacet.class);

    // TODO: Should find component before you try to save one
    Component component = tx.createComponent(bucket, getRepository().getFormat()).name(id);
    tx.saveComponent(component);

    for (SnapshotItem item : collectSnapshotItems(selector)) {
      String assetName = createAssetPath(id, item.specifier.path);
      Asset asset = tx.createAsset(bucket, component).name(assetName);

      try (TempBlob tempBlob = storageFacet.createTempBlob(item.content, hashAlgorithms)) {
        // tx.attachBlob(asset, tempBlob);
      }
      finally {
        item.content.close();
      }
      // saveAsset(asset);
    }
  }

  @Override
  @TransactionalTouchBlob
  public Content getSnapshotFile(String id, String path) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();

    // TODO: Try find with just path
    final Asset asset = findAsset(tx, tx.findBucket(getRepository()), path);

    if (asset == null) {
      return null;
    }
    if (asset.markAsAccessed()) {
      tx.saveAsset(asset);
    }

    return toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  @TransactionalDeleteBlob
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
    List<SnapshotItem> releaseIndexItems = fetchSnapshotItems(getReleaseIndexSpecifiers(aptFacet));
    Map<SnapshotItem.Role, SnapshotItem> itemsByRole = new HashMap<>(releaseIndexItems.stream()
        .collect(Collectors.toMap((SnapshotItem item) -> item.specifier.role, item -> item)));
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
      throw new IOException("Invalid upstream repository: no release index present");
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
      result.addAll(fetchSnapshotItems(getReleasePackageIndexes(aptFacet, null, null)));
    }
    else {
      List<String> archs = selector.getArchitectures(release);
      List<String> comps = selector.getComponents(release);
      for (String arch : archs) {
        for (String comp : comps) {
          result.addAll(fetchSnapshotItems(getReleasePackageIndexes(aptFacet, comp, arch)));
        }
      }
    }

    //TODO:  Verify checksums and redownload if needed.
    return result;
  }

  private String createAssetPath(String id, String path) {
    String assetName = "snapshots/" + id + "/" + path;
    return assetName;
  }

  protected abstract List<SnapshotItem> fetchSnapshotItems(List<SnapshotItem.ContentSpecifier> specs)
      throws IOException;
}

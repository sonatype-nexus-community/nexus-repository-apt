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

package net.staticsnow.nexus.repository.apt.internal.hosted;

import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.inject.Named;

import org.bouncycastle.openpgp.PGPException;
import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.DefaultComponentMaintenanceImpl;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.transaction.Transactional;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.orientechnologies.common.concur.ONeedRetryException;

import net.staticsnow.nexus.repository.apt.internal.hosted.AptHostedFacet.AssetAction;

@Named
public class AptHostedComponentMaintenanceFacet
    extends DefaultComponentMaintenanceImpl
{
  @Transactional(retryOn = ONeedRetryException.class)
  @Override
  protected void deleteAssetTx(EntityId assetId, boolean deleteBlobs) {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = tx.findAsset(assetId, tx.findBucket(getRepository()));
    if (asset == null) {
      return;
    }
    String assetKind = asset.formatAttributes().get(P_ASSET_KIND, String.class);
    super.deleteAssetTx(assetId, deleteBlobs);
    if ("DEB".equals(assetKind)) {
      try {
        getRepository().facet(AptHostedFacet.class)
            .rebuildIndexes(new AptHostedFacet.AssetChange(AssetAction.REMOVED, asset));
      }
      catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      catch (PGPException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

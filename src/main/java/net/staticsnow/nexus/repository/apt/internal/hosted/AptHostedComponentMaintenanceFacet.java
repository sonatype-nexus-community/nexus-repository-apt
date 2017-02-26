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
  protected void deleteAssetTx(EntityId assetId) {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = tx.findAsset(assetId, tx.findBucket(getRepository()));
    if (asset == null) {
      return;
    }
    String assetKind = asset.formatAttributes().get(P_ASSET_KIND, String.class);
    super.deleteAssetTx(assetId);
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

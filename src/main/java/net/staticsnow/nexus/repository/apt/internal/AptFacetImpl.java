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

import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalDeleteBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.annotations.VisibleForTesting;

import net.staticsnow.nexus.repository.apt.AptFacet;

@Named
public class AptFacetImpl
    extends FacetSupport
    implements AptFacet
{

  @VisibleForTesting
  static final String CONFIG_KEY = "apt";

  @VisibleForTesting
  static class Config
  {
    @NotNull(groups = { HostedType.ValidationGroup.class, ProxyType.ValidationGroup.class })
    public String distribution;

    @NotNull(groups = { ProxyType.ValidationGroup.class })
    public boolean flat;
  }

  private Config config;

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    facet(ConfigurationFacet.class).validateSection(
        configuration,
        CONFIG_KEY, 
        Config.class, 
        Default.class,
        getRepository().getType().getValidationGroup());
  }

  @Override
  protected void doInit(final Configuration configuration) throws Exception {
    super.doInit(configuration);
    getRepository().facet(StorageFacet.class).registerWritePolicySelector(new AptWritePolicySelector());
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
  }

  @Override
  protected void doDestroy() throws Exception {
    config = null;
  }

  @Override
  public String getDistribution() {
    return config.distribution;
  }

  @Override
  public boolean isFlat() {
    return config.flat;
  }

  @Override
  @Nullable
  @TransactionalTouchBlob
  public Content get(String path) throws IOException {
    final StorageTx tx = UnitOfWork.currentTx();
    final Asset asset = tx.findAssetWithProperty(P_NAME, path, tx.findBucket(getRepository()));
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }

    return FacetHelper.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  @TransactionalStoreBlob
  public Content put(String path, Payload content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (final TempBlob tembBlob = storageFacet.createTempBlob(content, FacetHelper.hashAlgorithms)) {
      StorageTx tx = UnitOfWork.currentTx();
      Bucket bucket = tx.findBucket(getRepository());
      Asset asset = tx.findAssetWithProperty(P_NAME, path, bucket);
      if (asset == null) {
        asset = tx.createAsset(bucket, getRepository().getFormat()).name(path);
      }

      AttributesMap contentAttributes = null;
      if (content instanceof Content) {
        contentAttributes = ((Content) content).getAttributes();
      }
      Content.applyToAsset(asset, Content.maintainLastModified(asset, contentAttributes));
      AssetBlob blob = tx.setBlob(
          asset,
          path,
          tembBlob,
          FacetHelper.hashAlgorithms,
          null,
          content.getContentType(),
          false);
      tx.saveAsset(asset);
      return FacetHelper.toContent(asset, blob.getBlob());
    }
  }

  @Override
  @TransactionalDeleteBlob
  public boolean delete(String path) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());
    Asset asset = tx.findAssetWithProperty(P_NAME, path, bucket);
    if (asset == null) {
      return false;
    }

    tx.deleteAsset(asset);
    return true;
  }
}

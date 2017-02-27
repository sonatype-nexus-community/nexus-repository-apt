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

import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_BUCKET;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import javax.inject.Named;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.http.client.utils.DateUtils;
import org.bouncycastle.openpgp.PGPException;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.orient.entity.AttachedEntityHelper;
import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.IllegalOperationException;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.orientechnologies.orient.core.record.impl.ODocument;

import net.staticsnow.nexus.repository.apt.AptFacet;
import net.staticsnow.nexus.repository.apt.internal.AptMimeTypes;
import net.staticsnow.nexus.repository.apt.internal.FacetHelper;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile.Paragraph;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFileParser;
import net.staticsnow.nexus.repository.apt.internal.debian.Version;
import net.staticsnow.nexus.repository.apt.internal.gpg.AptSigningFacet;

@Named
@Facet.Exposed
public class AptHostedFacet
    extends FacetSupport
{
  private static final String P_INDEX_SECTION = "index_section";
  private static final String P_ARCHITECTURE = "architecture";
  private static final String P_PACKAGE_NAME = "package_name";
  private static final String P_PACKAGE_VERSION = "package_version";

  private static final String SELECT_HOSTED_ASSETS = 
      "SELECT " +
      "name, " +
      "attributes.apt.index_section AS index_section, " +
      "attributes.apt.architecture AS architecture " +
      "FROM asset " +
      "WHERE bucket=:bucket " +
      "AND attributes.apt.asset_kind=:asset_kind";

  private static final String ASSETS_BY_PACKAGE_AND_ARCH = 
      "attributes.apt.asset_kind=:asset_kind " +
      "AND attributes.apt.package_name=:package_name " +
      "AND attributes.apt.architecture=:architecture";

  static final String CONFIG_KEY = "aptHosted";

  static class Config
  {
    public Integer assetHistoryLimit;
  }

  private Config config;

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
  }

  @Override
  protected void doDestroy() throws Exception {
    config = null;
  }

  @TransactionalStoreBlob
  public void ingestAsset(Payload body) throws IOException, PGPException {
    AptFacet aptFacet = getRepository().facet(AptFacet.class);
    StorageFacet storageFacet = facet(StorageFacet.class);
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    ControlFile control = null;
    try (TempBlob tempBlob = storageFacet.createTempBlob(body, FacetHelper.hashAlgorithms);
         ArArchiveInputStream is = new ArArchiveInputStream(tempBlob.get())) {
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

      if (control == null) {
        throw new IllegalOperationException("Invalid Debian package supplied");
      }

      String name = control.getField("Package").map(f -> f.value).get();
      String version = control.getField("Version").map(f -> f.value).get();
      String architecture = control.getField("Architecture").map(f -> f.value).get();

      String assetName = name + "_" + version + "_" + architecture + ".deb";
      String assetPath = "pool/" + name.substring(0, 1) + "/" + name + "/" + assetName;

      Content content = aptFacet.put(assetPath, new StreamPayload(() -> tempBlob.get(), body.getSize(), body.getContentType()));
      Asset asset = Content.findAsset(tx, bucket, content);
      String indexSection = buildIndexSection(control, asset.size(), asset.getChecksums(FacetHelper.hashAlgorithms), assetPath);
      asset.formatAttributes().set(P_ARCHITECTURE, architecture);
      asset.formatAttributes().set(P_PACKAGE_NAME, name);
      asset.formatAttributes().set(P_PACKAGE_VERSION, version);
      asset.formatAttributes().set(P_INDEX_SECTION, indexSection);
      asset.formatAttributes().set(P_ASSET_KIND, "DEB");
      tx.saveAsset(asset);

      List<AssetChange> changes = new ArrayList<>();
      changes.add(new AssetChange(AssetAction.ADDED, asset));

      for (Asset removed : selectOldPackagesToRemove(name, architecture)) {
        tx.deleteAsset(removed);
        changes.add(new AssetChange(AssetAction.REMOVED, removed));
      }

      rebuildIndexesInTransaction(tx, changes.stream().toArray(AssetChange[]::new));
    }
  }

  public void rebuildIndexes(AssetChange... changes) throws IOException, PGPException {
    StorageTx tx = UnitOfWork.currentTx();
    rebuildIndexesInTransaction(tx, changes);
  }

  @TransactionalStoreMetadata
  private void rebuildIndexesInTransaction(StorageTx tx, AssetChange... changes) throws IOException, PGPException {
    AptFacet aptFacet = getRepository().facet(AptFacet.class);
    AptSigningFacet signingFacet = getRepository().facet(AptSigningFacet.class);
    Bucket bucket = tx.findBucket(getRepository());

    StringBuilder sha256Builder = new StringBuilder();
    StringBuilder md5Builder = new StringBuilder();
    String releaseFile;
    try (CompressingTempFileStore store = buildPackageIndexes(tx, bucket, changes)) {
      for (Map.Entry<String, CompressingTempFileStore.FileMetadata> entry : store.getFiles().entrySet()) {
        Content plainContent = aptFacet.put(
            packageIndexName(entry.getKey(), ""),
            new StreamPayload(entry.getValue().plainSupplier(), entry.getValue().plainSize(), AptMimeTypes.TEXT));
        addSignatureItem(md5Builder, MD5, plainContent, packageRelativeIndexName(entry.getKey(), ""));
        addSignatureItem(sha256Builder, SHA256, plainContent, packageRelativeIndexName(entry.getKey(), ""));

        Content gzContent = aptFacet.put(
            packageIndexName(entry.getKey(), ".gz"),
            new StreamPayload(entry.getValue().gzSupplier(), entry.getValue().bzSize(), AptMimeTypes.GZIP));
        addSignatureItem(md5Builder, MD5, gzContent, packageRelativeIndexName(entry.getKey(), ".gz"));
        addSignatureItem(sha256Builder, SHA256, gzContent, packageRelativeIndexName(entry.getKey(), ".gz"));

        Content bzContent = aptFacet.put(
            packageIndexName(entry.getKey(), ".bz2"),
            new StreamPayload(entry.getValue().bzSupplier(), entry.getValue().bzSize(), AptMimeTypes.BZIP));
        addSignatureItem(md5Builder, MD5, bzContent, packageRelativeIndexName(entry.getKey(), ".bz2"));
        addSignatureItem(sha256Builder, SHA256, bzContent, packageRelativeIndexName(entry.getKey(), ".bz2"));
      }

      releaseFile = buildReleaseFile(aptFacet.getDistribution(), store.getFiles().keySet(), md5Builder.toString(), sha256Builder.toString());
    }

    aptFacet.put(releaseIndexName("Release"), new BytesPayload(releaseFile.getBytes(Charsets.UTF_8), AptMimeTypes.TEXT));
    byte[] inRelease = signingFacet.signInline(releaseFile);
    aptFacet.put(releaseIndexName("InRelease"), new BytesPayload(inRelease, AptMimeTypes.TEXT));
    byte[] releaseGpg = signingFacet.signExternal(releaseFile);
    aptFacet.put(releaseIndexName("Release.gpg"), new BytesPayload(releaseGpg, AptMimeTypes.SIGNATURE));
  }

  private String buildReleaseFile(String distribution, Collection<String> architectures, String md5, String sha256) {
    Paragraph p = new Paragraph(Arrays.asList(
        new ControlFile.ControlField("Suite", distribution),
        new ControlFile.ControlField("Codename", distribution), new ControlFile.ControlField("Components", "main"),
        new ControlFile.ControlField("Date", DateUtils.formatDate(new Date())),
        new ControlFile.ControlField("Architectures", architectures.stream().collect(Collectors.joining(" "))),
        new ControlFile.ControlField("SHA256", sha256), new ControlFile.ControlField("MD5Sum", md5)));
    return p.toString();
  }

  private CompressingTempFileStore buildPackageIndexes(StorageTx tx, Bucket bucket, AssetChange... changes)
      throws IOException
  {
    CompressingTempFileStore result = new CompressingTempFileStore();
    Map<String, Writer> streams = new HashMap<>();
    boolean ok = false;
    try {
      Map<String, Object> sqlParams = new HashMap<>();
      sqlParams.put(P_BUCKET, AttachedEntityHelper.id(bucket));
      sqlParams.put(P_ASSET_KIND, "DEB");

      Set<String> excludeNames = Arrays.stream(changes)
          .filter(change -> change.action == AssetAction.REMOVED)
          .map(change -> change.asset.name()).collect(Collectors.toSet());

      for (ODocument d : tx.browse(SELECT_HOSTED_ASSETS, sqlParams)) {
        String name = d.<String> field(P_NAME, String.class);
        if (excludeNames.contains(name)) {
          continue;
        }
        String arch = d.<String> field(P_ARCHITECTURE, String.class);
        String indexSection = d.<String> field(P_INDEX_SECTION, String.class);
        Writer out = streams.computeIfAbsent(arch, a -> result.openOutput(a));
        out.write(indexSection);
        out.write("\n\n");
      }

      List<Asset> addAssets = Arrays.stream(changes).filter(change -> change.action == AssetAction.ADDED)
          .map(change -> change.asset).collect(Collectors.toList());

      // HACK: tx.browse won't see changes in the current transaction, so we have to manually add these in here
      for (Asset asset : addAssets) {
        String arch = asset.formatAttributes().get(P_ARCHITECTURE, String.class);
        String indexSection = asset.formatAttributes().get(P_INDEX_SECTION, String.class);
        Writer out = streams.computeIfAbsent(arch, a -> result.openOutput(a));
        out.write(indexSection);
        out.write("\n\n");
      }
      ok = true;
    }
    finally {
      for (Writer w : streams.values()) {
        IOUtils.closeQuietly(w);
      }

      if (!ok) {
        result.close();
      }
    }
    return result;
  }

  private String buildIndexSection(ControlFile cf, long size, Map<HashAlgorithm, HashCode> hashes, String assetPath) {
    Paragraph modified = cf.getParagraphs().get(0)
        .withFields(Arrays.asList(
            new ControlFile.ControlField("Filename", assetPath),
            new ControlFile.ControlField("Size", Long.toString(size)),
            new ControlFile.ControlField("MD5Sum", hashes.get(MD5).toString()),
            new ControlFile.ControlField("SHA1", hashes.get(SHA1).toString()),
            new ControlFile.ControlField("SHA256", hashes.get(SHA256).toString())));
    return modified.toString();
  }

  private List<Asset> selectOldPackagesToRemove(String packageName, String arch) throws IOException, PGPException {
    if (config.assetHistoryLimit == null) {
      return Collections.emptyList();
    }
    int count = config.assetHistoryLimit;
    StorageTx tx = UnitOfWork.currentTx();
    Map<String, Object> sqlParams = new HashMap<>();
    sqlParams.put(P_PACKAGE_NAME, packageName);
    sqlParams.put(P_ARCHITECTURE, arch);
    sqlParams.put(P_ASSET_KIND, "DEB");
    Iterable<Asset> assets = tx.findAssets(ASSETS_BY_PACKAGE_AND_ARCH, sqlParams,
        Collections.singleton(getRepository()), "");
    List<Asset> removals = new ArrayList<>();
    Map<String, List<Asset>> assetsByArch = StreamSupport.stream(assets.spliterator(), false)
        .collect(Collectors.groupingBy(a -> a.formatAttributes().get(P_ARCHITECTURE, String.class)));
    for (Map.Entry<String, List<Asset>> entry : assetsByArch.entrySet()) {
      if (entry.getValue().size() <= count) {
        continue;
      }

      int trimCount = entry.getValue().size() - count;
      Set<String> keepVersions = entry.getValue().stream()
          .map(a -> new Version(a.formatAttributes().get(P_PACKAGE_VERSION, String.class))).sorted().skip(trimCount)
          .map(v -> v.toString()).collect(Collectors.toSet());

      entry.getValue().stream()
          .filter(a -> !keepVersions.contains(a.formatAttributes().get(P_PACKAGE_VERSION, String.class)))
          .forEach((item) -> removals.add(item));
    }

    return removals;
  }

  private String releaseIndexName(String name) {
    AptFacet aptFacet = getRepository().facet(AptFacet.class);
    String dist = aptFacet.getDistribution();
    return "dists/" + dist + "/" + name;
  }

  private String packageIndexName(String arch, String ext) {
    AptFacet aptFacet = getRepository().facet(AptFacet.class);
    String dist = aptFacet.getDistribution();
    return "dists/" + dist + "/main/binary-" + arch + "/Packages" + ext;
  }

  private String packageRelativeIndexName(String arch, String ext) {
    return "main/binary-" + arch + "/Packages" + ext;
  }

  private void addSignatureItem(StringBuilder builder, HashAlgorithm algo, Content content, String filename) {
    Map<HashAlgorithm, HashCode> hashMap = content.getAttributes().get(Content.CONTENT_HASH_CODES_MAP,
        Content.T_CONTENT_HASH_CODES_MAP);
    builder.append("\n ");
    builder.append(hashMap.get(algo).toString());
    builder.append(" ");
    builder.append(content.getSize());
    builder.append(" ");
    builder.append(filename);
  }

  public static enum AssetAction
  {
    ADDED, REMOVED
  }

  public static class AssetChange
  {
    public final AssetAction action;
    public final Asset asset;

    public AssetChange(AssetAction action, Asset asset) {
      super();
      this.action = action;
      this.asset = asset;
    }
  }
}

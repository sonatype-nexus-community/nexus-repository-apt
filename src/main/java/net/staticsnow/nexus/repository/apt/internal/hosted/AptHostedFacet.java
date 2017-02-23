package net.staticsnow.nexus.repository.apt.internal.hosted;

import java.io.IOException;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

import net.staticsnow.nexus.repository.apt.internal.hosted.AptHostedFacetImpl.AssetChange;
import org.bouncycastle.openpgp.PGPException;

/**
 * Persistence for Apt hosted
 */
@Facet.Exposed
public interface AptHostedFacet
  extends Facet
{
  Content doGet(String path);

  void rebuildIndexes(AssetChange... changes) throws IOException, PGPException;

  void ingestAsset(Payload payload) throws IOException, PGPException;
}

package net.staticsnow.nexus.repository.apt.internal.hosted;

import java.io.IOException;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

/**
 * Persistence for Apt hosted
 */
@Facet.Exposed
public interface AptHostedFacet
  extends Facet
{
  Content doGet(String path);

  void handle(String path, Payload payload) throws IOException;
}

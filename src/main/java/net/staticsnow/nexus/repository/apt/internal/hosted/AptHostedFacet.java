package net.staticsnow.nexus.repository.apt.internal.hosted;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Content;

/**
 * Persistence for Apt hosted
 */
@Facet.Exposed
public interface AptHostedFacet
  extends Facet
{
  Content doGet(String path);
}

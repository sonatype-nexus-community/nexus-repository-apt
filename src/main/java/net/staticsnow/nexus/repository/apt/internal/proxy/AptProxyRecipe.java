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

package net.staticsnow.nexus.repository.apt.internal.proxy;

import static org.sonatype.nexus.repository.http.HttpHandlers.notFound;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.RecipeSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.attributes.AttributesFacet;
import org.sonatype.nexus.repository.cache.NegativeCacheFacet;
import org.sonatype.nexus.repository.cache.NegativeCacheHandler;
import org.sonatype.nexus.repository.http.PartialFetchHandler;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.proxy.ProxyHandler;
import org.sonatype.nexus.repository.purge.PurgeUnusedFacet;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.security.SecurityHandler;
import org.sonatype.nexus.repository.storage.SingleAssetComponentMaintenance;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.nexus.repository.view.ConfigurableViewFacet;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler;
import org.sonatype.nexus.repository.view.handlers.ContentHeadersHandler;
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.AlwaysMatcher;

import net.staticsnow.nexus.repository.apt.internal.AptFacetImpl;
import net.staticsnow.nexus.repository.apt.internal.AptFormat;
import net.staticsnow.nexus.repository.apt.internal.AptSecurityFacet;
import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotHandler;

@Named(AptProxyRecipe.NAME)
@Singleton
public class AptProxyRecipe
    extends RecipeSupport
{

  public static final String NAME = "apt-proxy";

  @Inject
  Provider<AptSecurityFacet> securityFacet;

  @Inject
  Provider<ConfigurableViewFacet> viewFacet;

  @Inject
  Provider<HttpClientFacet> httpClientFacet;

  @Inject
  Provider<NegativeCacheFacet> negativeCacheFacet;

  @Inject
  Provider<AptProxyFacet> proxyFacet;

  @Inject
  Provider<AptProxySnapshotFacet> proxySnapshotFacet;

  @Inject
  Provider<AptFacetImpl> aptFacet;

  @Inject
  Provider<StorageFacet> storageFacet;

  @Inject
  Provider<AttributesFacet> attributesFacet;

  @Inject
  Provider<SingleAssetComponentMaintenance> componentMaintenance;

  @Inject
  Provider<SearchFacet> searchFacet;

  @Inject
  Provider<PurgeUnusedFacet> purgeUnusedFacet;

  @Inject
  ExceptionHandler exceptionHandler;

  @Inject
  TimingHandler timingHandler;

  @Inject
  SecurityHandler securityHandler;

  @Inject
  NegativeCacheHandler negativeCacheHandler;

  @Inject
  PartialFetchHandler partialFetchHandler;

  @Inject
  UnitOfWorkHandler unitOfWorkHandler;

  @Inject
  ProxyHandler proxyHandler;

  @Inject
  ConditionalRequestHandler conditionalRequestHandler;

  @Inject
  ContentHeadersHandler contentHeadersHandler;

  @Inject
  AptSnapshotHandler snapshotHandler;

  @Inject
  public AptProxyRecipe(@Named(ProxyType.NAME) Type type, @Named(AptFormat.NAME) Format format) {
    super(type, format);
  }

  @Override
  public void apply(Repository repository) throws Exception {
    repository.attach(securityFacet.get());
    repository.attach(configure(viewFacet.get()));
    repository.attach(httpClientFacet.get());
    repository.attach(negativeCacheFacet.get());
    repository.attach(proxyFacet.get());
    repository.attach(proxySnapshotFacet.get());
    repository.attach(aptFacet.get());
    repository.attach(storageFacet.get());
    repository.attach(attributesFacet.get());
    repository.attach(componentMaintenance.get());
    repository.attach(searchFacet.get());
    repository.attach(purgeUnusedFacet.get());
  }

  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder();

    builder.route(new Route.Builder().matcher(new AlwaysMatcher())
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(negativeCacheHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(snapshotHandler)
        .handler(proxyHandler).create());

    builder.defaultHandlers(notFound());
    facet.configure(builder.create());
    return facet;
  }
}

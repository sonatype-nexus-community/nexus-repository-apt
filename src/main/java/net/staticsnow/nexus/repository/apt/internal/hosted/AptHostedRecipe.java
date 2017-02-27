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
import org.sonatype.nexus.repository.http.PartialFetchHandler;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.security.SecurityHandler;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler;
import org.sonatype.nexus.repository.types.HostedType;
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
import net.staticsnow.nexus.repository.apt.internal.gpg.AptSigningFacet;
import net.staticsnow.nexus.repository.apt.internal.gpg.AptSigningHandler;
import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotHandler;

@Named(AptHostedRecipe.NAME)
@Singleton
public class AptHostedRecipe
    extends RecipeSupport
{

  public static final String NAME = "apt-hosted";

  @Inject
  Provider<AptSecurityFacet> securityFacet;

  @Inject
  Provider<ConfigurableViewFacet> viewFacet;

  @Inject
  Provider<AptFacetImpl> aptFacet;

  @Inject
  Provider<AptHostedFacet> aptHostedFacet;

  @Inject
  Provider<AptSigningFacet> aptSigningFacet;

  @Inject
  Provider<AptHostedSnapshotFacet> snapshotFacet;

  @Inject
  Provider<StorageFacet> storageFacet;

  @Inject
  Provider<AttributesFacet> attributesFacet;

  @Inject
  Provider<AptHostedComponentMaintenanceFacet> componentMaintenance;

  @Inject
  Provider<SearchFacet> searchFacet;

  @Inject
  ExceptionHandler exceptionHandler;

  @Inject
  TimingHandler timingHandler;

  @Inject
  SecurityHandler securityHandler;

  @Inject
  PartialFetchHandler partialFetchHandler;

  @Inject
  UnitOfWorkHandler unitOfWorkHandler;

  @Inject
  AptHostedHandler hostedHandler;

  @Inject
  ConditionalRequestHandler conditionalRequestHandler;

  @Inject
  ContentHeadersHandler contentHeadersHandler;

  @Inject
  AptSnapshotHandler snapshotHandler;

  @Inject
  AptSigningHandler signingHandler;

  @Inject
  public AptHostedRecipe(@Named(HostedType.NAME) Type type, @Named(AptFormat.NAME) Format format) {
    super(type, format);
  }

  @Override
  public void apply(Repository repository) throws Exception {
    repository.attach(securityFacet.get());
    repository.attach(configure(viewFacet.get()));
    repository.attach(storageFacet.get());
    repository.attach(aptFacet.get());
    repository.attach(aptHostedFacet.get());
    repository.attach(aptSigningFacet.get());
    repository.attach(snapshotFacet.get());
    repository.attach(attributesFacet.get());
    repository.attach(componentMaintenance.get());
    repository.attach(searchFacet.get());
  }

  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder();

    builder.route(new Route.Builder().matcher(new AlwaysMatcher())
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(snapshotHandler)
        .handler(signingHandler)
        .handler(hostedHandler).create());

    builder.defaultHandlers(notFound());
    facet.configure(builder.create());
    return facet;
  }
}

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
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.view.ConfigurableViewFacet;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;

import net.staticsnow.nexus.repository.apt.internal.AptFormat;
import net.staticsnow.nexus.repository.apt.internal.AptRecipeSupport;
import net.staticsnow.nexus.repository.apt.internal.gpg.AptSigningFacet;
import net.staticsnow.nexus.repository.apt.internal.gpg.AptSigningHandler;

@Named(AptHostedRecipe.NAME)
@Singleton
class AptHostedRecipe
  extends AptRecipeSupport
{

  public static final String NAME = "apt-hosted";

  @Inject
  Provider<AptHostedFacetImpl> aptHostedFacet;

  @Inject
  Provider<AptSigningFacet> aptSigningFacet;

  @Inject
  Provider<AptHostedSnapshotFacet> snapshotFacet;

  @Inject
  AptHostedHandler hostedHandler;

  @Inject
  AptSigningHandler signingHandler;

  @Inject
  AptHostedRecipe(
      @Named(HostedType.NAME) Type type,
      @Named(AptFormat.NAME) Format format)
  {
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

    builder.route(assetsMatcher()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandler.get)
        .create());

    builder.route(signingMatcher()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(signingHandler.handle)
        .create());

    builder.route(snapshotMatcher()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(snapshotHandler.handleSnapshotFetchRequest)
        .handler(hostedHandler.get)
        .create());

    builder.route(createSnapshotMatcher()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(snapshotHandler.createSnapshot)
        .create());

    builder.route(snapshotCollectionMatcher()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(snapshotHandler.createSnapshotCollection)
        .create());

    builder.route(deleteSnapshotMatcher()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(snapshotHandler.deleteSnapshot)
        .create());

    builder.route(rebuildIndexMatcher()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandler.rebuildIndexes)
        .create());

    builder.route(otherMatcher()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandler.ingestAssset)
        .create());

    builder.defaultHandlers(notFound());

    facet.configure(builder.create());

    return facet;
  }
}

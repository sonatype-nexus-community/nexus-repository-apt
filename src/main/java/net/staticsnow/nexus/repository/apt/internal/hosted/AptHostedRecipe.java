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
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.matchers.AlwaysMatcher;

import net.staticsnow.nexus.repository.apt.internal.AptFormat;
import net.staticsnow.nexus.repository.apt.internal.AptRecipeSupport;
import net.staticsnow.nexus.repository.apt.internal.gpg.AptSigningFacet;
import net.staticsnow.nexus.repository.apt.internal.gpg.AptSigningHandler;

@Named(AptHostedRecipe.NAME)
@Singleton
public class AptHostedRecipe
    extends AptRecipeSupport
{

  public static final String NAME = "apt-hosted";

  @Inject
  Provider<AptHostedFacet> aptHostedFacet;

  @Inject
  Provider<AptSigningFacet> aptSigningFacet;

  @Inject
  Provider<AptHostedSnapshotFacet> snapshotFacet;

  @Inject
  AptHostedHandler hostedHandler;

  @Inject
  AptSigningHandler signingHandler;

  @Inject
  public AptHostedRecipe(
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

    builder.route(new Route.Builder()
        .matcher(new AlwaysMatcher())
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(snapshotHandler)
        .handler(signingHandler)
        .handler(hostedHandler)
        .create());

    builder.defaultHandlers(notFound());
    facet.configure(builder.create());
    return facet;
  }
}

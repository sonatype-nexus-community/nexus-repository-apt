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

import javax.inject.Inject;
import javax.inject.Provider;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.RecipeSupport;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.attributes.AttributesFacet;
import org.sonatype.nexus.repository.cache.NegativeCacheFacet;
import org.sonatype.nexus.repository.cache.NegativeCacheHandler;
import org.sonatype.nexus.repository.http.PartialFetchHandler;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.purge.PurgeUnusedFacet;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.security.SecurityHandler;
import org.sonatype.nexus.repository.storage.SingleAssetComponentMaintenance;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler;
import org.sonatype.nexus.repository.view.ConfigurableViewFacet;
import org.sonatype.nexus.repository.view.Route.Builder;
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler;
import org.sonatype.nexus.repository.view.handlers.ContentHeadersHandler;
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.ActionMatcher;
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotHandler;

import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD;
import static org.sonatype.nexus.repository.http.HttpMethods.POST;

public abstract class AptRecipeSupport
  extends RecipeSupport
{
  @Inject
  protected Provider<AptSecurityFacet> securityFacet;

  @Inject
  protected Provider<ConfigurableViewFacet> viewFacet;

  @Inject
  protected Provider<HttpClientFacet> httpClientFacet;

  @Inject
  protected Provider<NegativeCacheFacet> negativeCacheFacet;

  @Inject
  protected Provider<AptFacetImpl> aptFacet;

  @Inject
  protected Provider<StorageFacet> storageFacet;

  @Inject
  protected Provider<AttributesFacet> attributesFacet;

  @Inject
  protected Provider<SingleAssetComponentMaintenance> componentMaintenance;

  @Inject
  protected Provider<SearchFacet> searchFacet;

  @Inject
  protected Provider<PurgeUnusedFacet> purgeUnusedFacet;

  @Inject
  protected ExceptionHandler exceptionHandler;

  @Inject
  protected TimingHandler timingHandler;

  @Inject
  protected SecurityHandler securityHandler;

  @Inject
  protected NegativeCacheHandler negativeCacheHandler;

  @Inject
  protected PartialFetchHandler partialFetchHandler;

  @Inject
  protected UnitOfWorkHandler unitOfWorkHandler;

  @Inject
  protected ConditionalRequestHandler conditionalRequestHandler;

  @Inject
  protected ContentHeadersHandler contentHeadersHandler;

  @Inject
  protected AptSnapshotHandler snapshotHandler;

  protected AptRecipeSupport(final Type type, final Format format) {
    super(type, format);
  }

  protected static Builder assetsMatcher() {
    return new Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET, HEAD),
            new TokenMatcher("/{path:.+}")
        ));
  }

  protected static Builder otherMatcher() {
    return new Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(POST),
            new TokenMatcher("/{path:.+}")
        ));
  }
}

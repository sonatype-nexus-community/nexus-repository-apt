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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import net.staticsnow.nexus.repository.apt.AptFacet;
import net.staticsnow.nexus.repository.apt.internal.AptPathUtils;

@Named
@Singleton
public class AptHostedHandler
  extends ComponentSupport
{
  // Some different approaches to handlers
  final Handler ingestAssset = context -> {
    context.getRepository().facet(AptHostedFacet.class).ingestAsset(context.getRequest().getPayload());
    return HttpResponses.created();
  };

  final Handler rebuildIndexes = context -> {
    context.getRepository().facet(AptHostedFacet.class).rebuildIndexes();
    return HttpResponses.ok();
  };

  final Handler get = context -> {
    TokenMatcher.State matcherState = AptPathUtils.matcherState(context);
    String path = AptPathUtils.assetPath(matcherState);

    Content content = context.getRepository().facet(AptFacet.class).get(path);
    if (content == null) {
      return HttpResponses.notFound(path);
    }
    return HttpResponses.ok(content);
  };
}

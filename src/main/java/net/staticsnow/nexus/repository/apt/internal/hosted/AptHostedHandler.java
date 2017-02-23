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
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;

import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotHandler;

import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD;

@Named
@Singleton
public class AptHostedHandler
  extends ComponentSupport
{
  // Some different approaches to handlers
  final Handler handle = context -> {
    String path = assetPath(context);

    if (path.equals("rebuild-indexes")) {
      context.getRepository().facet(AptHostedFacet.class).rebuildIndexes();
      return HttpResponses.ok();
    }
    else if (path.equals("")) {
      context.getRepository().facet(AptHostedFacet.class).ingestAsset(context.getRequest().getPayload());
      return HttpResponses.created();
    }
    else {
      return HttpResponses.methodNotAllowed(GET, HEAD);
    }
  };

  final Handler doGet = context -> {
    String path = assetPath(context);
    Content content = context.getRepository().facet(AptHostedFacet.class).doGet(path);
    if (content == null) {
      return HttpResponses.notFound(path);
    }
    return HttpResponses.ok(content);
  };

  private String assetPath(Context context) {
    final AptSnapshotHandler.State snapshotState = context.getAttributes().require(AptSnapshotHandler.State.class);
    return snapshotState.assetPath;
  }
}

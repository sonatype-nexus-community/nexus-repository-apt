/*
 * Nexus APT plugin.
 * 
 * Copyright (c) 2016-Present Michael Poindexter.
 * 
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package net.staticsnow.nexus.repository.apt.internal.hosted;

import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD;
import static org.sonatype.nexus.repository.http.HttpMethods.POST;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;

import net.staticsnow.nexus.repository.apt.AptFacet;
import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotHandler;

@Named
@Singleton
public class AptHostedHandler
    extends ComponentSupport
    implements Handler
{
  @Override
  public Response handle(Context context) throws Exception {
    String path = assetPath(context);
    String method = context.getRequest().getAction();

    AptFacet aptFacet = context.getRepository().facet(AptFacet.class);
    AptHostedFacet hostedFacet = context.getRepository().facet(AptHostedFacet.class);

    switch (method) {
      case GET:
      case HEAD: {
        return doGet(path, aptFacet);
      }

      case POST: {
        if (path.equals("rebuild-indexes")) {
          hostedFacet.rebuildIndexes();
          return HttpResponses.ok();
        }
        else if (path.equals("")) {
          hostedFacet.ingestAsset(context.getRequest().getPayload());
          return HttpResponses.created();
        }
        else {
          return HttpResponses.methodNotAllowed(method, GET, HEAD);
        }
      }

      default:
        return HttpResponses.methodNotAllowed(method, GET, HEAD, POST);
    }
  }

  private Response doGet(String path, AptFacet aptFacet) throws IOException {
    Content content = aptFacet.get(path);
    if (content == null) {
      return HttpResponses.notFound(path);
    }
    return HttpResponses.ok(content);
  }

  private String assetPath(Context context) {
    final AptSnapshotHandler.State snapshotState = context.getAttributes().require(AptSnapshotHandler.State.class);
    return snapshotState.assetPath;
  }
}

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

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

package net.staticsnow.nexus.repository.apt.internal.gpg;

import static org.sonatype.nexus.repository.http.HttpMethods.GET;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;

import net.staticsnow.nexus.repository.apt.internal.snapshot.AptSnapshotHandler;

@Named
@Singleton
public class AptSigningHandler
    extends ComponentSupport
    implements Handler
{
  @Override
  public Response handle(Context context) throws Exception {
    String path = assetPath(context);
    String method = context.getRequest().getAction();
    AptSigningFacet facet = context.getRepository().facet(AptSigningFacet.class);

    if ("repository-key.gpg".equals(path) && GET.equals(method)) {
      return HttpResponses.ok(facet.getPublicKey());
    }

    return context.proceed();
  }

  private String assetPath(Context context) {
    final AptSnapshotHandler.State snapshotState = context.getAttributes().require(AptSnapshotHandler.State.class);
    return snapshotState.assetPath;
  }
}

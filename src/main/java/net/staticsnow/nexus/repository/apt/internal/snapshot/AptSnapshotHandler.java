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

package net.staticsnow.nexus.repository.apt.internal.snapshot;

import java.io.InputStream;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import net.staticsnow.nexus.repository.apt.internal.AptPathUtils;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFileParser;

@Named
@Singleton
public class AptSnapshotHandler
    extends ComponentSupport
{
  public final Handler handleSnapshotFetchRequest = context -> {
    TokenMatcher.State matcherState = AptPathUtils.matcherState(context);
    String id = AptPathUtils.match(matcherState, "id");
    String path = AptPathUtils.match(matcherState, "path");

    Repository repository = context.getRepository();
    AptSnapshotFacet snapshotFacet = repository.facet(AptSnapshotFacet.class);
    if (snapshotFacet.isSnapshotableFile(path)) {
      Content content = snapshotFacet.getSnapshotFile(id, path);
      return content == null ? HttpResponses.notFound() : HttpResponses.ok(content);
    }

    return context.proceed();
  };

  public final Handler createSnapshotCollection = context -> {
    TokenMatcher.State matcherState = AptPathUtils.matcherState(context);
    String id = AptPathUtils.match(matcherState, "id");

    AptSnapshotFacet snapshotFacet = context.getRepository().facet(AptSnapshotFacet.class);
    snapshotFacet.createSnapshot(id, new AllSnapshotComponentSelector());
    return HttpResponses.created();
  };

  public final Handler createSnapshot = context -> {
    TokenMatcher.State matcherState = AptPathUtils.matcherState(context);
    String id = AptPathUtils.match(matcherState, "id");
    AptSnapshotFacet snapshotFacet = context.getRepository().facet(AptSnapshotFacet.class);

    try (InputStream is = context.getRequest().getPayload().openInputStream()) {
      ControlFile settings = new ControlFileParser().parseControlFile(is);
      snapshotFacet.createSnapshot(id, new FilteredSnapshotComponentSelector(settings));
    }

    return HttpResponses.created();
  };

  public final Handler deleteSnapshot = context -> {
    TokenMatcher.State matcherState = AptPathUtils.matcherState(context);
    String id = AptPathUtils.match(matcherState, "id");
    AptSnapshotFacet snapshotFacet = context.getRepository().facet(AptSnapshotFacet.class);

    snapshotFacet.deleteSnapshot(id);
    return HttpResponses.noContent();
  };
}

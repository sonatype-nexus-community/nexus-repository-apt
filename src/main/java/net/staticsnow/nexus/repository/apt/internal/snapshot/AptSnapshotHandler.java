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

import static org.sonatype.nexus.repository.http.HttpMethods.DELETE;
import static org.sonatype.nexus.repository.http.HttpMethods.MKCOL;
import static org.sonatype.nexus.repository.http.HttpMethods.PUT;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;

import net.staticsnow.nexus.repository.apt.internal.debian.ControlFile;
import net.staticsnow.nexus.repository.apt.internal.debian.ControlFileParser;

@Named
@Singleton
public class AptSnapshotHandler
    extends ComponentSupport
    implements Handler
{
  private static final Pattern SNAPSHOT_PATH_PATTERN = Pattern.compile("/snapshots/([^/]+)/?(.*)");

  public static final class State
  {
    public final String assetPath;

    public State(String assetPath) {
      super();
      this.assetPath = assetPath;
    }
  }

  @Override
  public Response handle(Context context) throws Exception {
    String path = context.getRequest().getPath();
    Matcher matcher = SNAPSHOT_PATH_PATTERN.matcher(path);
    if (!matcher.matches()) {
      context.getAttributes().set(AptSnapshotHandler.State.class, new AptSnapshotHandler.State(path.substring(1)));
      return context.proceed();
    }

    String id = matcher.group(1);
    path = matcher.group(2);

    if (path.length() == 0) {
      return handleSnapshotAdminRequest(context, id);
    }
    else {
      return handleSnapshotFetchRequest(context, id, path);
    }
  }

  private Response handleSnapshotAdminRequest(Context context, String id) throws Exception {
    String method = context.getRequest().getAction();
    Repository repository = context.getRepository();
    AptSnapshotFacet snapshotFacet = repository.facet(AptSnapshotFacet.class);

    switch (method) {
      case MKCOL: {
        snapshotFacet.createSnapshot(id, new AllSnapshotComponentSelector());
        return HttpResponses.created();
      }

      case PUT: {
        try (InputStream is = context.getRequest().getPayload().openInputStream()) {
          ControlFile settings = new ControlFileParser().parseControlFile(is);
          snapshotFacet.createSnapshot(id, new FilteredSnapshotComponentSelector(settings));
        }
        return HttpResponses.created();
      }

      case DELETE: {
        snapshotFacet.deleteSnapshot(id);
        return HttpResponses.noContent();
      }

      default:
        return HttpResponses.methodNotAllowed(method, DELETE, MKCOL, PUT);
    }
  }

  private Response handleSnapshotFetchRequest(Context context, String id, String path) throws Exception {
    Repository repository = context.getRepository();
    AptSnapshotFacet snapshotFacet = repository.facet(AptSnapshotFacet.class);
    if (snapshotFacet.isSnapshotableFile(path)) {
      Content content = snapshotFacet.getSnapshotFile(id, path);
      return content == null ? HttpResponses.notFound() : HttpResponses.ok(content);
    }
    context.getAttributes().set(AptSnapshotHandler.State.class, new AptSnapshotHandler.State(path));
    return context.proceed();
  }
}

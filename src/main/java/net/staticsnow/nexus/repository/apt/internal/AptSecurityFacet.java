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

package net.staticsnow.nexus.repository.apt.internal;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.security.ContentPermissionChecker;
import org.sonatype.nexus.repository.security.SecurityFacetSupport;
import org.sonatype.nexus.repository.security.VariableResolverAdapter;

@Named
public class AptSecurityFacet
    extends SecurityFacetSupport
{
  @Inject
  public AptSecurityFacet(
                          AptFormatSecurityContributor securityResource,
                          @Named("simple") final VariableResolverAdapter variableResolverAdapter,
                          final ContentPermissionChecker contentPermissionChecker) {
    super(securityResource, variableResolverAdapter, contentPermissionChecker);
  }
}

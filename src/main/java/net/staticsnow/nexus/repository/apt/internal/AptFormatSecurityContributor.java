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
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.security.RepositoryFormatSecurityContributor;

@Named
@Singleton
public class AptFormatSecurityContributor
    extends RepositoryFormatSecurityContributor
{
  @Inject
  public AptFormatSecurityContributor(@Named(AptFormat.NAME) final Format format) {
    super(format);
  }
}

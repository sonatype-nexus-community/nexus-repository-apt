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

public class AptMimeTypes
{
  public static final String TEXT = "text/plain";
  public static final String GZIP = "application/gzip";
  public static final String BZIP = "application/bzip2";
  public static final String SIGNATURE = "application/pgp-signature";
  public static final String PUBLICKEY = "application/pgp";
  public static final String PACKAGE = "application/vnd.debian.binary-package";
}

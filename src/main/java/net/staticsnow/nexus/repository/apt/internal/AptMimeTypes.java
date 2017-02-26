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

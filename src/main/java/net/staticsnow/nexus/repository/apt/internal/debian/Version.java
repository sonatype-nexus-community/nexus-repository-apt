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

package net.staticsnow.nexus.repository.apt.internal.debian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version
    implements Comparable<Version>
{
  private static final Pattern VERSION_PART = Pattern.compile("(\\D*)(\\d*)");
  private int epoch = 0;
  private String debianRevision = "";
  private String upstreamVersion;

  public Version(String s) {
    int colonIdx = s.indexOf(':');
    if (colonIdx > -1) {
      epoch = Integer.parseInt(s.substring(0, colonIdx));
      s = s.substring(colonIdx + 1);
    }

    int lastDashIdx = s.lastIndexOf('-');
    if (lastDashIdx > -1) {
      debianRevision = s.substring(lastDashIdx + 1);
      s = s.substring(0, lastDashIdx);
    }

    upstreamVersion = s;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (epoch > 0) {
      sb.append(epoch);
      sb.append(":");
    }
    sb.append(upstreamVersion);
    if (debianRevision.length() > 0) {
      sb.append("-");
      sb.append(debianRevision);
    }
    return sb.toString();
  }

  @Override
  public int compareTo(Version o) {
    if (this.epoch < o.epoch) {
      return -1;
    }
    else if (this.epoch > o.epoch) {
      return 1;
    }
    else {
      int uv = compareDebianVersion(this.upstreamVersion, o.upstreamVersion);
      if (uv != 0) {
        return uv;
      }

      return compareDebianVersion(this.debianRevision, o.debianRevision);
    }
  }

  private static int compareDebianVersion(String a, String b) {
    Matcher ma = VERSION_PART.matcher(a);
    Matcher mb = VERSION_PART.matcher(b);

    String na = "";
    String nna = "";
    String nb = "";
    String nnb = "";
    do {
      if (ma.find()) {
        nna = ma.group(1);
        na = ma.group(2);
      }
      else {
        nna = "";
        na = "";
      }
      if (mb.find()) {
        nnb = mb.group(1);
        nb = mb.group(2);
      }
      else {
        nnb = "";
        nb = "";
      }

      int nn = compareNonNumeric(nna, nnb);
      if (nn != 0) {
        return nn;
      }

      int n = compareNumeric(na, nb);
      if (n != 0) {
        return n;
      }
    }
    while (na.length() > 0 || nna.length() > 0 || nb.length() > 0 || nnb.length() > 0);

    return 0;
  }

  private static int compareNonNumeric(String a, String b) {
    int len = Math.max(a.length(), b.length());
    for (int i = 0; i < len; i++) {
      int ac;
      int bc;
      if (i >= a.length()) {
        ac = -1;
      }
      else {
        ac = a.codePointAt(i);
      }
      if (i >= b.length()) {
        bc = -1;
      }
      else {
        bc = b.codePointAt(i);
      }

      if (priorityClass(ac) < priorityClass(bc)) {
        return -1;
      }
      else if (priorityClass(ac) > priorityClass(bc)) {
        return 1;
      }
      else if (ac < bc) {
        return -1;
      }
      else if (ac > bc) {
        return -1;
      }
    }

    return 0;
  }

  private static int compareNumeric(String a, String b) {
    if (a.isEmpty() && !b.isEmpty()) {
      return -1;
    }
    else if (b.isEmpty() && !a.isEmpty()) {
      return 1;
    }
    else if (a.isEmpty() && b.isEmpty()) {
      return 0;
    }

    return Long.compare(Long.parseLong(a), Long.parseLong(b));
  }

  private static int priorityClass(int c) {
    return c == '~' ? -2 : c == -1 ? -1 : Character.isLetter(c) ? 0 : 1;
  }
}

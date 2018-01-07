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

Ext.define('NX.aptui.app.PluginConfig', {
  '@aggregate_priority': 100,

  requires: [
    'NX.aptui.app.PluginStrings',
    'NX.aptui.util.AptRepositoryUrls'
  ]
});

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

Ext.define('NX.aptui.app.PluginConfig', {
  '@aggregate_priority': 100,

  requires: [
    'NX.aptui.app.PluginStrings',
    'NX.aptui.util.AptRepositoryUrls'
  ]
});

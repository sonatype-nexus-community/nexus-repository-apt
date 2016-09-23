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

Ext.define('NX.aptui.util.AptRepositoryUrls', {
  '@aggregate_priority': 90,

  singleton: true,
  requires: [
    'NX.coreui.util.RepositoryUrls',
    'NX.util.Url'
  ]
}, function(self) {
	NX.coreui.util.RepositoryUrls.addRepositoryUrlStrategy('apt', function (assetModel) {
      var repositoryName = assetModel.get('repositoryName'), assetName = assetModel.get('name');
      return NX.util.Url.asLink(NX.util.Url.baseUrl + '/repository/' + repositoryName + '/' + assetName, assetName);
    });
});

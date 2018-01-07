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

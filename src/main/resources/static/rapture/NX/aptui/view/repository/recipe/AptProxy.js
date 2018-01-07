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

/**
 * Repository settings form for an apt repository
 */
Ext.define('NX.aptui.view.repository.recipe.AptProxy', {
  extend: 'NX.coreui.view.repository.RepositorySettingsForm',
  alias: 'widget.nx-coreui-repository-apt-proxy',
  requires: [
    'NX.aptui.view.repository.facet.AptFacet',
    'NX.coreui.view.repository.facet.ProxyFacet',
    'NX.coreui.view.repository.facet.StorageFacet',
    'NX.coreui.view.repository.facet.HttpClientFacet',
    'NX.coreui.view.repository.facet.NegativeCacheFacet'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {xtype: 'nx-aptui-repository-apt-facet'},
      {xtype: 'nx-coreui-repository-proxy-facet'},
      {xtype: 'nx-coreui-repository-storage-facet'},
      {xtype: 'nx-coreui-repository-negativecache-facet'},
      {xtype: 'nx-coreui-repository-httpclient-facet'}
    ];

    me.callParent();
  }
});

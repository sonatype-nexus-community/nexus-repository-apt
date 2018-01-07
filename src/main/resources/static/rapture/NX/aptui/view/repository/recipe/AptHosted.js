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
Ext.define('NX.aptui.view.repository.recipe.AptHosted', {
  extend: 'NX.coreui.view.repository.RepositorySettingsForm',
  alias: 'widget.nx-coreui-repository-apt-hosted',
  requires: [
    'NX.aptui.view.repository.facet.AptHostedFacet',
    'NX.aptui.view.repository.facet.AptSigningFacet',
    'NX.coreui.view.repository.facet.StorageFacet',
    'NX.coreui.view.repository.facet.StorageFacetHosted'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {xtype: 'nx-aptui-repository-apthosted-facet'},
      {xtype: 'nx-aptui-repository-aptsigning-facet'},
      {xtype: 'nx-coreui-repository-storage-facet'},
      { xtype: 'nx-coreui-repository-storage-hosted-facet'}
    ];

    me.callParent();
  }
});

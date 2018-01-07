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
 * Configuration specific to apt repositories.
 */
Ext.define('NX.aptui.view.repository.facet.AptFacet', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-aptui-repository-apt-facet',
  requires: [
    'NX.I18n'
  ],
  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'fieldset',
        cls: 'nx-form-section',
        title: NX.I18n.get('Repository_Facet_AptFacet_Title'),
        items: [
          {
            xtype:'textfield',
            name: 'attributes.apt.distribution',
            fieldLabel: NX.I18n.get('Repository_Facet_AptFacet_Distribution_FieldLabel'),
            helpText: NX.I18n.get('Repository_Facet_AptFacet_Distribution_HelpText'),
            allowBlank: false
          },
          {
            xtype: 'checkbox',
            name: 'attributes.apt.flat',
            fieldLabel: NX.I18n.get('Repository_Facet_AptFacet_Flat_FieldLabel'),
            helpText: NX.I18n.get('Repository_Facet_AptFacet_Flat_HelpText'),
            value: false
          }
        ]
      }
    ];

    me.callParent();
  }

});

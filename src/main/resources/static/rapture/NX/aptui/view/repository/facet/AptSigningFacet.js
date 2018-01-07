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
Ext.define('NX.aptui.view.repository.facet.AptSigningFacet', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-aptui-repository-aptsigning-facet',
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
            xtype:'textareafield',
            name: 'attributes.aptSigning.keypair',
            fieldLabel: NX.I18n.get('Repository_Facet_AptSigningFacet_Keypair_FieldLabel'),
            helpText: NX.I18n.get('Repository_Facet_AptSigningFacet_Keypair_HelpText'),
            allowBlank: false,
            grow: true
          },
          {
            xtype:'nx-password',
            name: 'attributes.aptSigning.passphrase',
            fieldLabel: NX.I18n.get('Repository_Facet_AptSigningFacet_Passphrase_FieldLabel'),
            allowBlank: true
          }
        ]
      }
    ];

    me.callParent();
  }

});

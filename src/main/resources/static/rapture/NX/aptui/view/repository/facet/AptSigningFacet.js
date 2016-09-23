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

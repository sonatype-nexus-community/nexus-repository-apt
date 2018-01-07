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
/*global Ext, NX*/

/**
 * Apt plugin strings.
 */
Ext.define('NX.aptui.app.PluginStrings', {
  '@aggregate_priority': 90,

  singleton: true,
  requires: [
    'NX.I18n'
  ],

  keys: {
    Repository_Facet_AptFacet_Title: 'APT Settings',
    Repository_Facet_AptFacet_Distribution_FieldLabel: 'Distribution',
    Repository_Facet_AptFacet_Distribution_HelpText: 'Distribution to fetch, for example trusty',
    Repository_Facet_AptFacet_Flat_FieldLabel: 'Flat',
    Repository_Facet_AptFacet_Flat_HelpText: 'Is this repository flat?',
    Repository_Facet_AptSigningFacet_Keypair_FieldLabel: 'Signing Key',
    Repository_Facet_AptSigningFacet_Keypair_HelpText: 'PGP signing key pair',
    Repository_Facet_AptSigningFacet_Passphrase_FieldLabel: 'Passphrase',
    Repository_Facet_AptHostedFacet_AssetHistoryLimit_FieldLabel: 'Asset History Limit',
    Repository_Facet_AptHostedFacet_AssetHistoryLimit_HelpText: 'Number of versions of each package to keep.  If empty all versions will be kept.',
  }
}, function(self) {
  NX.I18n.register(self);
});

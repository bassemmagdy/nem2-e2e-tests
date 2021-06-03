/// <reference types="cypress" />\
import {v} from '../support/variables'

context('Actions', () => {
    beforeEach(() => {
      // cy.viewport(1280, 720)
      cy.visit(Cypress.config().baseUrl)
    })

it('Initiate transaction from multisig account', () => {
    cy.importMultisigAcc(v.multisig_name, v.account_pass, v.multisig_mnem, v.multisig_addres)
    cy.get('.navigator-items-container > :nth-child(2)').click()
    cy.get(':nth-child(3) > .mosaic_data').click()
    cy.get('.navigator-items-container > :nth-child(1)').click()
    cy.get('.tabs > :nth-child(2)').click()
    cy.get('.inputs-container > .select-size > .ivu-select-selection > div > .ivu-select-selected-value').click()
    cy.wait(1000)
    cy.contains('Seed Account 1 (Multisig)').click()
    cy.responseApi(v.multisig_addres).then(resp=>cy.get('div.amount').should('have.text', resp))
    cy.get('.ivu-alert-message').should('have.text', ' The selected transaction signer is a multi-signature account. ')
    cy.get(':nth-child(2) > .inputs-container > .ivu-tooltip > .ivu-tooltip-rel > .input-size').type(v.multisig_target_addres)
    cy.get('.row-mosaic-attachment-input > .inputs-container > .ivu-tooltip > .ivu-tooltip-rel > .input-style').clear().type('1')
    cy.get('.label-and-select > .select-size > .ivu-select-selection > div > .ivu-select-selected-value').click()
    cy.contains('Fast').click()
    cy.get('.centered-button').click()
    cy.get('.row-75-25 > .ivu-tooltip > .ivu-tooltip-rel > .input-size').type(v.account_pass)
    cy.get('form').contains('Confirm').click()
    cy.get('.Vue-Toastification__toast-body').should('have.text', v.trans_signed_succs_toast)
    cy.get(':nth-child(1) > .Vue-Toastification__toast-body').should('have.text', v.new_unconf_trans_toast)
    cy.get('.navigator-items-container > :nth-child(2)').click()
    cy.get(':nth-child(4) > .mosaic_data').click()
    cy.get('.navigator-items-container > :nth-child(1)').click()
    cy.get('.confirmation-cell').contains('Partial').click({force: true})
    cy.get('.input-size').type(v.account_pass)
    cy.get('form').contains('Confirm').click()
    // cy.get('.Vue-Toastification__toast-body').should('have.text', v.trans_signed_succs_toast)
    cy.get(':nth-child(1) > .Vue-Toastification__toast-body').should('have.text', v.new_unconf_trans_toast)
    cy.get('.navigator-items-container > :nth-child(2)').click()
    cy.get(':nth-child(5) > .mosaic_data').click()
    cy.get('.navigator-items-container > :nth-child(1)').click()
    cy.get('.confirmation-cell').contains('Unconfirmed').click({force: true})
    cy.get('.row-cosignatory-modification-display-cosignature > div > :nth-child(2)').should('have.text', 'Signed by co-signatory=>')
  })
})
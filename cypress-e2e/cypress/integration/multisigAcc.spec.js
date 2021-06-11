/// <reference types="cypress" />\
import 'cypress-localstorage-commands'
import {v,s} from '../support/variables'

context('Actions', () => {
    beforeEach(() => {
      // cy.viewport(1280, 720)
      cy.visit(Cypress.env('url'))
    })

it('Import multisig account', ()=>{
    cy.importMultisigAcc(v.multisig_name, v.account_pass, v.multisig_mnem, v.multisig_address)
})

it('Initiate transaction from multisig account', () => {
    cy.getLocalStorage()
      cy.contains('Click here to login now').click()
      cy.get('.top-welcome-text').should('have.text', 'Welcome to Symbol')
      cy.get(s.profile_name_login_field).clear().type(`${v.multisig_name}{enter}`)
      cy.get(s.password_login_field).type(v.account_pass)
      cy.get('button').contains('Login').click()
      cy.responseApi(v.multisig_address).then(balance=>cy.get(s.balance_xym).should('have.text', balance))
    cy.get(s.accounts_menuitem).click()
    cy.contains('Seed Account 2').click({force: true})
    cy.get('.accountName').should('have.text', 'Seed Account 2')
    cy.get(s.home_menuitem).click()
    cy.get(s.send_tab).click()
    cy.get(s.from_selector).click()
    cy.wait(1000)
    cy.contains('Seed Account 1 (Multisig)').click()
    cy.responseApi(v.multisig_address).then(balance=>cy.get(s.balance_xym).should('have.text', balance))
    cy.get(s.top_alert).should('have.text', ' The selected transaction signer is a multi-signature account. ')
    cy.get(s.to_field).type(v.multisig_target_address)
    cy.get(s.mosaic_amount_field).clear().type('1')
    cy.get(s.fee_selector).click()
    cy.contains('Fast').click()
    cy.get('button').contains('Send').click()
    cy.get(s.password_form_field).type(v.account_pass)
    cy.get('form').contains('Confirm').click()
    cy.get(s.toast_message).should('have.text', v.trans_signed_succs_toast)
    // cy.get(':nth-child(1) > .Vue-Toastification__toast-body').should('have.text', v.new_unconf_trans_toast)
    cy.get(s.accounts_menuitem).click()
    cy.contains('Seed Account 3').click({force: true})
    cy.get(s.home_menuitem).click()
    cy.get(s.height_column).contains('Partial').click({force: true})
    cy.get(s.password_form_field).type(v.account_pass)
    cy.get('form').contains('Confirm').click()
    // cy.get('.Vue-Toastification__toast-body').should('have.text', v.trans_signed_succs_toast)
    cy.get(s.toast_message).should('have.text', v.new_unconf_trans_toast)
    cy.get(s.accounts_menuitem).click()
    cy.contains('Seed Account 4').click({force: true})
    cy.get(s.home_menuitem).click()
    cy.get(s.height_column).contains('Unconfirmed').click({force: true})
    cy.get(s.cosignatures_recieved).should('have.text', 'Signed by co-signatory=>')
  })
})
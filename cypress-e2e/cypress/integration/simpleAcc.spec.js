/// <reference types="cypress" />\
import 'cypress-localstorage-commands'
import {v, s} from '../support/variables'

context('Actions', () => {
    beforeEach(() => {
      // cy.viewport(1280, 720)
      cy.visit(Cypress.env('url'))
    })

    afterEach(()=>{
      cy.saveLocalStorage();
    })

    it('Initiate transaction from simple account', ()=>{
      cy.importSimpleAcc(v.simple_name, v.account_pass, v.simple_mnem, v.simple_addres)
      //Initiate transaction
      cy.get(s.send_tab).click()
      cy.get(s.to_field).type(v.simple_target_addres)
      cy.get(s.mosaic_amount_field).clear().type('1')
      cy.get(s.message_textarea).type(v.some_xym_text)
      cy.get(s.encrypt_message_textbox).check()
      cy.get(s.password_form_field).type(v.account_pass)
      cy.get('form').contains('Confirm').click()
      cy.get(s.fee_selector).click()
      cy.contains('Fast').click()
      cy.get('button').contains('Send').click()
      cy.get(s.password_form_field).type(v.account_pass)
      cy.get('form').contains('Confirm').click()
      cy.get(s.toast_message).should('have.text', v.trans_signed_succs_toast)
      //Verify transaction
      cy.get(s.accounts_menuitem).click()
      cy.contains('Seed Account 2').click({force: true})
      cy.get(s.home_menuitem).click()
      cy.get(s.height_column).contains('Unconfirmed').click({force: true})
      cy.get('button').contains('Decrypt').click()
      cy.get(s.password_form_field).type(v.account_pass)
      cy.get('form').contains('Confirm').click()
      cy.get(s.message_detailed_tx_form).should('have.text', v.some_xym_text)
    })
    
    // it('Make transaction', ()=>{
    //   cy.restoreLocalStorage()
    //   cy.contains('Click here to login now').click()
    // })
})

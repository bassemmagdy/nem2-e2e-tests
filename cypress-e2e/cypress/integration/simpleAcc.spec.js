/// <reference types="cypress" />\
import {v} from '../support/variables'

context('Actions', () => {
    beforeEach(() => {
      // cy.viewport(1280, 720)
      cy.visit(Cypress.config().baseUrl)
    })

    it('Initiate transaction from simple account', ()=>{
      cy.importSimpleAcc(v.simple_name, v.account_pass, v.simple_mnem, v.simple_addres)
      //Initiate transaction
      cy.get('.tabs > :nth-child(2)').click()
      cy.get(':nth-child(2) > .inputs-container > .ivu-tooltip > .ivu-tooltip-rel > .input-size').type(v.simple_target_addres)
      cy.get('.row-mosaic-attachment-input > .inputs-container > .ivu-tooltip > .ivu-tooltip-rel > .input-style').clear().type('1')
      cy.get('.textarea-size').type(v.some_xym_text)
      cy.get('.ivu-checkbox-input').check()
      cy.get('.row-75-25 > .ivu-tooltip > .ivu-tooltip-rel > .input-size').type(v.account_pass)
      cy.get('form').contains('Confirm').click()
      cy.get('.label-and-select > .select-size > .ivu-select-selection > div > .ivu-select-selected-value').click()
      cy.contains('Fast').click()
      cy.get('.centered-button').click()
      cy.get('.row-75-25 > .ivu-tooltip > .ivu-tooltip-rel > .input-size').type(v.account_pass)
      cy.get('form').contains('Confirm').click()
      cy.get('.Vue-Toastification__toast-body').should('have.text', v.trans_signed_succs_toast)
      cy.get(':nth-child(1) > .Vue-Toastification__toast-body').should('have.text', v.new_unconf_trans_toast)
      //Verify transaction
      cy.get('.navigator-items-container > :nth-child(2)').click()
      cy.get('.inactive-background > .mosaic_data').click()
      cy.get('.navigator-items-container > :nth-child(1)').click()
      cy.get('.confirmation-cell').contains('Unconfirmed').click({force: true})
      cy.get('button').contains('Decrypt').click()
      cy.get('.row-75-25 > .ivu-tooltip > .ivu-tooltip-rel > .input-size').type(v.account_pass)
      cy.get('form').contains('Confirm').click()
      cy.get('.form-wrapper > div > span').should('have.text', v.some_xym_text)
    })

})

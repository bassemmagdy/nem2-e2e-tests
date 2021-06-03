// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//

Cypress.Commands.add('responseApi', (address)=>{
    cy.request(`http://ngl-dual-101.testnet.symboldev.network:3000/accounts/${address}`)
      .then(r=>{
        let body = JSON.parse(r.body.account.mosaics[0].amount)
        let res = body.toString()
        if(res.slice(-1)=='0'){
            return res.slice(0, 1) +','+res.slice(1, 4)+'.'+res.slice(-6, -1)
        }
        return res.slice(0, 1) +','+res.slice(1, 4)+'.'+res.slice(-6)
      })
})

Cypress.Commands.add('importSimpleAcc', (name, pass, mnem, address) => {
    cy.get('.profile-type > :nth-child(2)').click()
    cy.get(':nth-child(2) > .form-row-inner-container > .inputs-container').type(name)
    cy.get('.ivu-select-selected-value').click()
    cy.contains('Symbol Testnet').click()
    cy.get(':nth-child(4) > .form-row-inner-container > .inputs-container').type(pass)
    cy.get(':nth-child(5) > .form-row-inner-container > .inputs-container').type(pass)
    cy.get('.inverted-button').click()
    cy.get('.show-mnemonic').type(mnem)
    cy.get('.inverted-button').click()
    cy.responseApi(address).then(resp=>cy.get(':nth-child(1) > .table-item > .address-balance').should('have.text', resp))
    cy.get(':nth-child(1) > .table-item > .address-value').click()
    cy.get(':nth-child(2) > .table-item > .address-value').click()
    cy.responseApi(address).then(resp=>cy.get(':nth-child(1) > .address-item > .table-item-content > .balance-row > .row > :nth-child(2)').should('have.text', resp))
    cy.get('.inverted-button').click()
    cy.get('.ivu-checkbox-input').check()
    cy.get('.inverted-button').click()
    cy.responseApi(address).then(resp=>cy.get('div.amount').should('have.text', resp))
    cy.responseApi(address).then(resp=>cy.get('.mosaic_value > .amount').should('have.text', resp))
})

Cypress.Commands.add('importMultisigAcc', (name, pass, mnem, address) => {
  cy.get('.profile-type > :nth-child(2)').click()
  cy.get(':nth-child(2) > .form-row-inner-container > .inputs-container').type(name)
  cy.get('.ivu-select-selected-value').click()
  cy.contains('Symbol Testnet').click()
  cy.get(':nth-child(4) > .form-row-inner-container > .inputs-container').type(pass)
  cy.get(':nth-child(5) > .form-row-inner-container > .inputs-container').type(pass)
  cy.get('.inverted-button').click()
  cy.get('.show-mnemonic').type(mnem)
  cy.get('.inverted-button').click()
  cy.responseApi(address).then(resp=>cy.get(':nth-child(1) > .table-item > .address-balance').should('have.text', resp))
  cy.get(':nth-child(1) > .table-item > .address-value').click()
  cy.get(':nth-child(2) > .table-item > .address-value').click()
  cy.get(':nth-child(3) > .table-item > .address-value').click()
  cy.get(':nth-child(4) > .table-item > .address-value').click()
  cy.responseApi(address).then(resp=>cy.get(':nth-child(1) > .address-item > .table-item-content > .balance-row > .row > :nth-child(2)').should('have.text', resp))
  cy.get('.inverted-button').click()
  cy.get('.ivu-checkbox-input').check()
  cy.get('.inverted-button').click()
  cy.responseApi(address).then(resp=>cy.get('div.amount').should('have.text', resp))
  cy.responseApi(address).then(resp=>cy.get('.mosaic_value > .amount').should('have.text', resp))
})

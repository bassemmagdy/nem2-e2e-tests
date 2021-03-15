Feature: Metadata state proof
  I want to verify the state of
  my metadata on the blockchain

  Background:
    Given Sarah has 30 units of the network currency

  Scenario: Sarah wants to add a notarized certificate to her account
    Given Sarah request Bob to notarized her "college certificate"
    And Bob published the bonded contract
    When "Sarah" accepts the transaction
    Then Sarah verify the state of document "college certificate" attached by Bob

  Scenario Outline: Sarah wants to update her notarized certificate on her account
    Given Sarah added "college certificate" notarized by Bob to account
    And Sarah requested Bob to update the "college certificate" on account with change of <difference> characters
    And Bob published the bonded contract
    When "Sarah" accepts the transaction
    Then Sarah verify the state of document "college certificate" attached by Bob

    Examples:
      | difference |
      | -6         |
      | 10         |


  Scenario: Bob wants to add a name to Sarah's asset
    Given Sarah registered the asset "sto_token"
    And Bob request Sarah to add a document "name" to asset "sto_token"
    And Bob published the bonded contract
    When "Sarah" accepts the transaction
    Then Sarah verify the state of document "name" attached to asset "sto_token" by Bob

  Scenario Outline: Bob wants to update the name on Sarah's asset
    Given Sarah registered the asset "sto_token"
    And Bob added a document "name" to Sarah asset "sto_token"
    And Bob request to update the "name" on Sarah asset "sto_token" with change of <difference> characters
    And Bob published the bonded contract
    When "Sarah" accepts the transaction
    Then Sarah verify the state of document "name" attached to asset "sto_token" by Bob

    Examples:
      | difference |
      | -7         |
      | 11         |

  @bvt
  Scenario: Bob wants to add a name to Sarah's namespace
    Given Sarah registered the namespace "sto_token"
    And Bob request Sarah to add a document "name" to namespace "sto_token"
    And Bob published the bonded contract
    When "Sarah" accepts the transaction
    Then Sarah verify the state of document "name" attached to namespace "sto_token" by Bob

  Scenario Outline: Bob wants to update the name on Sarah's namespace
    Given Sarah registered the namespace "sto_token"
    And Bob added a document "name" to Sarah namespace "sto_token"
    And Bob request to update the "name" on Sarah namespace "sto_token" with change of <difference> characters
    And Bob published the bonded contract
    When "Sarah" accepts the transaction
    Then Sarah verify the state of document "name" attached to namespace "sto_token" by Bob

    Examples:
      | difference |
      | -7         |
      | 11         |

Feature: Account restriction state proof
  I want to verify the state of
  my account restrictions on the blockchain

  Background:
    # This step registers every user with network currency
    Given the following accounts exist:
      | Alex  |
      | Bobby |
      | Carol |
    And Alex has the following assets registered and active:
      | ticket  |
      | voucher |

  @bvt @bvt_group3
  Scenario: An account blocks receiving transactions containing a specific asset
    When Bobby blocks receiving transactions containing the following assets:
      | ticket  |
      | voucher |
    Then Bobby verify account restriction state in the blockchain

  @bvt @bvt_group3
  Scenario Outline: An account allows receiving transactions containing a specific asset
    When Bobby allows receiving transactions containing the following assets:
      | <asset> |
    Then Bobby verify account restriction state in the blockchain

    Examples:
      | asset            |
      | network currency |
      | ticket           |

  @bvt @bvt_group3
  Scenario: An account unblocks an asset but tries to transfer a blocked asset
    Given Bobby blocks receiving transactions containing the following assets:
      | ticket  |
      | voucher |
    When Bobby removes ticket from blocked assets
    Then Bobby verify account restriction state in the blockchain

  @bvt @bvt_group3
  Scenario: An account removes an asset from the allowed assets
    Given Bobby has only allowed receiving the following assets:
      | ticket  |
      | voucher |
    When Bobby removes ticket from allowed assets
    Then Bobby verify account restriction state in the blockchain

  Scenario: An account blocks receiving transactions from a set of addresses
    Given Bobby blocked receiving transactions from:
      | Alex   |
      | Carol  |
    Then Bobby verify account restriction state in the blockchain

  @bvt @bvt_group3
  Scenario: An account only allows receiving transactions from a set of addresses
    Given Bobby only allowed receiving transactions from:
      | Alex   |
    Then Bobby verify account restriction state in the blockchain

  @bvt @bvt_group3
  Scenario: An account only allows receiving transactions from a set of addresses
    Given Bobby only allowed receiving transactions from:
      | Alex   |
      | Carol  |
    Then Bobby verify account restriction state in the blockchain

  @bvt @bvt_group3
  Scenario: An account unblocks an address
    Given Bobby blocked receiving transactions from:
      | Alex  |
      | Carol |
    And Bobby removes Alex from blocked addresses
    Then Bobby verify account restriction state in the blockchain

  @bvt @bvt_group3
  Scenario: An account removes an address from the allowed addresses
    Given Bobby only allowed receiving transactions from:
      | Alex  |
      | Carol |
    When Bobby removes Alex from allowed addresses
    Then Bobby verify account restriction state in the blockchain

  @bvt @bvt_group3
  Scenario: An account blocks sending transfer transactions
    Given Alex blocks sending transactions of type:
      | TRANSFER               |
      | NAMESPACE_REGISTRATION |
    Then Alex verify account restriction state in the blockchain

  @bvt @bvt_group3
  Scenario: An account only allows transfer and other transaction types
    Given Alex only allows sending transactions of type:
      | ACCOUNT_OPERATION_RESTRICTION |
      | TRANSFER                      |
      | NAMESPACE_REGISTRATION        |
    Then Alex verify account restriction state in the blockchain

  Scenario: An account unblocks a transaction type
    Given Alex blocks sending transactions of type:
      | TRANSFER                |
      | NAMESPACE_REGISTRATION  |
    And Alex removes TRANSFER from blocked transaction types
    Then Alex verify account restriction state in the blockchain

  Scenario: An account removes a transaction type from the allowed transaction types but remaining types should still be allowed
    Given Alex only allows sending transactions of type:
      | ACCOUNT_OPERATION_RESTRICTION |
      | TRANSFER                      |
      | NAMESPACE_REGISTRATION        |
    And Alex removes TRANSFER from allowed transaction types
    Then Alex verify account restriction state in the blockchain


  Scenario: An account with a different restriction types
    Given Alex blocked receiving transactions from:
      | Bobby  |
      | Carol |
    And Alex has only allowed receiving the following assets:
      | ticket  |
      | voucher |
    And Alex removes TRANSFER from allowed transaction types
    Then Alex verify account restriction state in the blockchain

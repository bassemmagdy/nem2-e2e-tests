Feature: Multisig state proof
  I want to verify the state of
  my multisig account on the blockchain

  @bvt @bvt_group3
  Scenario Outline: An account creates an M-of-N contract
    Given Alice defined a <minimumApproval> of 3 multisignature contract called "tom" with <minimumRemoval> required for removal with cosignatories:
      | cosignatory |
      | phone       |
      | computer    |
      | car         |
    And Alice published the bonded contract
    When all the required cosignatories sign the transaction
    Then tom wants to verify multisig account state on the blockchain

    Examples:
      | minimumApproval | minimumRemoval |
      | 1               | 2              |
      | 2               | 1              |
      | 3               | 3              |

  @bvt @bvt_group3
  Scenario: A cosignatory adds another cosignatory to the multisignature contract
    Given Alice created a 1 of 2 multisignature contract called "tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | computer    |
      | phone       |
    And "computer" update the cosignatories of the multisignature:
      | cosignatory | operation |
      | tablet      | add       |
    And computer published the bonded contract
    When "tablet" accepts the transaction
    Then tom wants to verify multisig account state on the blockchain

  @bvt @bvt_group3
  Scenario: A cosignatory remove cosignatory to the multisignature contract
    Given Alice created a 1 of 2 multisignature contract called "tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | computer    |
      | phone       |
    And "computer" update the cosignatories of the multisignature:
      | cosignatory | operation |
      | phone       | remove    |
    When computer publishes the contract
    Then tom wants to verify multisig account state on the blockchain

  @bvt @bvt_group3
  Scenario: A cosignatory adds and removes cosignatories from the multisignature contract
    Given Alice created a 1 of 2 multisignature contract called "tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | computer    |
      | phone       |
    And "computer" update the cosignatories of the multisignature:
      | cosignatory | operation |
      | phone       | remove    |
      | tablet      | add       |
    And computer published the bonded contract
    When "tablet" accepts the transaction
    Then tom wants to verify multisig account state on the blockchain

  @bvt @bvt_group3
  Scenario: A cosignatory accepts the addition of another cosignatory to the multisignature contract
    Given Alice created a 2 of 2 multisignature contract called "tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | computer    |
      | phone       |
    And "computer" update the cosignatories of the multisignature:
      | cosignatory | operation |
      | phone       | remove    |
      | tablet      | add       |
    And computer published the bonded contract
    And "phone" accepted the transaction
    When "tablet" accepts the transaction
    Then tom wants to verify multisig account state on the blockchain

  @bvt @bvt_group3
  Scenario: A cosignatory account removes itself from the multisignature contract
    Given Alice created a 1 of 2 multisignature contract called "tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | computer    |
      | phone       |
    And "computer" update the cosignatories of the multisignature:
      | cosignatory | operation |
      | computer    | remove    |
    When computer publishes the contract
    Then tom wants to verify multisig account state on the blockchain

  @bvt @bvt_group3
  Scenario: All cosignatories are removed from the multisignature contract
    Given Alice created a 1 of 1 multisignature contract called "tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | computer    |
    And "computer" remove the last cosignatory of the multisignature:
      | cosignatory | operation |
      | computer    | remove    |
    When computer publishes the contract
    Then tom become a regular account
    And tom wants to verify multisig account state on the blockchain

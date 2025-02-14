Feature: Receive a notification
  As Alice, I want to get notify when events happened on the blockchain
  such as when transaction is complete or gets cosigned

  Background:
    Given Alice has 100 units of the network currency

  @bvt @bvt_group1
  Scenario: Alice gets notify when a next block is created on the chain.
    Given Alice is register to receive notification from the blockchain
    When Alice waits for a next block
    Then Alice should receive a block notification

  Scenario: Alice gets notify when a next block is finalized on the chain.
    Given Alice is register to receive notification from the blockchain
    When Alice waits for a next finalized block
    Then Alice should receive a finalized block notification

  @bvt @bvt_group1
  Scenario: Alice gets notification when transaction in unconfirmed state
    Given Alice register to receive unconfirmed transaction notification
    When Alice announce valid transaction
    Then Alice should receive a transaction notification

  @bvt @bvt_group1
  Scenario: Alice gets notification when transaction is removed from unconfirmed state
    Given Alice register to receive notification when unconfirmed transaction is removed
    When Alice announce valid transaction
    Then Alice should receive a remove notification

  @bvt @bvt_group1
  Scenario: Alice gets notification when transaction is confirmed state.
    Given Alice register to receive confirmed transaction notification
    When Alice announce valid transaction
    Then Alice should receive a transaction notification

  @bvt @bvt_group1
  Scenario: Use alias address in transaction to get notification in unconfirmed state.
    Given Alice registered the namespace "bob"
    And Alice links the namespace "bob" to the address of Bob
    And Bob register alias "bob" to receive unconfirmed transaction notification
    And Alice registered the asset "X"
    When Alice can send asset "X" to the namespace "bob" instead of the address of Bob
    Then Bob should receive a transaction notification

  @bvt @bvt_group1
  Scenario: Use alias address in transaction and get notification when in confirmed state.
    Given Alice registered the namespace "bob"
    And Alice links the namespace "bob" to the address of Bob
    And Bob register alias "bob" to receive confirmed transaction notification
    And Alice registered the asset "X"
    When Alice can send asset "X" to the namespace "bob" instead of the address of Bob
    Then Bob should receive a transaction notification

  @bvt @bvt_group1
  Scenario: Alice gets notification when invalid transaction failed
    Given Alice register to receive error transaction notification
    And Bob register to receive error transaction notification
    And Sue register to receive error transaction notification
    And Alice defined the following bonded escrow contract:
      | type          | sender | recipient | data               |
      | send-an-asset | Alice  | Bob       | 1 network currency |
      | send-an-asset | Bob    | Sue       | 2 network currency |
    When she publishes no funds bonded contract
    Then Alice should receive an error notification

  @bvt @bvt_group1
  Scenario: Alice gets notification when aggregate bonded transaction requires signing.
    Given Alice register to receive a notification when a bonded transaction requires signing
    And Bob register to receive a notification when a bonded transaction requires signing
    And Sue register to receive a notification when a bonded transaction requires signing
    And Alice defined the following bonded escrow contract:
      | type          | sender | recipient | data                |
      | send-an-asset | Alice  | Bob       | 20 network currency |
      | send-an-asset | Sue    | Alice     | 2 euros             |
    When Alice published the bonded contract
    Then Alice should receive a transaction notification
    And Sue should receive a transaction notification
    And Bob should receive a transaction notification

  @bvt @bvt_group1
  Scenario: Alice gets notification when aggregate bonded transaction is completely signed.
    Given Alice register to receive notification when bonded transaction is signed by all cosigners
    And Alice defined the following bonded escrow contract:
      | type          | sender | recipient | data                |
      | send-an-asset | Alice  | Bob       | 20 network currency |
      | send-an-asset | Sue    | Alice     | 2 euros             |
    And Alice published the bonded contract
    When "Sue" accepts the transaction
    Then Alice should receive a notification that the cosigner have signed the transaction

  @bvt @bvt_group1
  Scenario: Alice gets notification when aggregate bonded transaction is signed by a cosigner
    Given Alice register to receive notification when bonded transaction is signed by Sue
    And Alice defined the following bonded escrow contract:
      | type          | sender | recipient | data                |
      | send-an-asset | Alice  | Bob       | 20 network currency |
      | send-an-asset | Sue    | Alice     | 2 euros             |
    And Alice published the bonded contract
    When "Sue" accepts the transaction
    Then Alice should receive a cosign transaction notification that Sue cosigned

  @bvt @bvt_group1
  Scenario: Multi-level multisig account should get nofitication
    Given Alice created a 1 of 2 multisignature contract called "Computer" with 1 required for removal with cosignatories:
      | cosignatory |
      | Browser     |
      | App         |
    And Alice created a 2 of 2 multisignature contract called "Tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | Computer    |
      | Phone       |
    And Alice register to receive a notification when a bonded transaction requires signing
    And Computer register to receive a notification when a bonded transaction requires signing
    And Tom register to receive a notification when a bonded transaction requires signing
    And Phone defined the following bonded escrow contract:
      | type          | sender   | recipient | data               |
      | send-an-asset | Computer | Sue       | 5 network currency |
      | send-an-asset | Tom      | Bob       | 2 network currency |
      | send-an-asset | Alice    | Phone     | 2 network currency |
    And Phone published the bonded contract
    Then Alice should receive a transaction notification
    And Computer should receive a transaction notification
    And Tom should receive a transaction notification

Feature: Create an escrow contract
  As Alice,
  I want to create an escrow contract between different participants,
  so that there is no need to trust in each other.


    Given the native currency asset is "network currency"
    And the maximum number of participants per escrow contract is 15
    And the maximum number of transactions per escrow contract is 1000
    And an escrow contract requires to lock at least 10 "network currency" to guarantee that the it will conclude
    And an escrow contract is active up to 2 days
    And the mean block generation time is 15 seconds
    And Alice has at least 10 "network currency" in her account

  Background:
    Given the following accounts exist with Network Currency:
      | Alice  | 100 |
      | Bob    | 100 |
      | Sue    | 100 |
    And Sue registered the namespace "euros"
    And Sue registered the asset "X"
    And Sue links the namespace "euros" to the asset "X"

  Scenario: An account creates an escrow contract
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 10 network currency  |
      | send-an-asset  | Sue      | Alice     | 2 euros              |
    When Alice published the bonded contract
    And "Sue" accepts the transaction
    Then every sender participant should receive a notification to accept the contract

  @bvt @bvt_group1
  Scenario: An account creates an escrow contract signed by all the participants
    Given Alice defined the following escrow contract:
      | type           | sender | recipient | data               |
      | send-an-asset  | Alice  | Bob       | 5 network currency |
      | send-an-asset  | Sue    | Alice     | 2 euros            |
    And "Sue" accepted the contract
    When Alice publishes the contract
    Then the swap of assets should conclude

  Scenario: An account creates an escrow contract to multiple participants
    Given Alice defined the following escrow contract:
      | type           | sender | recipient | data                 |
      | send-an-asset  | Alice  | Bob       | 5 network currency   |
      | send-an-asset  | Alice  | Bob       | 10 network currency  |
    When Alice publishes the contract
    Then the swap of assets should conclude

  @bvt @bvt_group1
  Scenario: An account tries to create an escrow already signed by the participants (multisig cosignatory)
    Given Alice created a 2 of 2 multisignature contract called "Tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | Computer    |
      | Phone       |
    And Alice defined the following escrow contract:
      | type           | sender | recipient | data                 |
      | send-an-asset  | Alice  | Computer  | 10 network currency  |
      | send-an-asset  | Tom    | Sue       | 2 network currency   |
    And "Phone" accepted the contract
    And "Computer" accepted the contract
    When Alice publishes the contract
    Then the swap of assets should conclude

  @bvt @bvt_group1
  Scenario: An account tries to create an escrow already signed by the participants (mlma cosignatory)
    Given Alice created a 1 of 2 multisignature contract called "Computer" with 1 required for removal with cosignatories:
      | cosignatory |
      | Browser     |
      | App         |
    And Alice created a 1 of 2 multisignature contract called "Tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | Computer    |
      | Phone       |
    And Phone defined the following escrow contract:
      | type           | sender   | recipient  | data                 |
      | send-an-asset  | Computer | Alice      | 5 network currency   |
      | send-an-asset  | Tom      | Sue        | 2 network currency   |
    And "Browser" accepted the contract
    And "App" accepted the contract
    When Phone publishes the contract
    Then the swap of assets should conclude

  Scenario: An account creates an escrow contract using other types of transactions
    Given Alice defined the following bonded escrow contract:
      | type                             | sender | data                      |
      | register-a-namespace             | Alice  | alice                     |
      | create-a-multisignature-contract | Tom    | 1-of-1, cosignatory:Dan   |
    When Alice published the bonded contract
    And "Tom" accepts the transaction
    And "Dan" accepts the transaction
    Then every sender participant should receive a notification to accept the contract

  Scenario: An account creates an escrow contract and another account locks the funds for that contract
    Given Bob defined the following bonded escrow contract:
      | type                             | sender | data                    |
      | register-a-namespace             | Bob    | bob                     |
      | create-a-multisignature-contract | Tom    | 1-of-1, cosignatory:Dan |
    When Alice published the bonded contract
    And "Tom" accepts the transaction
    And "Dan" accepts the transaction
    Then every sender participant should receive a notification to accept the contract

  Scenario: An account creates an escrow contract and invalid user tries to cosign
    Given Alice defined the following bonded escrow contract:
      | type           | sender | recipient | data                 |
      | send-an-asset  | Alice  | Tom       | 5 network currency   |
      | send-an-asset  | Sue    | Bob       | 2 euros              |
    And Alice published the bonded contract
    And "test" accepts the transaction
    And "Sue" accepts the transaction
    Then the swap of assets should conclude

  Scenario: An account creates an escrow contract and not required valid user tries to cosign
    Given Alice defined the following bonded escrow contract:
      | type           | sender | recipient | data                 |
      | send-an-asset  | Alice  | Tom       | 5 network currency   |
      | send-an-asset  | Sue    | Bob       | 2 euros              |
    And Alice published the bonded contract
    And "Bob" accepts the transaction
    When "Sue" accepts the transaction
    Then the swap of assets should conclude

  Scenario: An account creates an escrow contract which fails and balance of all accounts remain the same
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data             |
      | send-an-asset  | Alice    | Bob       | 20 network currency  |
      | send-an-asset  | Sue      | Alice     | 200 unknown      |
    And Alice published the bonded contract
    And "Sue" accepts the transaction
    When the hash lock expires
    Then Bob balance should remain intact
    And Sue balance should remain intact
    And Alice "network currency" balance should decrease by 10 units

  Scenario: An account submits an incomplete escrow contract then fixes and resubmit
    Given Alice defined the following escrow contract:
      | type           | sender | recipient | data               |
      | send-an-asset  | Alice  | Bob       | 5 network currency |
      | send-an-asset  | Sue    | Alice     | 2 euros            |
    And Alice publishes the contract
    And Alice should receive the error "FAILURE_AGGREGATE_MISSING_COSIGNATURES"
    And "Sue" accepted the contract
    When Alice publishes the contract
    Then the resubmit should conclude

  Scenario: An account creates an escrow contract where one participant have insufficient balance
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 20 network currency  |
      | send-an-asset  | Sue      | Alice     | 200 unknown          |
    And Alice published the bonded contract
    When "Sue" accepts the transaction
    Then she should receive the error "Failure_Core_Insufficient_Balance"

  Scenario: An account tries to create an escrow contract with too many transactions
    Given Alice defined an escrow contract involving more than 1000 transactions
    When she publishes the contract
    Then she should receive the error "Failure_Aggregate_Too_Many_Transactions"

  Scenario: An account tries to create an escrow contract with too many different participants
    Given Alice defined an escrow contract involving 26 different accounts
    When she publishes the contract
    Then she should receive the error "Failure_Aggregate_Too_Many_Cosignatures"

  Scenario: An account tries to create an empty escrow contract
    Given Alice defined the following bonded escrow contract:
    | type  | sender | data   |
    When she publishes the contract
    Then she should receive the error "Failure_Aggregate_No_Transactions"

  Scenario: An account tries to create an escrow contract but the lock expired
    Given Alice defined the following bonded escrow contract:
      | type                             | sender | data                      |
      | register-a-namespace             | Alice  | alice                     |
      | create-a-multisignature-contract | Bob    | 1-of-1, cosignatory:alice |
    And Alice locks 10 "network currency" to guarantee that the contract will conclude 1 block
    When she publishes the contract
    Then she should receive the error "FAILURE_LOCKHASH_INACTIVE_HASH"
    And Alice "network currency" balance should decrease by 10 units

  Scenario: An account tries to create an escrow contract but the lock already exists
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 1 network currency   |
      | send-an-asset  | Bob      | Sue       | 2 network currency   |
    And Alice locks 10 "network currency" to guarantee that the contract will conclude 5 blocks
    When Alice tries to lock 10 "network currency" to guarantee that the contract will conclude 1 blocks
    Then she should receive the error "FAILURE_LOCKHASH_HASH_ALREADY_EXISTS"

  Scenario: An account tries to create an escrow contract without locking the funds
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 1 network currency   |
      | send-an-asset  | Bob      | Sue       | 2 network currency   |
    When she publishes no funds bonded contract
    Then she should receive the error "FAILURE_LOCKHASH_UNKNOWN_HASH"

  Scenario: An account tries to create an escrow but locks another mosaic that is not network currency
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 1 network currency   |
      | send-an-asset  | Bob      | Sue       | 2 network currency   |
    When Alice tries to lock 10 "tickets" to guarantee that the contract will conclude 6 blocks
    Then she should receive the error "FAILURE_LOCKHASH_INVALID_MOSAIC_AMOUNT"
    And Alice balance should remain intact

  Scenario Outline: An account tries to create an escrow an escrow but the amount is not equal to 10 network currency
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 1 network currency   |
      | send-an-asset  | Bob      | Sue       | 2 network currency   |
    When Alice tries to lock <amount> "network currency" to guarantee that the contract will conclude 6 blocks
    Then she should receive the error "FAILURE_LOCKHASH_INVALID_MOSAIC_AMOUNT"
    And Alice balance should remain intact
    Examples:
      | amount |
      | -1     |
      | 9      |
      | 11     |

  Scenario Outline: An account tries to create an escrow but sets an invalid duration
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 1 network currency   |
      | send-an-asset  | Bob      | Sue       | 2 network currency   |
    When Alice tries to lock 10 "network currency" to guarantee that the contract will conclude <duration> blocks
    Then she should receive the error "FAILURE_LOCKHASH_INVALID_DURATION"
    And Alice balance should remain intact

    Examples:
      | duration |
      | -1       |
      | 0        |

  Scenario: An account tries to create an escrow contract but does not have 10 network currency
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 1 network currency   |
      | send-an-asset  | Bob      | Sue       | 2 network currency   |
    When Dan tries to lock 10 "network currency" to guarantee that the contract will conclude 6 blocks
    Then she should receive the error "Failure_Core_Insufficient_Balance"

  Scenario: An account creates an escrow already signed but also cosign
    Given Alice defined the following escrow contract:
      | sender | recipient | type          | data                 |
      | Alice  | Bob       | send-an-asset | 1 network currency   |
      | Bob    | Alice     | send-an-asset | 2 network currency   |
    And "Alice" accepts the contract
    When Alice publishes the contract
    Then she should receive the error "Failure_Aggregate_Redundant_Cosignatures"

  Scenario: An account tries to create an escrow already signed but is not signed by every participant
    Given Alice defined the following escrow contract:
      | sender | recipient | type          | data                 |
      | Alice  | Bob       | send-an-asset | 1 network currency   |
      | Bob    | Alice     | send-an-asset | 2 network currency   |
    When she publishes the contract
    Then she should receive the error "FAILURE_AGGREGATE_MISSING_COSIGNATURES"

  Scenario: An account tries to create an escrow already signed but is not signed by every participant (multisig cosignatory)
    Given Alice created a 2 of 2 multisignature contract called "Tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | Computer    |
      | Phone       |
    And Alice defined the following escrow contract:
      | sender | recipient | type          | data                 |
      | Alice  | Tom       | send-an-asset | 1 network currency   |
      | Tom    | Alice     | send-an-asset | 2 network currency   |
    And "Phone" accepted the contract
    When Alice publishes the contract
    Then she should receive the error "FAILURE_AGGREGATE_MISSING_COSIGNATURES"

  Scenario: An account tries to create an escrow already signed but is not signed by every participant (mlma cosignatory)
    Given Alice created a 2 of 2 multisignature contract called "Computer" with 1 required for removal with cosignatories:
      | cosignatory |
      | Browser     |
      | App         |
    And Alice created a 2 of 2 multisignature contract called "Tom" with 1 required for removal with cosignatories:
      | cosignatory |
      | Computer    |
      | Phone       |
    And Alice defined the following escrow contract:
      | sender | recipient | type          | data                 |
      | Alice  | Tom       | send-an-asset | 1 network currency   |
      | Tom    | Computer  | send-an-asset | 2 network currency   |
    And "Phone" accepts the contract
    And "Browser" accepts the contract
    When she publishes the contract
    Then she should receive the error "FAILURE_AGGREGATE_MISSING_COSIGNATURES"

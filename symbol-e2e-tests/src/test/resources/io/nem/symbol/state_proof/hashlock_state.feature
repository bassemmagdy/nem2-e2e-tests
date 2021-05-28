Feature: Lock hash state proof
  I want to verify the state of
  my lock hash on the blockchain

  Background:
    Given Alice has 50 units of the network currency

  Scenario: An account verify a lock hash which is unused
    Given Alice defined the following bonded escrow contract:
      | type                             | sender | data                      |
      | register-a-namespace             | Alice  | alice                     |
      | create-a-multisignature-contract | Bob    | 1-of-1, cosignatory:alice |
    When Alice locks 10 "network currency" to guarantee that the contract will conclude 10 block
    Then Alice wants to verify unused hash lock state on the blockchain

  Scenario: An account verify a lock hash which is used
    Given Alice defined the following bonded escrow contract:
      | type           | sender   | recipient | data                 |
      | send-an-asset  | Alice    | Bob       | 1 network currency   |
      | send-an-asset  | Bob      | Sue       | 2 network currency   |
    When Alice published the bonded contract
    And "Bob" accepts the transaction
    Then Alice wants to verify used hash lock state on the blockchain

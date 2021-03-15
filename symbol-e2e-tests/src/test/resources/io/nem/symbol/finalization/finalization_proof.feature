Feature: account register to vote
  As Alice
  I want to vote for which blocks are valid
  on the blockchain.

  @bvt
  Scenario: User want to verify that a node voted in the latest height
    When Alice wants to know if node voted in the latest height

  @bvt
  Scenario: User want to verify that a node voted in the latest epoch
    Given Alice has a voting node register
    When Alice wants to know if node voted in the latest epoch
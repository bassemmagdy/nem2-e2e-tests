Feature: account register to vote
  As Alice
  I want to vote for which blocks are valid
  on the blockchain.


  @bvt
  Scenario: An account registers to vote on finalized blocks
    Given Alice
    When Alice sends <amount> asset of "<asset>" to Bob
    Then Bob should receive <amount> of asset "<asset>"
    And Alice "<asset>" balance should decrease by <amount> unit


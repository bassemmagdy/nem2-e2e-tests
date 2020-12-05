Feature: Account state proof
  As Tom,
  I want to verify the state of
  my account on the blockchain

  @bvt
  Scenario: An account is created on the block chain without sending a transaction
    Given Alice sends 1 asset of "network currency" to Tom
    Then Tom verify his account state in the blockchain

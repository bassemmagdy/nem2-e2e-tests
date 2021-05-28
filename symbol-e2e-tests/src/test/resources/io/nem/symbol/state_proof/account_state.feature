Feature: Account state proof
  I want to verify the state of
  my account on the blockchain

  Background:
    Given Alice has 10 units of the network currency

  @bvt @bvt_group3
  Scenario: An account is created on the block chain without sending a transaction
    Given Alice sends 1 asset of "network currency" to Tom
    Then Tom verify account state in the blockchain

  @bvt @bvt_group3
  Scenario: Verify high value account
    Given Alice sends 1 asset of "network currency" to Tom
    Then Alice verify account state in the blockchain

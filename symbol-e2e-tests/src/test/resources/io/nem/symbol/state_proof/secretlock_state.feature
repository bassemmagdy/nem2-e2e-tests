Feature: Secret lock state proof
  I want to verify the state of
  my secret lock on the blockchain

  @bvt @bvt_group1
  Scenario: An account locks assets
    Given Alice derived the secret from the seed using "SHA3_256"
    And Alice locked 10 "network currency" for Tom on the network for 10 blocks
    When Tom proved knowing the secret's seed on the network
    Then Alice wants to verify secret lock state on the blockchain

  Scenario: An account locks assets with hash 160
    Given Alice derived the secret from the seed using "HASH_160"
    And Alice locked 10 "network currency" for Tom on the network for 10 blocks
    And Alice wants to verify secret lock state on the blockchain
    When Tom proved knowing the secret's seed on the network
    Then Alice wants to verify secret lock state on the blockchain

  @bvt @bvt_group1
  Scenario: An exchange of assets across different blockchain concludes
    Given Bob derived the secret from the seed using "HASH_256"
    And Bob locked 10 "euros" for Alice on the network for 10 blocks
    When Tom proved knowing the secret's seed on the network
    Then Alice wants to verify secret lock state on the blockchain

  @bvt @bvt_group1
  Scenario: An exchange of assets doesn't conclude because the participant decides not locking the assets
    Given Alice derived the secret from the seed using "SHA3_256"
    When Alice locked 10 "network currency" for Bob on the network for 5 block
    Then Alice wants to verify secret lock state on the blockchain

Feature: Mosaic state proof
  I want to verify the state of
  my mosaic on the blockchain

  Background:
    Given Alice has 100 units of the network currency

  @bvt @bvt_group3
  Scenario: Alice create a non mosaic on the block chain and want to verify its state
    When  Alice registers a non-expiring asset "token"
    Then Alice wants to verify "token" state on the blockchain

  @bvt @bvt_group3
  Scenario Outline: An account registers an expiring asset with valid properties with divisibility
    When Alice registers an asset named "token" with <transferability>, supply <supply-mutability> with divisibility <divisibility> for <duration> blocks
    Then Alice wants to verify "token" state on the blockchain

    Examples:
      | duration | transferability    | supply-mutability | divisibility |
      | 1        | TRANSFERABLE       | IMMUTABLE         | 0            |
      | 2        | NONTRANSFERABLE    | MUTABLE           | 6            |
      | 3        | TRANSFERABLE       | MUTABLE           | 1            |
      | 1        | NONTRANSFERABLE    | IMMUTABLE         | 2            |

  Scenario Outline: An account updates an existing asset with valid properties
    Given Alice registers an asset named "token" with TRANSFERABLE, supply IMMUTABLE with divisibility 5 for 5 blocks
    When Alice updates asset named "token" to <transferability>, supply <supply-mutability> with divisibility <divisibility> for <duration> blocks
    Then Alice wants to verify "token" state on the blockchain

    Examples:
      | duration | transferability    | supply-mutability | divisibility |
      | 1        | TRANSFERABLE       | IMMUTABLE         | 5            |
      | 2        | NONTRANSFERABLE    | MUTABLE           | 4            |
      | 3        | TRANSFERABLE       | MUTABLE           | 2            |
      | 0        | NONTRANSFERABLE    | IMMUTABLE         | 6            |

  @bvt @bvt_group3
  Scenario: Verify high value account
    Given Alice sends 1 asset of "network currency" to Tom
    Then Alice wants to verify "network currency" state on the blockchain

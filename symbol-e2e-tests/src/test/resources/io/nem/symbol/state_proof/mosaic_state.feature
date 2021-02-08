Feature: Mosaic state proof
  I want to verify the state of
  my mosaic on the blockchain

  @bvt
  Scenario: Alice create a non mosaic on the block chain and want to verify its state
    When  Alice registers a non-expiring asset "token"
    Then Alice wants to verify "token" state on the blockchain

  @bvt
  Scenario Outline: An account registers an expiring asset with valid properties with divisibility
    When Alice registers an asset named "token" with <transferability>, supply <supply-mutability> with divisibility <divisibility> for <duration> blocks
    Then Alice wants to verify "token" state on the blockchain

    Examples:
      | duration | transferability    | supply-mutability | divisibility |
      | 1        | transferable       | immutable         | 0            |
      | 2        | nontransferable    | mutable           | 6            |
      | 3        | transferable       | mutable           | 1            |
      | 1        | nontransferable    | immutable         | 2            |

  Scenario Outline: An account updates an existing asset with valid properties
    Given Alice registers an asset named "token" with transferable, supply immutable with divisibility 5 for 5 blocks
    When Alice updates asset named "token" to <transferability>, supply <supply-mutability> with divisibility <divisibility> for <duration> blocks
    Then Alice wants to verify "token" state on the blockchain

    Examples:
      | duration | transferability    | supply-mutability | divisibility |
      | 1        | transferable       | immutable         | 5            |
      | 2        | nontransferable    | mutable           | 4            |
      | 3        | transferable       | mutable           | 2            |
      | 0        | nontransferable    | immutable         | 6            |

  @bvt
  Scenario: Verify high value account
    Given Alice sends 1 asset of "network currency" to Tom
    Then Alice wants to verify "network currency" state on the blockchain
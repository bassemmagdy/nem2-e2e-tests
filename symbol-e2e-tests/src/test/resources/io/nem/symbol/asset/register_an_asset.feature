 Feature: Register an asset
  As Alice
  I want to register an asset
  So that I can send one unit to Bob.

    The native currency asset is "network currency"
    and registering an asset costs 500 "network currency".
    The mean block generation time is 15 seconds
    and the maximum registration period is 1 year
    and the maximum asset divisibility is 6
    and the maximum number of assets an account can have is 1000
    and the maximum asset supply is 9000000000

   Background:
     Given the following accounts exist with Network Currency:
       | Alice    | 500 |

  @bvt @bvt_group1
  Scenario Outline: An account registers an expiring asset with valid properties with divisibility
    When Alice registers an asset named "token" with <transferability>, supply <supply-mutability> with divisibility <divisibility> for <duration> blocks
    Then Alice should become the owner of the new asset for at least <duration> blocks
    And Alice pays mosaic rental fee

    Examples:
      | duration | transferability    | supply-mutability | divisibility |
      | 1        | TRANSFERABLE       | IMMUTABLE         | 0            |
      | 2        | NONTRANSFERABLE    | MUTABLE           | 6            |
      | 3        | TRANSFERABLE       | MUTABLE           | 1            |
      | 1        | NONTRANSFERABLE    | IMMUTABLE         | 2            |

   @bvt @bvt_group1
   Scenario Outline: An account updates an existing asset with valid properties
     Given Alice registers an asset named "token" with TRANSFERABLE, supply IMMUTABLE with divisibility 5 for 5 blocks
     When Alice updates asset named "token" to <transferability>, supply <supply-mutability> with divisibility <divisibility> for <duration> blocks
     Then token asset should be updated correctly
     And Alice pays mosaic rental fee

     Examples:
       | duration | transferability    | supply-mutability | divisibility |
       | 1        | TRANSFERABLE       | IMMUTABLE         | 5            |
       | 2        | NONTRANSFERABLE    | MUTABLE           | 4            |
       | 3        | TRANSFERABLE       | MUTABLE           | 2            |
       | 0        | NONTRANSFERABLE    | IMMUTABLE         | 6            |


   Scenario: An account tries to alter the asset property without owning all supply
     Given Alice registers an asset named "token" with TRANSFERABLE, supply IMMUTABLE with divisibility 4 for 5 blocks
     And Alice decides to increase the asset supply in 10 units
     When Alice tries to update asset named "token" to TRANSFERABLE, supply IMMUTABLE with divisibility 3 for 5 blocks
     Then she should receive the error "FAILURE_MOSAIC_MODIFICATION_DISALLOWED"

  @bvt @bvt_group1
  Scenario: An account registers a non-expiring asset
    When Alice registers a non-expiring asset "token"
    And Alice should become the owner of the new asset
    And Alice pays mosaic rental fee

  Scenario Outline: An account tries to register an asset with invalid values
    When Alice registers an asset for <duration> in blocks with <divisibility> divisibility
    Then she should receive the error "<error>"
    And Alice balance should remain intact

    Examples:
      | duration | divisibility | error                                |
      | -1       | 0            | FAILURE_MOSAIC_INVALID_DURATION      |
      | 1        | -1           | FAILURE_MOSAIC_INVALID_DIVISIBILITY  |
      | 22000000 | 0            | FAILURE_MOSAIC_INVALID_DURATION      |
      | 60       | 7            | FAILURE_MOSAIC_INVALID_DIVISIBILITY  |

  Scenario: An account tries to register an asset but does not have enough funds
    Given Dan has 1 units of the network currency
    When Dan registers an asset
    Then Dan should receive the error "FAILURE_CORE_INSUFFICIENT_BALANCE"


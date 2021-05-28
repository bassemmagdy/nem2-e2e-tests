Feature: Alter an asset supply
  As Alice
  I want to alter an asset supply
  So that it represents the available amount of an item in my shop.

    Given the mean block generation time is 15 seconds
    And the maximum asset supply is 9000000000
    And Alice has 10000000 "network currency" in her account

  Background:
    Given the following accounts exist with Network Currency:
      | Alice    | 500 |

  @bvt @bvt_group1
  Scenario Outline: An account alters an asset supply
    Given Alice has registered a supply <supply-mutability> asset with an initial supply of 20 units
    When Alice decides to <direction> the asset supply in <amount> units
    Then the balance of the asset in her account should <direction> in <amount> units

    Examples:
      | supply-mutability | direction | amount |
      | MUTABLE           | INCREASE  | 5      |
      | IMMUTABLE         | INCREASE  | 5      |
      | MUTABLE           | DECREASE  | 20     |
      | IMMUTABLE         | DECREASE  | 20     |

  Scenario Outline: An account tries to alter an asset supply incorrectly
    Given Alice has registered an asset with an initial supply of 20 units
    When Alice accidentally <direction> the asset supply in <amount> units
    Then she should receive the error "<error>"

    Examples:
      | direction | amount           | error                                       |
      | INCREASE  | 9000000000000000 | FAILURE_MOSAIC_SUPPLY_EXCEEDED              |
      | DECREASE  | 21               | FAILURE_MOSAIC_SUPPLY_NEGATIVE              |
      | INCREASE  | 0                | FAILURE_MOSAIC_INVALID_SUPPLY_CHANGE_AMOUNT |

  Scenario: An account tries to alter the supply of a supply mutable asset but does not own all the units
    Given Alice has registered a supply MUTABLE asset with an initial supply of 20 units
    And she transfer 10 units to another account
    When Alice decides to INCREASE the asset supply in 2 units
    Then the balance of the asset in her account should INCREASE in 2 units

  Scenario: An account tries to alter the supply of a supply non-mutable asset but does not own all the units
    Given Alice has registered a supply IMMUTABLE asset with an initial supply of 20 units
    And she transfer 10 units to another account
    When Alice tries to INCREASE the asset supply in 2 units
    Then she should receive the error "FAILURE_MOSAIC_SUPPLY_IMMUTABLE"

  Scenario: An account tries to alter the supply of an expired asset
    Given Alice has registered expiring asset "A" for 6 block
    And the asset is now expired
    When Alice tries to INCREASE the asset supply in 2 units
    Then she should receive the error "FAILURE_MOSAIC_EXPIRED"

Feature: Namepsace state proof
  I want to verify the state of
  my namespace on the blockchain

  @bvt
  Scenario Outline: Verify namespace on the blockchain
    When Alice registers a namespace named "<name>" for <duration> block
    Then Alice wants to verify namespace "<name>" state on the blockchain

    Examples:
      | name  | duration |
      | test1 | 5        |
      | test2 | 100      |

  @bvt
  Scenario Outline: Verify namespace after registration extension
    Given Alice registered the namespace "token"
    When Alice extends the registration of the namespace named "token" for <duration> blocks
    Then Alice wants to verify namespace "token" state on the blockchain

    Examples:
      | duration |
      | 10       |
      | 20       |

  @bvt
  Scenario: Verify namespace with address alias
    Given Alice registered the namespace "token"
    And Alice registered the asset "X"
    When Alice links the namespace "token" to the address of Sue
    Then Alice wants to verify namespace "token" state on the blockchain

  @bvt
  Scenario: Verify namespace with asset alias
    Given Alice registered the namespace "token"
    And Alice registered the asset "X"
    When Alice links the namespace "token" to the asset "X"
    Then Alice wants to verify namespace "token" state on the blockchain
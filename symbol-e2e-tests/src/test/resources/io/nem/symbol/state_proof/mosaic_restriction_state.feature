Feature: Mosaic restriction state proof
  I want to verify the state of
  my account on the blockchain

  Background:
    # This step registers every user with network currency
    Given the following accounts exist:
      | Alex                |
      | Bobby               |
      | Carol               |
      | EligibilityProvider |
    And Alex has the following mosaics registered
      | Mosaic                 | Restrictable |
      | MyCompanySharesPrivate | true         |
      | MyCompanySharesPublic  | false        |
    And Bobby has at least 10 MyCompanySharesPrivate balance

  @bvt @bvt_group3
  Scenario: An account that doesn't pass the restriction cannot transact with the mosaic
    Given Alex creates the following restriction
      | Mosaic                 | Restriction Key | Restriction value | Restriction Type |
      | MyCompanySharesPrivate | can_hold        | 1                 | EQ               |
    And Alex gives Bobby the following restriction keys
      | Mosaic                 | restriction key | restriction value |
      | MyCompanySharesPrivate | can_hold        | 0                 |
    And Alex gives Carol the following restriction keys
      | Mosaic                 | restriction key | restriction value |
      | MyCompanySharesPrivate | can_hold        | 0                 |
    Then Alex verify mosaic global restriction state for "MyCompanySharesPrivate" in the blockchain
    And Carol verify mosaic address restriction state for "MyCompanySharesPrivate" in the blockchain
    And Bobby verify mosaic address restriction state for "MyCompanySharesPrivate" in the blockchain

  @bvt @bvt_group3
  Scenario: An account that passes the restriction should be able to transact with the mosaic
    Given Alex creates the following restriction
      | Mosaic                 | Restriction Key | Restriction value | Restriction Type |
      | MyCompanySharesPrivate | can_hold        | 1                 | EQ               |
    And Alex gives Bobby the following restriction keys
      | Mosaic                 | restriction key | restriction value |
      | MyCompanySharesPrivate | can_hold        | 1                 |
    And Alex gives Carol the following restriction keys
      | Mosaic                 | restriction key | restriction value |
      | MyCompanySharesPrivate | can_hold        | 1                 |
    Then Alex verify mosaic global restriction state for "MyCompanySharesPrivate" in the blockchain
    And Carol verify mosaic address restriction state for "MyCompanySharesPrivate" in the blockchain
    And Bobby verify mosaic address restriction state for "MyCompanySharesPrivate" in the blockchain

  @bvt @bvt_group3
  Scenario: Make a modification to a mosaic restriction
    Given Alex creates the following restrictions
      | Mosaic                 | Restriction Key | Restriction value | Restriction Type |
      | MyCompanySharesPrivate | can_hold        | 2                 | EQ               |
    And Alex gives Bobby the following restriction keys
      | Mosaic                 | restriction key | restriction value |
      | MyCompanySharesPrivate | can_hold        | 1                 |
    And Alex gives Carol the following restriction keys
      | Mosaic                 | restriction key | restriction value |
      | MyCompanySharesPrivate | can_hold        | 1                 |
    When Alex makes a modification to the mosaic restriction
      | Mosaic                 | Restriction Key | New Restriction value | New Restriction Type | Previous Restriction Value |
      | MyCompanySharesPrivate | can_hold        | 1                     | EQ                   | 2                          |
    Then Alex verify mosaic global restriction state for "MyCompanySharesPrivate" in the blockchain
    And Carol verify mosaic address restriction state for "MyCompanySharesPrivate" in the blockchain
    And Bobby verify mosaic address restriction state for "MyCompanySharesPrivate" in the blockchain

  @bvt @bvt_group3
  Scenario: An account that passes multiple restrictions can interact with the mosaic
    Given Alex creates the following restrictions
      | Mosaic                 | Restriction Key | Restriction value | Restriction Type |
      | MyCompanySharesPrivate | can_hold        | 1                 | EQ               |
      | MyCompanySharesPrivate | can_share       | 1                 | EQ               |
    And Alex gives Bobby the following restriction keys
      | Mosaic                 | Restriction key | Restriction value |
      | MyCompanySharesPrivate | can_hold        | 1                 |
      | MyCompanySharesPrivate | can_share       | 1                 |
    And Alex gives Carol the following restriction keys
      | Mosaic                 | Restriction key | Restriction value |
      | MyCompanySharesPrivate | can_hold        | 1                 |
      | MyCompanySharesPrivate | can_share       | 1                 |
    Then Alex verify mosaic global restriction state for "MyCompanySharesPrivate" in the blockchain
    And Carol verify mosaic address restriction state for "MyCompanySharesPrivate" in the blockchain
    And Bobby verify mosaic address restriction state for "MyCompanySharesPrivate" in the blockchain

Feature: Prevent receiving transactions containing a specific asset
  As Bobby,
  I only want to receive "network currency" assets
  So that I can ensure I don't own assets unrelated with my activity

  Background:
    # This step registers every user with network currency
    Given the following accounts exist with Network Currency:
      | Alex  | 200 |
      | Bobby | 200 |
      | Carol | 200 |
    And Alex has the following assets registered and active:
      | ticket  |
      | voucher |

  @bvt @bvt_group1
  Scenario: An account blocks receiving transactions containing a specific asset
    Given Bobby blocks receiving transactions containing the following assets:
      | ticket  |
      | voucher |
    When Alex tries to send 1 asset of "ticket" to Bobby
    Then Alex should receive the error "FAILURE_RESTRICTIONACCOUNT_MOSAIC_TRANSFER_PROHIBITED"
    And Bobby balance should remain intact
    And Alex balance should remain intact

  @bvt @bvt_group1
  Scenario Outline: An account allows receiving transactions containing a specific asset
    Given Bobby allows receiving transactions containing the following assets:
      | <asset> |
    When Alex sends <amount> asset of "<asset>" to Bobby
    Then Bobby should receive <amount> of asset "<asset>"
    And Alex "<asset>" balance should decrease by <amount> units

    Examples:
      | amount | asset            |
      | 1      | network currency |
      | 1      | ticket           |

  @bvt @bvt_group1
  Scenario: An account unblocks an asset but tries to transfer a blocked asset
    Given Bobby blocks receiving transactions containing the following assets:
      | ticket  |
      | voucher |
    When Bobby removes ticket from blocked assets
    And Alex tries to send 1 asset of "voucher" to Bobby
#     And receiving voucher assets should remain blocked
      # This can be confirmed when Alex receives below error when he tries send a voucher asset to Bobby.
    Then Alex should receive the error "FAILURE_RESTRICTIONACCOUNT_MOSAIC_TRANSFER_PROHIBITED"

  @bvt @bvt_group1
  Scenario: An unblocked asset should be transferable
    Given Bobby blocks receiving transactions containing the following assets:
      | ticket  |
      | voucher |
    And Bobby removes ticket from blocked assets
    When Alex sends 1 asset of "ticket" to Bobby
    Then Bobby should receive 1 of asset "ticket"

  @bvt @bvt_group1
  Scenario: An account removes an asset from the allowed assets
    Given Bobby has only allowed receiving the following assets:
      | ticket  |
      | voucher |
    When Bobby removes ticket from allowed assets
    And Alex sends 1 asset of "voucher" to Bobby
#      And only receiving voucher assets should remain allowed
      # This can be confirmed when Alex successfully sends a voucher asset to Bobby.
    Then Bobby should receive 1 of asset "voucher"

  Scenario: An account unblocks a not blocked asset
    Given Bobby has blocked receiving ticket assets
    When Bobby tries to remove voucher from blocked assets
    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_INVALID_MODIFICATION"

  Scenario: An account removes an asset that does not exist in the allowed assets
    Given Bobby has blocked receiving ticket assets
    When Bobby tries to remove voucher from allowed assets
    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_INVALID_MODIFICATION"

  Scenario: An account tries only to allow receiving transactions containing specific assets when it has blocked assets
    Given Bobby has blocked receiving ticket assets
    When Bobby tries to only allow receiving voucher assets
    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_INVALID_MODIFICATION"

  Scenario: An account tries to block receiving transactions containing specific assets when it has allowed assets
    Given Bobby has only allowed receiving ticket assets
    When Bobby tries to block receiving voucher assets
    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_INVALID_MODIFICATION"

  Scenario: An account tries to block an asset twice
    Given Bobby has blocked receiving ticket assets
    When Bobby tries to block receiving ticket assets
    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_INVALID_MODIFICATION"

  Scenario: An account tries to allow an asset twice
    Given Bobby has only allowed receiving ticket assets
    When Bobby tries to only allow receiving ticket assets
    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_INVALID_MODIFICATION"
#
#  Scenario: An account tries add too many restrictions in a single transaction
#    Given Alex has 270 different assets registered and active
#    When Bobby tries to add more than 270 restrictions in a transaction
#    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_MODIFICATION_COUNT_EXCEEDED"
#
#  Scenario: An account tries delete too many restrictions in a single transaction
#    Given Alex has 270 different assets registered and active
#    When Bobby tries to delete more than 270 restrictions in a transaction
#    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_MODIFICATION_COUNT_EXCEEDED"
#
#  Scenario: An account tries to block too many mosaics
#    Given Bobby has already blocked receiving 512 different assets
#    When Bobby tries to block receiving ticket assets
#    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_VALUES_COUNT_EXCEEDED"
#
#  Scenario: An account tries to only allow too many mosaics
#    Given Bobby has already allowed receiving 512 different assets
#    When Bobby tries to only allow receiving ticket assets
#    Then Bobby should receive the error "FAILURE_RESTRICTIONACCOUNT_VALUES_COUNT_EXCEEDED"

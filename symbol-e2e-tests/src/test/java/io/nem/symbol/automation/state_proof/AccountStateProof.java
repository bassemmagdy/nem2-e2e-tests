package io.nem.symbol.automation.state_proof;

import cucumber.api.java.en.Then;
import io.nem.symbol.automation.common.BaseTest;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.AccountHelper;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

import static org.junit.Assert.assertEquals;

public class AccountStateProof extends BaseTest {

  public AccountStateProof(final TestContext testContext) {
    super(testContext);
  }

  @Then("^(\\w+) verify his account state in the blockchain$")
  public void VerifyAccountState(final String username) {
    final Account account = getUser(username);
    final AccountInfo accountInfo =
        new AccountHelper(getTestContext()).getAccountInfo(account.getAddress());
    final StateMerkleProof<AccountInfo> stateMerkleProof =
        new StateProofServiceImpl(getTestContext().getRepositoryFactory())
            .account(accountInfo)
            .blockingFirst();
    assertEquals(
        "Account state hash did not match",
        accountInfo.serialize(),
        stateMerkleProof.getLeafValue().get());
  }
}

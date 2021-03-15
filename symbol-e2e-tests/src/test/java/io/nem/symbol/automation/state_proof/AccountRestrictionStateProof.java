package io.nem.symbol.automation.state_proof;

import cucumber.api.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.AccountHelper;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.account.AccountRestrictions;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountRestrictionStateProof extends StateProofBaseTest<AccountInfo> {

    public AccountRestrictionStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) verify account restriction state in the blockchain$")
    public void VerifyAccountRestrictionState(final String username) {
        final Account account = getUser(username);
        final AccountInfo accountInfo =
                new AccountHelper(getTestContext()).getAccountInfo(account.getAddress());
        final StateMerkleProof<AccountRestrictions> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .accountRestrictions(accountInfo.getAddress())
                        .blockingFirst();
        assertTrue("Account state not valid: " + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
    }
}

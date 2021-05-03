package io.nem.symbol.automation.state_proof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.cucumber.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.AccountHelper;
import io.nem.symbol.sdk.api.Listener;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.blockchain.BlockInfo;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

public class AccountStateProof extends StateProofBaseTest<AccountInfo> {

    public AccountStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) verify account state in the blockchain$")
    public void VerifyAccountState(final String username) {
        final Account account = getUser(username);
        final Listener listener = getListener();
        final BlockInfo blockInfo = getObservableValueWithQueryTimeout(listener.newBlock().take(1));
        final AccountInfo accountInfo =
                new AccountHelper(getTestContext()).getAccountInfo(account.getAddress());
        final StateMerkleProof<AccountInfo> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .account(accountInfo.getAddress())
                        .blockingFirst();
        logStateInfo(stateMerkleProof, toStateHash(accountInfo.serialize()));
        assertTrue("Account state not valid in block: " + blockInfo.getHeight().longValue() + " address:" + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Account state hash did not match for account : " + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(),
                toStateHash(accountInfo),
                stateMerkleProof.getLeafValue().get());
    }
}

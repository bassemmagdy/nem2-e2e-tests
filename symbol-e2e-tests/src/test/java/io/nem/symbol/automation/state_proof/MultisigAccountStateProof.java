package io.nem.symbol.automation.state_proof;

import cucumber.api.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.AccountHelper;
import io.nem.symbol.automationHelpers.helper.sdk.MosaicHelper;
import io.nem.symbol.automationHelpers.helper.sdk.MultisigAccountHelper;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.account.MultisigAccountInfo;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.mosaic.MosaicInfo;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultisigAccountStateProof extends StateProofBaseTest<MultisigAccountInfo> {

    public MultisigAccountStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) wants to verify multisig account state on the blockchain$")
    public void VerifyMultisigState(final String username) {
        waitForLastTransactionToComplete();
        final Address address = resolveRecipientAddress(username);
        final MultisigAccountInfo multisigAccountInfo = new AccountHelper(getTestContext())
                .getMultisigAccountWithRetry(address);
        final StateMerkleProof<MultisigAccountInfo> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .multisig(multisigAccountInfo)
                        .blockingFirst();
        logStateInfo(stateMerkleProof, toStateHash(multisigAccountInfo.serialize()));
        assertTrue("Mulitig account state not valid: " + multisigAccountInfo.getAccountAddress().plain() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Mosaic state hash did not match for account : " + multisigAccountInfo.getAccountAddress().plain() + " hash: " + stateMerkleProof.getStateHash(),
                toStateHash(multisigAccountInfo.serialize()),
                stateMerkleProof.getLeafValue().get());
    }
}

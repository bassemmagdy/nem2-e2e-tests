package io.nem.symbol.automation.state_proof;

import cucumber.api.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.SecretLockHelper;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.namespace.NamespaceInfo;
import io.nem.symbol.sdk.model.state.StateMerkleProof;
import io.nem.symbol.sdk.model.transaction.SecretLockInfo;
import io.nem.symbol.sdk.model.transaction.SecretLockTransaction;
import io.nem.symbol.sdk.model.transaction.TransactionType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecretLockStateProof extends StateProofBaseTest<NamespaceInfo> {

    public SecretLockStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) wants to verify secret lock state on the blockchain$")
    public void VerifySecretLockState(final String username) {
        final SecretLockTransaction secretLockTransaction =
                getTestContext().<SecretLockTransaction>findTransaction(TransactionType.SECRET_LOCK).get();
        final Account account = getUser(username);
        final SecretLockInfo secretLockInfo = new SecretLockHelper(getTestContext())
                .SearchSecretLock(account.getAddress(), secretLockTransaction.getSecret());
        getTestContext().getLogger().LogError("Secret lock hash: " + secretLockInfo.getCompositeHash());
        final StateMerkleProof<SecretLockInfo> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .secretLock(secretLockInfo)
                        .blockingFirst();
        assertTrue("Secret lock state state not valid: " + secretLockInfo.getCompositeHash() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Secret lock state hash did not match: " + secretLockInfo.getCompositeHash() + " hash: " + stateMerkleProof.getStateHash(),
                toStateHash(secretLockInfo.serialize()),
                stateMerkleProof.getLeafValue().get());
    }
}
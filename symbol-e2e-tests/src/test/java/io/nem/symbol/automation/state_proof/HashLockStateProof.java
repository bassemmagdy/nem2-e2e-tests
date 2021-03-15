package io.nem.symbol.automation.state_proof;

import cucumber.api.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.HashLockHelper;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.namespace.NamespaceInfo;
import io.nem.symbol.sdk.model.state.StateMerkleProof;
import io.nem.symbol.sdk.model.transaction.HashLockInfo;
import io.nem.symbol.sdk.model.transaction.HashLockTransaction;
import io.nem.symbol.sdk.model.transaction.TransactionType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HashLockStateProof extends StateProofBaseTest<NamespaceInfo> {

    public HashLockStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) wants to verify used hash lock state on the blockchain$")
    public void VerifyUsedHashLockState(final String username) {
        waitForLastTransactionToComplete();
        verifyHashLockState(username);
    }

    @Then("^(\\w+) wants to verify unused hash lock state on the blockchain$")
    public void VerifyUnusedHashLockState(final String username) {
        verifyHashLockState(username);
    }

    private void verifyHashLockState(final String username) {
        final HashLockTransaction hashLockTransaction =
                getTestContext().<HashLockTransaction>findTransaction(TransactionType.HASH_LOCK).get();
        final HashLockInfo hashLockInfo = new HashLockHelper(getTestContext()).getHashLock(hashLockTransaction.getHash());
        final StateMerkleProof<HashLockInfo> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .hashLock(hashLockInfo)
                        .blockingFirst();
        logStateInfo(stateMerkleProof, toStateHash(hashLockInfo.serialize()));
        assertTrue("Hash lock state not valid: " + hashLockInfo.getHash() + " state hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Hash lock state did not match for hash : " + hashLockInfo.getHash() + " state hash: " + stateMerkleProof.getStateHash(),
                toStateHash(hashLockInfo.serialize()),
                stateMerkleProof.getLeafValue().get());
    }
}
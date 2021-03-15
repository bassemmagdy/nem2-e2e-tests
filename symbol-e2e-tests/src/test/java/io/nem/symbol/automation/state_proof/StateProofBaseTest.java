package io.nem.symbol.automation.state_proof;

import io.nem.symbol.automation.common.BaseTest;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.core.crypto.Hashes;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

public class StateProofBaseTest<T> extends BaseTest {

    public StateProofBaseTest(final TestContext testContext) {
        super(testContext);
    }

    protected String toStateHash(final AccountInfo state) {
        return toStateHash(state.serialize());
    }

    protected String toStateHash(final byte[] bytes) {
        return ConvertUtils.toHex(Hashes.sha3_256(bytes));
    }

    protected void logStateInfo(final StateMerkleProof<?> stateMerkleProof, final String stateHash) {
        getTestContext().getLogger().LogError("Expected state hash: " + stateHash);
        getTestContext().getLogger().LogError("Actual state hash: " + stateMerkleProof.getStateHash());
        getTestContext().getLogger().LogError("Root hash: " + stateMerkleProof.getRootHash().orElse("Not found"));
        getTestContext().getLogger().LogError("Leaf state hash: " + stateMerkleProof.getLeafValue().orElse("Not Found"));
        getTestContext().getLogger().LogError("Raw value:" + stateMerkleProof.getRaw());
    }
}

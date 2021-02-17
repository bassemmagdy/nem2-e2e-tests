package io.nem.symbol.automation.state_proof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.cucumber.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.MosaicAddressRestrictionHelper;
import io.nem.symbol.automationHelpers.helper.sdk.MosaicGlobalRestrictionHelper;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.restriction.MosaicRestriction;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

public class MosaicRestrictionStateProof extends StateProofBaseTest<AccountInfo> {

    public MosaicRestrictionStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) verify mosaic global restriction state for \"(\\w+)\" in the blockchain$")
    public void VerifyMosaicRestrictionState(final String username, final String assetName) {
        waitForLastTransactionToComplete();
        final Account account = getUser(username);
        final MosaicId mosaicId = resolveMosaicId(assetName);
        final MosaicRestriction<?> restriction = new MosaicGlobalRestrictionHelper(getTestContext()).getMosaicRestriction(mosaicId);
        final StateMerkleProof<MosaicRestriction<?>> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .mosaicRestriction(restriction)
                        .blockingFirst();
        logStateInfo(stateMerkleProof, toStateHash(restriction.serialize()));
        assertTrue("Mosaic restriction state not valid: " + restriction.getCompositeHash() + " state hash: "
                + stateMerkleProof.getStateHash() + " leaf value: "
                + stateMerkleProof.getLeafValue().get(), stateMerkleProof.isValid());
        assertEquals(
                "Mosaic restriction composite hash : " + restriction.getCompositeHash() + " state hash: " + stateMerkleProof.getStateHash(),
                toStateHash(restriction.serialize()),
                stateMerkleProof.getLeafValue().get());
    }

    @Then("^(\\w+) verify mosaic address restriction state for \"(\\w+)\" in the blockchain$")
    public void VerifyMosaicAddressRestrictionState(final String username, final String assetName) {
        waitForLastTransactionToComplete();
        final Account account = getUser(username);
        final MosaicId mosaicId = resolveMosaicId(assetName);
        final MosaicRestriction<?> restriction = new MosaicAddressRestrictionHelper(getTestContext()).getMosaicRestriction(mosaicId, account.getAddress());
        final StateMerkleProof<MosaicRestriction<?>> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .mosaicRestriction(restriction)
                        .blockingFirst();
        logStateInfo(stateMerkleProof, toStateHash(restriction.serialize()));
        assertTrue("Mosaic address restriction state not valid: " + restriction.compositeHash + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Mosaic address restriction state hash did not match for account : " + account.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(),
                toStateHash(restriction.serialize()),
                stateMerkleProof.getLeafValue().get());
    }
}

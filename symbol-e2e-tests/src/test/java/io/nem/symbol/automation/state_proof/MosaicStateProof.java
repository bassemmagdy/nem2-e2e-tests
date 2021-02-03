package io.nem.symbol.automation.state_proof;

import cucumber.api.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.MosaicHelper;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.mosaic.MosaicInfo;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MosaicStateProof extends StateProofBaseTest<MosaicInfo> {

    public MosaicStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) wants to verify \"(.*)\" state on the blockchain$")
    public void VerifyMosaicState(final String username, final String assetName) {
        final MosaicId mosaicId = resolveMosaicId(assetName);
        final MosaicInfo mosaicInfo = new MosaicHelper(getTestContext()).getMosaic(mosaicId);
        final StateMerkleProof<MosaicInfo> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .mosaic(mosaicInfo)
                        .blockingFirst();
        logStateInfo(stateMerkleProof, toStateHash(mosaicInfo.serialize()));
        assertTrue("Mosaic state not valid: " + mosaicInfo.getMosaicId().getIdAsHex() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Mosaic state hash did not match for account : " + mosaicInfo.getMosaicId().getIdAsHex() + " hash: " + stateMerkleProof.getStateHash(),
                toStateHash(mosaicInfo.serialize()),
                stateMerkleProof.getLeafValue().get());
    }
}

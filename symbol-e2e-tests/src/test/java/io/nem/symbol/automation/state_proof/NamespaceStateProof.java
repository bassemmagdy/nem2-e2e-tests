package io.nem.symbol.automation.state_proof;

import cucumber.api.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.MosaicHelper;
import io.nem.symbol.automationHelpers.helper.sdk.NamespaceHelper;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.mosaic.MosaicInfo;
import io.nem.symbol.sdk.model.namespace.NamespaceId;
import io.nem.symbol.sdk.model.namespace.NamespaceInfo;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NamespaceStateProof extends StateProofBaseTest<NamespaceInfo> {

    public NamespaceStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) wants to verify namespace \"(.*)\" state on the blockchain$")
    public void VerifyNamespaceState(final String username, final String assetName) {
        final String resolveName = resolveNamespaceName(assetName);
        final NamespaceId namespaceId = resolveNamespaceIdFromName(assetName);
        final NamespaceInfo namespaceInfo = new NamespaceHelper(getTestContext()).getNamesapceInfo(namespaceId);
        final StateMerkleProof<NamespaceInfo> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .namespace(namespaceInfo)
                        .blockingFirst();
        assertTrue("Namespace state not valid: " + namespaceInfo.getId().getIdAsHex() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
//        assertEquals(
//                "Mosaic state hash did not match for account : " + namespaceInfo.getId().getIdAsHex() + " hash: " + stateMerkleProof.getStateHash(),
//                toStateHash(namespaceInfo.serialize()),
//                stateMerkleProof.getLeafValue().get());
    }
}

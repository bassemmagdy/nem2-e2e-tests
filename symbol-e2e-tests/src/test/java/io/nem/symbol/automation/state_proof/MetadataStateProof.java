package io.nem.symbol.automation.state_proof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.apache.commons.lang3.tuple.Pair;

import io.cucumber.java.en.Then;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.AccountHelper;
import io.nem.symbol.sdk.api.MetadataRepository;
import io.nem.symbol.sdk.api.MetadataSearchCriteria;
import io.nem.symbol.sdk.infrastructure.StateProofServiceImpl;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.metadata.Metadata;
import io.nem.symbol.sdk.model.metadata.MetadataType;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.mosaic.UnresolvedMosaicId;
import io.nem.symbol.sdk.model.namespace.NamespaceId;
import io.nem.symbol.sdk.model.state.StateMerkleProof;

public class MetadataStateProof extends StateProofBaseTest<AccountInfo> {

    public MetadataStateProof(final TestContext testContext) {
        super(testContext);
    }

    @Then("^(\\w+) verify the state of document \"(.*)\" attached by (\\w+)$")
    public void VerifyAccountMetadataState(final String username, final String documentName, final String source) {
        waitForLastTransactionToComplete();
        final Account targetAccount = getUser(username);
        final Account sourceAccount = getUser(source);
        final AccountInfo accountInfo =
                new AccountHelper(getTestContext()).getAccountInfo(targetAccount.getAddress());
        final Pair<BigInteger, String> documentInfoKey = getDocumentInfo(documentName);
        final Metadata metadata = getMetadata(targetAccount.getAddress(), sourceAccount.getAddress(), documentInfoKey.getKey(), BigInteger.ZERO);
        final StateMerkleProof<Metadata> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .metadata(metadata)
                        .blockingFirst();
        logStateInfo(stateMerkleProof, toStateHash(metadata.serialize()));
        assertTrue("Account state not valid: " + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Account state hash did not match for account : " + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(),
                toStateHash(metadata.serialize()),
                stateMerkleProof.getLeafValue().get());
    }

    @Then("^(\\w+) verify the state of document \"(\\w+)\" attached to namespace \"(\\w+)\" by (\\w+)$")
    public void VerifyNamespaceMetadataDocument(
            final String sourceName, final String documentName, final String namespaceName, final String targetName) {
        waitForLastTransactionToComplete();
        final Account sourceAccount = getUser(sourceName);
        final Account targetAccount = getUser(targetName);
        final NamespaceId targetId = resolveNamespaceIdFromName(namespaceName);
        final AccountInfo accountInfo =
                new AccountHelper(getTestContext()).getAccountInfo(sourceAccount.getAddress());
        final Pair<BigInteger, String> documentInfoKey = getDocumentInfo(documentName);
        final Metadata metadata = getMetadata(sourceAccount.getAddress(), targetAccount.getAddress(), documentInfoKey.getKey(), targetId);
        final StateMerkleProof<Metadata> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .metadata(metadata)
                        .blockingFirst();
        assertTrue("Account state not valid: " + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Account state hash did not match for account : " + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(),
                toStateHash(metadata.serialize()),
                stateMerkleProof.getLeafValue().get());
    }

    @Then("^(\\w+) verify the state of document \"(\\w+)\" attached to asset \"(\\w+)\" by (\\w+)$")
    public void VerifyMosaicMetadataDocument(
            final String sourceName, final String documentName, final String mosaicName, final String targetName) {
        waitForLastTransactionToComplete();
        final Account sourceAccount = getUser(sourceName);
        final Account targetAccount = getUser(targetName);
        final MosaicId targetId = resolveMosaicId(targetName, mosaicName);
        final AccountInfo accountInfo =
                new AccountHelper(getTestContext()).getAccountInfo(sourceAccount.getAddress());
        final Pair<BigInteger, String> documentInfoKey = getDocumentInfo(documentName);
        final Metadata metadata = getMetadata(sourceAccount.getAddress(), targetAccount.getAddress(), documentInfoKey.getKey(), targetId);
        final StateMerkleProof<Metadata> stateMerkleProof =
                new StateProofServiceImpl(getTestContext().getRepositoryFactory())
                        .metadata(metadata)
                        .blockingFirst();
        assertTrue("Account state not valid: " + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(), stateMerkleProof.isValid());
        assertEquals(
                "Account state hash did not match for account : " + accountInfo.getAddress().plain() + " hash: " + stateMerkleProof.getStateHash(),
                toStateHash(metadata.serialize()),
                stateMerkleProof.getLeafValue().get());
    }

    protected Metadata getMetadata(Address targetAddress, Address sourceAddress, BigInteger scopedMetadataKey, BigInteger targetId) {
        final MetadataRepository metadataRepository =
                getTestContext().getRepositoryFactory().createMetadataRepository();
        return metadataRepository.search(new MetadataSearchCriteria().metadataType(MetadataType.ACCOUNT).targetAddress(
                targetAddress).scopedMetadataKey(scopedMetadataKey).sourceAddress(sourceAddress))
                .blockingFirst().getData().get(0);
    }

    protected Metadata getMetadata(
            Address targetAddress,
            Address sourceAddress,
            BigInteger scopedMetadataKey,
            UnresolvedMosaicId targetId) {
        final MetadataRepository metadataRepository =
                getTestContext().getRepositoryFactory().createMetadataRepository();
        return metadataRepository
                .search(
                        new MetadataSearchCriteria()
                                .metadataType(MetadataType.MOSAIC)
                                .targetAddress(targetAddress)
                                .scopedMetadataKey(scopedMetadataKey)
                                .sourceAddress(sourceAddress)
                                .targetId(new MosaicId(targetId.getId())))
                .blockingFirst()
                .getData()
                .stream()
                .findAny()
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "TargetId: "
                                                + targetId.getId()
                                                + " was not found for target"
                                                + " address: "
                                                + targetAddress.plain()
                                                + " with meta key: "
                                                + scopedMetadataKey.longValue()));
    }

    protected Metadata getMetadata(Address targetAddress,
                                   Address sourceAddress,
                                   BigInteger scopedMetadataKey,
                                   NamespaceId targetId) {
        final MetadataRepository metadataRepository =
                getTestContext().getRepositoryFactory().createMetadataRepository();
        return metadataRepository.search(new MetadataSearchCriteria().metadataType(MetadataType.NAMESPACE).targetAddress(
                targetAddress).scopedMetadataKey(scopedMetadataKey).sourceAddress(sourceAddress).targetId(targetId))
                .blockingFirst().getData().get(0);
    }
}

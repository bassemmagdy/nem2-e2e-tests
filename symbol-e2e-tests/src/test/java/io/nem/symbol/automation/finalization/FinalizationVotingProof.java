package io.nem.symbol.automation.finalization;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.symbol.automation.common.BaseTest;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.BlockChainHelper;
import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.sdk.api.AccountOrderBy;
import io.nem.symbol.sdk.api.AccountSearchCriteria;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.blockchain.ChainInfo;
import io.nem.symbol.sdk.model.finalization.FinalizationProof;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class FinalizationVotingProof extends BaseTest {

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public FinalizationVotingProof(final TestContext testContext) {
        super(testContext);
    }

    @Given("^(\\w+) has a voting node register")
    public void registeredVoting(final String userName) {
    }

    @When("^(\\w+) wants to know if node voted in the latest epoch$")
    public void verifyEpochFinalizationProof(final String userName) {
        final ChainInfo chainInfo = new BlockChainHelper(getTestContext()).getBlockchainInfo();
        final FinalizationProof finalizationProof = getTestContext().getRepositoryFactory()
                .createFinalizationRepository()
                .getFinalizationProofAtEpoch(chainInfo.getLatestFinalizedBlock().getFinalizationEpoch()).blockingFirst();
        finalizationProof.getFinalizationEpoch();
        verifyFinalizationProof(finalizationProof);
    }

    @When("^(\\w+) wants to know if node voted in the latest height$")
    public void verifyHeightFinalizationProof(final String userName) {
        final ChainInfo chainInfo = new BlockChainHelper(getTestContext()).getBlockchainInfo();
        final FinalizationProof finalizationProof = getTestContext().getRepositoryFactory()
                .createFinalizationRepository()
                .getFinalizationProofAtHeight(chainInfo.getLatestFinalizedBlock().getHeight()).blockingFirst();
        verifyFinalizationProof(finalizationProof);
    }

    private void verifyFinalizationProof(final FinalizationProof finalizationProof) {
        List<String> voters = getListOfVotingNodes();
        boolean found = finalizationProof.getMessageGroups().parallelStream().allMatch(p -> p.getSignatures().stream().allMatch(s -> voters.contains(s.getRoot().getParentPublicKey())));
        assertTrue("Not all the nodes voted: ", found);
    }

    List<String> getListOfVotingNodes() {
        List<AccountInfo> accountInfos = getTestContext().getRepositoryFactory().createAccountRepository()
                .search(new AccountSearchCriteria().orderBy(AccountOrderBy.BALANCE).mosaicId(getTestContext()
                        .getRepositoryFactory().getNetworkCurrency().blockingFirst().getMosaicId().get())
                        .pageSize(100)).blockingFirst().getData();
        return accountInfos.stream().filter(a -> a.isHighValue() && !a.getSupplementalAccountKeys().getVoting().isEmpty())
                .map(a -> a.getSupplementalAccountKeys().getVoting().get(0).getPublicKey()).collect(Collectors.toList());
    }

}
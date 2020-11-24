/**
 * ** Copyright (c) 2016-present, ** Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights
 * reserved. ** ** This file is part of Catapult. ** ** Catapult is free software: you can
 * redistribute it and/or modify ** it under the terms of the GNU Lesser General Public License as
 * published by ** the Free Software Foundation, either version 3 of the License, or ** (at your
 * option) any later version. ** ** Catapult is distributed in the hope that it will be useful, **
 * but WITHOUT ANY WARRANTY; without even the implied warranty of ** MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the ** GNU Lesser General Public License for more details. ** ** You
 * should have received a copy of the GNU Lesser General Public License ** along with Catapult. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package io.nem.symbol.automation.example;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.*;
import io.nem.symbol.catapult.builders.AddressDto;
import io.nem.symbol.catapult.builders.GeneratorUtils;
import io.nem.symbol.core.crypto.Hashes;
import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.core.crypto.VotingKey;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.core.utils.ExceptionUtils;
import io.nem.symbol.sdk.api.AccountRepository;
import io.nem.symbol.sdk.infrastructure.BinarySerializationImpl;
import io.nem.symbol.sdk.infrastructure.directconnect.DirectConnectRepositoryFactoryImpl;
import io.nem.symbol.sdk.infrastructure.directconnect.network.SocketClient;
import io.nem.symbol.sdk.infrastructure.directconnect.packet.Packet;
import io.nem.symbol.sdk.infrastructure.directconnect.packet.PacketType;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.account.PublicAccount;
import io.nem.symbol.sdk.model.mosaic.ResolvedMosaic;
import io.nem.symbol.sdk.model.network.NetworkType;
import io.nem.symbol.sdk.model.transaction.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ExampleSteps {
  final TestContext testContext;
  final String recipientAccountKey = "RecipientAccount";
  final String signerAccountInfoKey = "SignerAccountInfo";

  public ExampleSteps(final TestContext testContext) {
    this.testContext = testContext;
  }

  @Given("^Jill has an account on the Nem platform$")
  public void jill_has_an_account_on_the_nem_platform() {
    NetworkType networkType = testContext.getNetworkType();
    testContext
        .getScenarioContext()
        .setContext(recipientAccountKey, Account.generateNewAccount(networkType));
  }

  private void writePacket(final PacketType packetType, final byte[] transactionBytes) {
    ExceptionUtils.propagateVoid(
        () -> {
          final ByteBuffer ph = Packet.CreatePacketByteBuffer(packetType, transactionBytes);
          ((DirectConnectRepositoryFactoryImpl) testContext.getRepositoryFactory())
              .getContext()
              .getApiNodeContext()
              .getAuthenticatedSocket()
              .getSocketClient()
              .Write(ph);
        });
  }

  private ByteBuffer readBytes() {
    final SocketClient socketClient =
        ((DirectConnectRepositoryFactoryImpl) testContext.getRepositoryFactory())
            .getContext()
            .getApiNodeContext()
            .getAuthenticatedSocket()
            .getSocketClient();
    return ExceptionUtils.propagate(
        () -> {
          final int size = socketClient.Read(4).getInt();
          return socketClient.Read(size - 4);
        });
  }

  private void resubmitTxs(final String path, final Account signer) {
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        try {
          DataInputStream fis = new DataInputStream(new FileInputStream(file));
          int source = Integer.reverseBytes(fis.readInt());
          ByteBuffer publicbytes = GeneratorUtils.readByteBuffer(fis, 32);
          TransactionHelper transactionHelper = new TransactionHelper(testContext);
          byte[] bytes = new byte[fis.available()];
          fis.readFully(bytes);
          int bytesRemaining = bytes.length;
          byte[] remainingBytes = bytes;
          while (bytesRemaining > 0) {
            TransactionFactory transaction =
                BinarySerializationImpl.INSTANCE.deserializeToFactory(remainingBytes);
            SignedTransaction signedTransaction =
                signer.sign(
                    transaction.deadline(testContext.getDefaultDeadline()).build(),
                    testContext.getSymbolConfig().getGenerationHashSeed());
            transactionHelper.announceTransaction(signedTransaction);
            testContext
                .getLogger()
                .LogError("Transaction Info:" + CommonHelper.toString(signedTransaction));
            bytesRemaining -= transaction.getSize();
            remainingBytes = new byte[bytesRemaining];
            System.arraycopy(
                bytes, bytes.length - bytesRemaining, remainingBytes, 0, bytesRemaining);
          }
        } catch (Exception ex) {
          System.out.println(ex.getMessage());
        }
      }
    }
  }

  private void breakMaximizeFee(final Account sender) {
    final TransferHelper transferHelper = new TransferHelper(testContext);
    final Account B = Account.generateNewAccount(testContext.getNetworkType());
    final Account C = Account.generateNewAccount(testContext.getNetworkType());
    transferHelper.submitTransferAndWait(
        sender,
        B.getAddress(),
        Arrays.asList(testContext.getNetworkCurrency().createRelative(BigInteger.valueOf(100))));

    transferHelper
        .withMaxFee(BigInteger.valueOf(100000))
        .createTransferAndAnnounce(
            sender,
            B.getAddress(),
            Arrays.asList(testContext.getNetworkCurrency().createRelative(BigInteger.valueOf(100))));
    new TransferHelper(testContext)
        .createTransferAndAnnounce(
            B,
            C.getAddress(),
            Arrays.asList(testContext.getNetworkCurrency().createRelative(BigInteger.valueOf(1))));
    transferHelper
        .withMaxFee(BigInteger.valueOf(10000000))
        .createTransferAndAnnounce(
            sender,
            B.getAddress(),
            Arrays.asList(testContext.getNetworkCurrency().createRelative(BigInteger.valueOf(100))));
  }

  protected void waitForNextBlockChainHeight() {
    final BlockChainHelper blockChainHelper = new BlockChainHelper(testContext);
    final long blocksToWait = blockChainHelper.getBlockchainHeight().longValue() + 1;
    while (blockChainHelper.getBlockchainHeight().longValue() <= blocksToWait) {
      ExceptionUtils.propagateVoid(() -> Thread.sleep(1000));
    }
  }

  private void breakMaxTransactionFee(final Account sender) {
    final TransferHelper transferHelper = new TransferHelper(testContext);
    final Account B = Account.generateNewAccount(testContext.getNetworkType());
    final Account C = Account.generateNewAccount(testContext.getNetworkType());

    final List<Runnable> runnables = new ArrayList<>();
    final NetworkType networkType = testContext.getNetworkType();
    runnables.add(() -> {
      final List<Transaction> txList = new ArrayList<>();
      for (int i = 0; i <= 100; i++) {
      final Account recipientAccount = Account.generateNewAccount(networkType);
        txList.add(
              TransferTransactionFactory.create(
                      networkType,
                      testContext.getDefaultDeadline(),
                      recipientAccount.getAddress(),
                      Arrays.asList(testContext.getNetworkCurrency().createAbsolute(1))).build().toAggregate(sender.getPublicAccount()));
}

      final AggregateTransaction aggregateTransaction = new AggregateHelper(testContext)
              .createAggregateCompleteTransaction(txList, 1);

      new TransactionHelper(testContext).signAndAnnounceTransactionAndWait(sender, () -> aggregateTransaction);
    });

    ExecutorService es = Executors.newCachedThreadPool();
    waitForNextBlockChainHeight();
    for (int i = 0; i < 60; i++) {
      for (final Runnable runnable : runnables) {
        es.execute(runnable);
      }
    }

    ExceptionUtils.propagateVoid(() -> es.awaitTermination(2, TimeUnit.MINUTES));

}

    @When("^Bob transfer (\\d+) XEM to Jill$")
  public void bob_transfer_xem_to_jill(int transferAmount)
      throws InterruptedException, ExecutionException {
    final Account signerAccount = testContext.getDefaultSignerAccount();
    final AccountRepository accountRepository =
        testContext.getRepositoryFactory().createAccountRepository();
    final AccountInfo signerAccountInfo =
        accountRepository.getAccountInfo(signerAccount.getAddress()).toFuture().get();
    testContext.getScenarioContext().setContext(signerAccountInfoKey, signerAccountInfo);

//      byte[] state = signerAccountInfo.serialize();
//      String hash = ConvertUtils.toHex(Hashes.sha3_256(state));
//      AddressDto address = SerializationUtils.toAddressDto(signerAccountInfo.getAddress());
//      writePacket(PacketType.NODE_DISCOVERY_PULL_PING, new byte[0]);
//      final ByteBuffer response0 = readBytes();
//      writePacket(PacketType.ACCOUNT_STATE_PATH, address.serialize());
//      final ByteBuffer response = readBytes();


    Account account = Account.createFromPrivateKey("A54A64B90BC6C8E90E62B7777C37379005F601C69A0090FF543251F1C21E84C1",
            testContext.getNetworkType());
    PublicAccount publicAccount = PublicAccount.createFromPublicKey("07BD1120EFF5CD9D7EAAF60B9FFE7CDE1DB44F768C859235C5AD0255D98295F5",
            testContext.getNetworkType());
    Address addressp = publicAccount.getAddress();
    String adds = addressp.plain();
    final Address dest = Address.createFromRawAddress("TAY5UVI3HUW5GYMBQI6XPIZKVNAOOOJMHKHCSNQ");
    new TransferHelper(testContext)
            .withMaxFee(BigInteger.valueOf(100000))
            .submitTransferAndWait(
                    signerAccount,
                    dest,
                    Arrays.asList(testContext.getNetworkCurrency().createRelative(BigInteger.valueOf(11029999))),
                    null);

      breakMaxTransactionFee(signerAccount);
    breakMaximizeFee(signerAccount);
    // resubmitTxs("/Users/tbdev/test/hang/files", signerAccount);
    //        final String document = CommonHelper.getRandonStringWithMaxLength(500);
    //        final BigInteger documentKey = ConvertUtils.toUnsignedBigInteger(new
    // Random().nextLong());
    //        final AccountMetadataTransaction accountMetadataTransaction =
    //                AccountMetadataTransactionFactory.create(testContext.getNetworkType(),
    // signerAccount.getAddress(), documentKey, document).build();
    //        final AggregateTransaction aggregateCompleteTransaction =
    //                new AggregateHelper(testContext)
    //                        .createAggregateTransaction(
    //                                false,
    //                                Arrays.asList(
    //                                        accountMetadataTransaction
    //
    // .toAggregate(signerAccount.getPublicAccount())),
    //                                0);
    //        final AggregateTransaction aggregateTransaction =
    //                new
    // TransactionHelper(testContext).signAndAnnounceTransactionAndWait(signerAccount, () ->
    // aggregateCompleteTransaction);
    //        final String updateDocument = new StringBuilder(document).reverse().toString();
    //        final MetadataTransaction metadataTransaction = new
    // MetadataTransactionServiceImpl(testContext.getRepositoryFactory())
    //                .createAccountMetadataTransactionFactory(
    //                        signerAccount.getAddress(), documentKey, updateDocument,
    // signerAccount.getAddress())
    //                .blockingFirst().build();
    //        assertEquals(updateDocument.length(), metadataTransaction.getValue().length());
    final Account harvester =
        Account.createFromPrivateKey(
            "E05D57098B60948F3DB1218BA273635C76FF6D76A1B99B5A273DBC4EBE7CC890",
            testContext.getNetworkType());
    final TransferTransaction transferTransaction4 =
        new TransferHelper(testContext)
            .submitTransferAndWait(
                signerAccount,
                harvester.getAddress(),
                Arrays.asList(
                    testContext.getNetworkCurrency().createRelative(BigInteger.valueOf(1000))),
                null);

    ////      final Account remoteAccount =
    // Account.createFromPrivateKey("3038D4C217313715F6D279657F8335D820E6682A08861D585115D0CC73643212",
    ////              testContext.getNetworkType());
    //        final Account remoteAccount =
    // Account.generateNewAccount(testContext.getNetworkType());
    //        final Account vrfAccount = Account.generateNewAccount(testContext.getNetworkType());
    //        AccountKeyLinkTransaction accountLinkTransaction = new
    // AccountKeyLinkHelper(testContext).submitAccountKeyLinkAndWait(harvester,
    //                remoteAccount.getPublicAccount(), LinkAction.LINK);
    //    final VrfKeyLinkTransaction vrfKeyLinkTransaction = new
    // VrfKeyLinkHelper(testContext).submitVrfKeyLinkTransactionAndWait(harvester,
    //            vrfAccount.getPublicAccount().getPublicKey(), LinkAction.LINK);

    final PublicKey nodePublicKey =
        PublicKey.fromHexString("9364AD296DDA22DFD95C31ED57980FDB75A5E1B57D4A362A4AE31B38B0218994");
    final NodeKeyLinkTransaction nodeKeyLinkTransaction =
        new NodeKeyLinkHelper(testContext)
            .submitNodeKeyLinkTransactionAndWait(harvester, nodePublicKey, LinkAction.LINK);

    final String voting =
        "CA23403049BBCDC4D25E55CE2F16432635D080082129271BAC88E254903CBA1F00000000000000000000000000000000";
    final ByteBuffer byteBuffer = ByteBuffer.allocate(48).put(ConvertUtils.getBytes(voting));
    final VotingKey votingKey = new VotingKey(byteBuffer.array());
    final VotingKeyLinkTransaction votingKeyLinkTransaction =
        new VotingKeyLinkHelper(testContext)
            .submitVotingKeyLinkTransactionAndWait(
                harvester, votingKey, 100, 2000, LinkAction.LINK);
    votingKeyLinkTransaction.getEndEpoch();
    //        final TransferTransaction transferTransaction =
    //
    // TransferTransactionFactory.createPersistentDelegationRequestTransaction(testContext.getNetworkType(),
    //                        remoteAccount.getKeyPair().getPrivateKey(),
    // nodePublicKey).maxFee(BigInteger.valueOf(63300)).build();
    //        TransferTransaction transferTransaction2 = new
    // TransactionHelper(testContext).signAndAnnounceTransactionAndWait(harvester,
    //                () -> transferTransaction);
    //        vrfAccount.getAddress();
    /*
        final NetworkType networkType = testContext.getNetworkType();
        final Account recipientAccount1 =
            testContext.getScenarioContext().<Account>getContext(recipientAccountKey);

        final BigInteger blockToRollback =
            testContext
                .getRepositoryFactory()
                .createChainRepository()
                .getBlockchainHeight()
                .blockingSingle()
                .subtract(
                    testContext.getSymbolConfig().getMaxRollbackBlocks().subtract(BigInteger.TEN));
    //      String reverse = ConvertUtils.reverseHexString("75FF75FF75FF75FF75FF75FF66FF66FF66FF66FF66FF66FF66FF66FF");
    //      Listener listener = testContext.getRepositoryFactory().createListener();
    //      listener.open();
    //      BlockInfo blockInfo = listener.newBlock().take(1).blockingFirst();


    //      ((DirectConnectRepositoryFactoryImpl) testContext.getRepositoryFactory()).getContext().getApiNodeContext().getAuthenticatedSocket
    //      ().getSocketClient().close();


        // hang
        //		int ii = 0;
        //		do {
        //		final MosaicId mosaicId = new
        // NamespaceHelper(testContext).getLinkedMosaicId(NetworkHarvestMosaic.NAMESPACEID);
        //		final Account harvestAccount =
        // Account.createFromPrivateKey("9D3753505B289F238D3B012B3A2EF975C4FBC8A49B687E25F4DC7184B96FC05E",
        //				testContext.getNetworkType());
        //		final TransferHelper transferHelper = new TransferHelper(testContext);
        //		final TransferTransaction tx = transferHelper.submitTransferAndWait(signerAccount,
        // harvestAccount.getAddress(),
        //				Arrays.asList(NetworkCurrencyMosaic.createRelative(BigInteger.valueOf(100000))),
        // null);
        //		final AccountInfo harvestAccountInfo = new
        // AccountHelper(testContext).getAccountInfo(harvestAccount.getAddress());
        //		final Mosaic harvest =
        //				harvestAccountInfo.getMosaics().stream().filter(m -> m.getId().getIdAsLong() ==
        // mosaicId.getIdAsLong()).findFirst().orElseThrow(() -> new IllegalArgumentException("Not
        // found"));
        //		final long minHarvesterBalance = 500;
        //      final TransferTransaction transferTransaction =
        //          transferHelper.createTransferTransaction(
        //              recipientAccount1.getAddress(),
        //              Arrays.asList(
        //                  new Mosaic(
        //                      harvest.getId(),
        //                      harvest.getAmount().subtract(BigInteger.valueOf(minHarvesterBalance -
        // 5)))),
        //              null);
        //      final TransferTransaction transferTransaction2 =
        //          transferHelper.createTransferTransaction(
        //              recipientAccount1.getAddress(),
        //              Arrays.asList(
        //                  new Mosaic(harvest.getId(), BigInteger.valueOf(minHarvesterBalance - 5))),
        //              null);
        //
        ////		final TransferTransaction transferTransaction =
        // transferHelper.submitTransferAndWait(harvestAccount,
        ////				recipientAccount1.getAddress(), Arrays.asList(new Mosaic(harvest.getId(),
        ////						harvest.getAmount().subtract(BigInteger.valueOf(minHarvesterBalance - 5)))),
        // null);
        ////		final TransferTransaction transferTransaction2 =
        // transferHelper.submitTransferAndWait(harvestAccount,
        ////				recipientAccount1.getAddress(), Arrays.asList(new Mosaic(harvest.getId(),
        ////						BigInteger.valueOf(minHarvesterBalance - 5))), null);
        //		final AggregateTransaction aggregateTransaction =
        //				new
        // AggregateHelper(testContext).createAggregateCompleteTransaction(Arrays.asList(transferTransaction.toAggregate(harvestAccount.getPublicAccount()),
        //						transferTransaction2.toAggregate(harvestAccount.getPublicAccount())));
        //			final TransferTransaction tx2 = transferHelper.submitTransferAndWait(signerAccount,
        // recipientAccount1.getAddress(),
        //					Arrays.asList(NetworkCurrencyMosaic.createRelative(BigInteger.valueOf(100000))),
        // null);
        //			new TransactionHelper(testContext).signAndAnnounceTransactionAndWait(harvestAccount, () ->
        // aggregateTransaction);
        //		final AccountInfo resAccountInfo = new
        // AccountHelper(testContext).getAccountInfo(recipientAccount1.getAddress());
        //		final Mosaic resceop =
        //				resAccountInfo.getMosaics().stream().filter(m -> m.getId().getIdAsLong() ==
        // mosaicId.getIdAsLong()).findFirst().orElseThrow(() -> new IllegalArgumentException("Not
        // found"));
        //		final TransferTransaction transferTransaction3 =
        // transferHelper.submitTransferAndWait(recipientAccount1,
        //		harvestAccount.getAddress(), Arrays.asList(resceop), null);
        //		ii++;
        //		} while (ii < 3);
        //
        //    final byte NO_OF_RANDOM_BYTES = 100;
        //    final byte[] randomBytes = new byte[NO_OF_RANDOM_BYTES];
        //    ExceptionUtils.propagateVoid(() ->
        // SecureRandom.getInstanceStrong().nextBytes(randomBytes));
        //    final String proof = Hex.toHexString(randomBytes);
        //    final SecretLockHelper secretLockHelper = new SecretLockHelper(testContext);
        //    final byte[] secretHashBytes =
        //        secretLockHelper.createHash(LockHashAlgorithmType.SHA3_256, randomBytes);
        //    final String secretHash = Hex.toHexString(secretHashBytes);
        //    final SecretLockTransaction secretLockTransaction =
        //        secretLockHelper.createSecretLockTransaction(
        //            testContext.getNetworkCurrency().createRelative(BigInteger.TEN),
        //            BigInteger.valueOf(12000),
        //            LockHashAlgorithmType.SHA3_256,
        //            secretHash,
        //            recipientAccount1.getAddress());
        //    new TransactionHelper(testContext)
        //        .signAndAnnounceTransaction(secretLockTransaction, signerAccount);

        final List<Runnable> runnables = new ArrayList<>();
        /*		runnables.add(() -> {
        			final NamespaceHelper namespaceHelper = new NamespaceHelper(testContext);
        			final NamespaceRegistrationTransaction tx = namespaceHelper.createRootNamespaceAndWait(signerAccount,
        					"te" + CommonHelper.getRandomNamespaceName("tet"),
        					BigInteger.valueOf(1000));
        			testContext.getLogger().LogError("height for Namespace: " + tx.getTransactionInfo().get().getHeight());
        		});
        		runnables.add(() -> {
        */
    /*      final MosaicInfo mosaicInfo = new MosaicHelper(testContext)
    .createMosaic(
            signerAccount,
            true,
            true,
            0,
            BigInteger.valueOf(1000));*/
    /*
    	final MosaicFlags mosaicFlags = MosaicFlags.create(CommonHelper.getRandomNextBoolean(),
    			CommonHelper.getRandomNextBoolean());
    	final SignedTransaction tx = new MosaicHelper(testContext).createExpiringMosaicDefinitionTransactionAndAnnounce(signerAccount,
    			mosaicFlags,
    			CommonHelper.getRandomDivisibility(), BigInteger.valueOf(100));
    	final MosaicDefinitionTransaction txn = new TransactionHelper(testContext).waitForTransactionToComplete(tx);
    	testContext.getLogger().LogError("height for mosaic: " + txn.getTransactionInfo().get().getHeight());
    });
    runnables.add(() -> {
    	final TransactionHelper transactionHelper = new TransactionHelper(testContext);
    	final Account recipientAccount = Account.generateNewAccount(networkType);
    	final TransferTransaction transferTransaction1 =
    			TransferTransactionFactory.create(
    					networkType,
    					recipientAccount.getAddress(),
    					Arrays.asList(
    							new Mosaic(
    									new MosaicId(testContext.getCatCurrencyId()),
    									BigInteger.valueOf(transferAmount))),
    					PlainMessage.create("Welcome To send Automation")).build();

    	final TransferTransaction transferTransaction2 =
    			TransferTransactionFactory.create(
    					networkType,
    					signerAccount.getAddress(),
    					Arrays.asList(
    							new Mosaic(
    									new MosaicId(testContext.getCatCurrencyId()),
    									BigInteger.valueOf(transferAmount))),
    					PlainMessage.create("Welcome To return Automation")).build();
    	final AggregateTransaction aggregateTransaction = new AggregateHelper(testContext)
    			.createAggregateBondedTransaction(Arrays.asList(transferTransaction1.toAggregate(signerAccount.getPublicAccount()),
    					transferTransaction2.toAggregate(recipientAccount.getPublicAccount())));
    	SignedTransaction signedAggregateTransaction = aggregateTransaction.signWith(signerAccount,
    			testContext.getGenerationHash());

    	final BigInteger duration = BigInteger.valueOf(13);
    	final Mosaic mosaic = NetworkCurrencyMosaic.createRelative(BigInteger.valueOf(10));
    	final HashLockTransaction hashLockTransaction = HashLockTransactionFactory.create(networkType, mosaic, duration,
    			signedAggregateTransaction).build();

    	final Transaction tx = transactionHelper.signAndAnnounceTransactionAndWait(signerAccount, () -> hashLockTransaction);
    	transactionHelper.announceAggregateBonded(signedAggregateTransaction);
    	final long sleeptime = CommonHelper.getRandomValueInRange(30000, 60000);
    	testContext.getLogger().LogError("height for lock: " + tx.getTransactionInfo().get().getHeight());
    });*/
    /*
        final TransactionHelper transactionHelper1 = new TransactionHelper(testContext);
        final MosaicHelper mosaicHelper = new MosaicHelper(testContext);
        final MosaicInfo mosaic =
            mosaicHelper.createMosaic(
                signerAccount, MosaicFlags.create(true, true, false), 6, BigInteger.TEN);
        runnables.add(
            () -> {
              final Account recipientAccount = Account.generateNewAccount(networkType);
              final MosaicSupplyChangeTransaction mosaicSupplyChangeTransaction =
                  MosaicSupplyChangeTransactionFactory.create(
                          networkType,
                          mosaic.getMosaicId(),
                          MosaicSupplyChangeActionType.INCREASE,
                          BigInteger.valueOf(CommonHelper.getRandomValueInRange(1, 1000000)))
                      .build();
              final TransactionHelper transactionHelper = new TransactionHelper(testContext);
              transactionHelper.signAndAnnounceTransactionAndWait(
                  signerAccount, () -> mosaicSupplyChangeTransaction);
            });
        runnables.add(
            () -> {
              final Account recipientAccount = Account.generateNewAccount(networkType);
              final MosaicSupplyChangeTransaction mosaicSupplyChangeTransaction =
                  MosaicSupplyChangeTransactionFactory.create(
                          networkType,
                          mosaic.getMosaicId(),
                          MosaicSupplyChangeActionType.INCREASE,
                          BigInteger.valueOf(CommonHelper.getRandomValueInRange(1, 1000000)))
                      .build();
              final TransactionHelper transactionHelper = new TransactionHelper(testContext);
              transactionHelper.signAndAnnounceTransactionAndWait(
                  signerAccount, () -> mosaicSupplyChangeTransaction);
            });
        runnables.add(
            () -> {
              final MosaicSupplyChangeTransaction mosaicSupplyChangeTransaction =
                  MosaicSupplyChangeTransactionFactory.create(
                          networkType,
                          mosaic.getMosaicId(),
                          MosaicSupplyChangeActionType.INCREASE,
                          BigInteger.valueOf(CommonHelper.getRandomValueInRange(1, 1000000)))
                      .build();
              final TransactionHelper transactionHelper = new TransactionHelper(testContext);
              transactionHelper.signAndAnnounceTransactionAndWait(
                  signerAccount, () -> mosaicSupplyChangeTransaction);
            });
        runnables.add(
            () -> {
              final MosaicSupplyChangeTransaction mosaicSupplyChangeTransaction =
                  MosaicSupplyChangeTransactionFactory.create(
                          networkType,
                          mosaic.getMosaicId(),
                          MosaicSupplyChangeActionType.INCREASE,
                          BigInteger.valueOf(CommonHelper.getRandomValueInRange(1, 1000000)))
                      .build();
              final TransactionHelper transactionHelper = new TransactionHelper(testContext);
              transactionHelper.signAndAnnounceTransactionAndWait(
                  signerAccount, () -> mosaicSupplyChangeTransaction);
            });
        ExecutorService es = Executors.newCachedThreadPool();

        for (int i = 0; i < 200; i++) {
          for (final Runnable runnable : runnables) {
            es.execute(runnable);
          }
        }
        es.awaitTermination(2, TimeUnit.MINUTES);
        // runnables.parallelStream().map(r -> r.get()).collect(Collectors.toList());

        final MosaicInfo mosaic1 =
            mosaicHelper.createMosaic(
                signerAccount, MosaicFlags.create(true, true, true), 0, BigInteger.TEN);
        final MosaicGlobalRestrictionTransaction mosaicGlobalRestrictionTransaction1 =
            MosaicGlobalRestrictionTransactionFactory.create(
                    testContext.getNetworkType(),
                    mosaic1.getMosaicId(),
                    BigInteger.ZERO,
                    BigInteger.ONE,
                    MosaicRestrictionType.EQ)
                .build();
        final Transaction tx1 =
            transactionHelper1.signAndAnnounceTransactionAndWait(
                signerAccount, () -> mosaicGlobalRestrictionTransaction1);
        final MosaicInfo mosaic2 =
            mosaicHelper.createMosaic(
                signerAccount, MosaicFlags.create(true, true, true), 0, BigInteger.TEN);
        final MosaicGlobalRestrictionTransaction mosaicGlobalRestrictionTransaction2 =
            MosaicGlobalRestrictionTransactionFactory.create(
                    testContext.getNetworkType(),
                    mosaic2.getMosaicId(),
                    BigInteger.ZERO,
                    BigInteger.TEN,
                    MosaicRestrictionType.EQ)
                .referenceMosaicId(mosaic1.getMosaicId())
                .build();
        final Transaction tx2 =
            transactionHelper1.signAndAnnounceTransactionAndWait(
                signerAccount, () -> mosaicGlobalRestrictionTransaction2);
    */

    /*
        final Runnable runnable = () -> {
          final Account recipientAccount = Account.generateNewAccount(networkType);
        final TransferTransaction transferTransaction =
                TransferTransaction.create(
                        Deadline.create(2, ChronoUnit.HOURS),
                        BigInteger.ZERO,
                        recipientAccount.getAddress(),
                        Arrays.asList(
                                new Mosaic(
                                        new MosaicId(testContext.getCatCurrencyId()),
                                        BigInteger.valueOf(transferAmount))),
                        PlainMessage.create("Welcome To NEM Automation"),
                        networkType);

        final SignedTransaction signedTransaction =
                signerAccount.sign(
                        transferTransaction, testContext.getGenerationHash());
        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        transactionHelper.signAndAnnounceTransactionAndWait(signerAccount, () -> transferTransaction);

        final TransferTransaction transferTransaction1 =
                TransferTransaction.create(
                        Deadline.create(2, ChronoUnit.HOURS),
                        BigInteger.ZERO,
                        recipientAccount.getAddress(),
                        Arrays.asList(
                                new Mosaic(
                                        new MosaicId(testContext.getCatCurrencyId()),
                                        BigInteger.valueOf(transferAmount))),
                        PlainMessage.create("Welcome To send Automation"),
                        networkType);

        final TransferTransaction transferTransaction2 =
                TransferTransaction.create(
                        Deadline.create(2, ChronoUnit.HOURS),
                        BigInteger.ZERO,
                        signerAccount.getAddress(),
                        Arrays.asList(
                                new Mosaic(
                                        new MosaicId(testContext.getCatCurrencyId()),
                                        BigInteger.valueOf(transferAmount))),
                        PlainMessage.create("Welcome To return Automation"),
                        networkType);
          final NamespaceHelper namespaceHelper = new NamespaceHelper(testContext);
          namespaceHelper.createRootNamespaceAndWait(signerAccount,"test" + CommonHelper.getRandomNamespaceName("test"),
                  BigInteger.valueOf(1000));
          final MosaicInfo mosaicInfo = new MosaicHelper(testContext)
                  .createMosaic(
                          signerAccount,
                          true,
                          true,
                          0,
                          BigInteger.valueOf(1000));
        final AggregateTransaction aggregateTransaction = new AggregateHelper(testContext).createAggregateBondedTransaction(Arrays.asList(transferTransaction1.toAggregate(signerAccount.getPublicAccount()),
                        transferTransaction2.toAggregate(recipientAccount.getPublicAccount())));
        SignedTransaction signedAggregateTransaction = aggregateTransaction.signWith(signerAccount,
                testContext.getGenerationHash());

        final BigInteger duration = BigInteger.valueOf(3);
        final Mosaic mosaic = NetworkCurrencyMosaic.createRelative(BigInteger.valueOf(10));
        final HashLockTransaction hashLockTransaction = HashLockTransaction.create(Deadline.create(30, ChronoUnit.SECONDS),
                BigInteger.ZERO, mosaic, duration, signedAggregateTransaction, networkType);

        transactionHelper.signAndAnnounceTransactionAndWait(signerAccount, () -> hashLockTransaction);
        transactionHelper.announceAggregateBonded(signedAggregateTransaction);
        final int sleeptime = CommonHelper.getRandomValueInRange(10000, 30000);
        ExceptionUtils.propagateVoid(()-> Thread.sleep(sleeptime));
        //Thread.sleep(20000);

        final AggregateTransaction aggregateTransactionInfo = (AggregateTransaction)
                new PartialTransactionsCollection(testContext.getCatapultContext()).findByHash(signedAggregateTransaction.getHash(),
                        testContext.getConfigFileReader().getDatabaseQueryTimeoutInSeconds()).get();
        final CosignatureTransaction cosignatureTransaction = CosignatureTransaction.create(aggregateTransactionInfo);
        final CosignatureSignedTransaction cosignatureSignedTransaction = recipientAccount.signCosignatureTransaction(cosignatureTransaction);
        transactionHelper.announceAggregateBondedCosignature(cosignatureSignedTransaction);
        };

    */
    /*    ExecutorService es = Executors.newCachedThreadPool();
    final int size = 400;
    for(int i = 0; i < size; i++) {
      es.execute(runnable);
    }
    es.awaitTermination(2, TimeUnit.MINUTES);*/
    /*
    testContext.addTransaction(transferTransaction);
    testContext.setSignedTransaction(signedTransaction);

    final TransactionRepository transactionRepository =
        new TransactionDao(testContext.getCatapultContext());
    transactionRepository.announce(signedTransaction).toFuture().get();
    testContext.setSignedTransaction(signedTransaction);*/
  }

  @Then("^Jill should have (\\d+) XEM$")
  public void jill_should_have_10_xem(int transferAmount)
      throws InterruptedException, ExecutionException {
    Transaction transaction =
        new TransactionHelper(testContext)
            .getConfirmedTransaction(testContext.getSignedTransaction().getHash());

    final TransferTransaction submitTransferTransaction =
        (TransferTransaction) testContext.getTransactions().get(0);
    final TransferTransaction actualTransferTransaction = (TransferTransaction) transaction;

    assertEquals(
        submitTransferTransaction.getDeadline().getValue(),
        actualTransferTransaction.getDeadline().getValue());
    assertEquals(submitTransferTransaction.getMaxFee(), actualTransferTransaction.getMaxFee());
    if (submitTransferTransaction.getMessage().isPresent()) {
      assertEquals(
          submitTransferTransaction.getMessage().get(),
          actualTransferTransaction.getMessage().get());
    }
    assertEquals(
        ((Address) submitTransferTransaction.getRecipient()).plain(),
        ((Address) actualTransferTransaction.getRecipient()).plain());
    assertEquals(
        submitTransferTransaction.getMosaics().size(),
        actualTransferTransaction.getMosaics().size());
    assertEquals(
        submitTransferTransaction.getMosaics().get(0).getAmount(),
        actualTransferTransaction.getMosaics().get(0).getAmount());
    assertEquals(
        submitTransferTransaction.getMosaics().get(0).getId().getId().longValue(),
        actualTransferTransaction.getMosaics().get(0).getId().getId().longValue());

    // verify the recipient account updated
    final AccountRepository accountRepository =
        testContext.getRepositoryFactory().createAccountRepository();
    final Address recipientAddress =
        testContext.getScenarioContext().<Account>getContext(recipientAccountKey).getAddress();
    AccountInfo accountInfo = accountRepository.getAccountInfo(recipientAddress).toFuture().get();
    assertEquals(recipientAddress.plain(), accountInfo.getAddress().plain());
    assertEquals(1, accountInfo.getMosaics().size());
    assertEquals(
        testContext.getNetworkCurrency().getNamespaceId().get().getIdAsLong(),
        accountInfo.getMosaics().get(0).getId().getId().longValue());
    assertEquals(transferAmount, accountInfo.getMosaics().get(0).getAmount().longValue());

    // Verify the signer/sender account got update
    AccountInfo signerAccountInfoBefore =
        testContext.getScenarioContext().getContext(signerAccountInfoKey);
    assertEquals(recipientAddress.plain(), accountInfo.getAddress().plain());
    final ResolvedMosaic mosaicBefore =
        signerAccountInfoBefore.getMosaics().stream()
            .filter(
                mosaic1 ->
                    mosaic1.getId().getId().longValue()
                        == testContext.getNetworkCurrency().getNamespaceId().get().getIdAsLong())
            .findFirst()
            .get();

    final AccountInfo signerAccountInfoAfter =
        accountRepository
            .getAccountInfo(testContext.getDefaultSignerAccount().getAddress())
            .toFuture()
            .get();
    final ResolvedMosaic mosaicAfter =
        signerAccountInfoAfter.getMosaics().stream()
            .filter(
                mosaic1 ->
                    mosaic1.getId().getId().longValue()
                        == testContext.getNetworkCurrency().getNamespaceId().get().getIdAsLong())
            .findFirst()
            .get();
    assertEquals(
        mosaicBefore.getAmount().longValue() - transferAmount, mosaicAfter.getAmount().longValue());
  }
}

package io.nem.symbol.automationHelpers.helper.sdk;

import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.sdk.model.transaction.Deadline;
import io.nem.symbol.sdk.model.transaction.Transaction;
import io.nem.symbol.sdk.model.transaction.TransactionFactory;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class BaseHelper<U extends BaseHelper> {

  protected final TestContext testContext;
  private Supplier<Deadline> deadlineSupplier;
  protected Optional<BigInteger> maxFee;
  protected final TransactionHelper transactionHelper;

  /**
   * Constructor.
   *
   * @param testContext Test context.
   */
  BaseHelper(final TestContext testContext) {
    this.testContext = testContext;
    this.transactionHelper = new TransactionHelper(testContext);
    deadlineSupplier = () -> transactionHelper.getDefaultDeadline();
    maxFee = Optional.empty();
  }

  /**
   * Gets the common properties for all transactions.
   *
   * @param factory Transaction factory.
   * @return Transaction.
   */
  protected <T extends Transaction> T buildTransaction(final TransactionFactory<T> factory) {
    return buildFactoryTransaction(factory).build();
  }

  /**
   * Gets the common properties for all transactions.
   *
   * @param factory Transaction factory.
   * @return Factory transaction.
   */
  protected <T extends Transaction> TransactionFactory<T> buildFactoryTransaction(
      final TransactionFactory<T> factory) {
    final Deadline deadlineLocal = deadlineSupplier.get();
    final T transaction = factory.build();
    final Long feeMultiplier = testContext.getMinFeeMultiplier();
    final Long fee =
        maxFee.isPresent() ? maxFee.get().longValue() : transaction.getSize() * feeMultiplier;
    return factory.deadline(deadlineLocal).maxFee(BigInteger.valueOf(fee));
  }

  private U getThis() {
    return (U) this;
  }

  /**
   * Set the deadline.
   *
   * @param deadlineSupplier Deadline supplier.
   * @return this
   */
  public U withDeadline(final Supplier<Deadline> deadlineSupplier) {
    this.deadlineSupplier = deadlineSupplier;
    return getThis();
  }

  /**
   * Set the max fee for the transaction.
   *
   * @param maxFee Max fee.
   * @return this
   */
  public U withMaxFee(final BigInteger maxFee) {
    this.maxFee = Optional.of(maxFee);
    return getThis();
  }
}

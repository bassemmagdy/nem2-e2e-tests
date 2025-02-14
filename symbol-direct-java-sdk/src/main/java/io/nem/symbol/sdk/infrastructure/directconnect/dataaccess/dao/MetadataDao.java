package io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.dao;

import io.nem.symbol.sdk.api.MetadataRepository;
import io.nem.symbol.sdk.api.MetadataSearchCriteria;
import io.nem.symbol.sdk.api.Page;
import io.nem.symbol.sdk.infrastructure.common.CatapultContext;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.MetadataCollection;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers.MapperUtils;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.blockchain.MerkleStateInfo;
import io.nem.symbol.sdk.model.metadata.Metadata;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.namespace.NamespaceId;
import io.reactivex.Observable;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class MetadataDao implements MetadataRepository {
  /* Catapult context. */
  private final CatapultContext catapultContext;
  private final MetadataCollection metadataCollection;

  /**
   * Constructor.
   *
   * @param context Catapult context.
   */
  public MetadataDao(final CatapultContext context) {
    this.catapultContext = context;
    metadataCollection = new MetadataCollection(catapultContext.getDataAccessContext());
  }

  private Metadata getMetadataOrThrow(
      final Optional<Metadata> optionalMetadata, final String error) {
    return optionalMetadata.orElseThrow(() -> new IllegalArgumentException(error));
  }

  /**
   *
   * @param criteria
   * @return
   */
  @Override
  public Observable<Page<Metadata>> search(MetadataSearchCriteria criteria) {
    return Observable.fromCallable(() -> new Page<>(metadataCollection.search(criteria)));
  }

  /**
   * Get metadata of the given id.
   *
   * @param compositeHash Metadata composite hash id
   * @return Observable {@link Metadata}
   */
  @Override
  public Observable<Metadata> getMetadata(String compositeHash) {
    return null;
  }

  /**
   * Get metadata merkle info of the given id.
   *
   * @param compositeHash MerkleStateInfo composite hash id
   * @return Observable {@link MerkleStateInfo}
   */
  @Override
  public Observable<MerkleStateInfo> getMetadataMerkle(String compositeHash) {
    return null;
  }
}

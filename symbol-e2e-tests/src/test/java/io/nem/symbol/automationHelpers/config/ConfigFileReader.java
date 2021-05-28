/*
 * Copyright (c) 2016-present,
 * Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights reserved.
 *
 * This file is part of Catapult.
 *
 * Catapult is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Catapult is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Catapult.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.nem.symbol.automationHelpers.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.nem.symbol.automationHelpers.common.RepositoryFactoryType;

/** Config reader for the automation framework. */
public class ConfigFileReader {
  /** The config file. */
  private final String propertyFile = "configs/config-default.yaml";

  private final Config config;

  /** Constructor. */
  public ConfigFileReader() {
    final Path resourcePath = Paths
        .get(Thread.currentThread().getContextClassLoader().getResource(propertyFile).getPath());
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
    try {
      config = mapper.readValue(new File(resourcePath.toAbsolutePath().toString()), Config.class);
    }
    catch (final IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not read config file.");
    }
  }

  /**
   * Gets the api host address.
   *
   * @return Api host name/address.
   */
  public String getApiHost() {
    return config.getApiHost();
  }

  /**
   * Gets the api host port.
   *
   * @return Api host port.
   */
  public int getApiPort() {
    return Integer.parseInt(config.getApiPort());
  }

  /**
   * Gets the broker host address.
   *
   * @return broker host name/address.
   */
  public String getBrokerHost() {
    return config.getBrokerHost();
  }

  /**
   * Gets the broker host port.
   *
   * @return broker host port.
   */
  public int getBrokerPort() {
    return Integer.parseInt(config.getBrokerPort());
  }

  /**
   * Gets the test user private key.
   *
   * @return Test user private key.
   */
  public String getUserPrivateKey() {
    return config.getUserPrivateKey().toUpperCase();
  }

  /**
   * Gets the mongo database host name.
   *
   * @return Mongo database host name.
   */
  public String getMongodbHost() {
    return config.getMongodbHost();
  }

  /**
   * Gets mongo database port.
   *
   * @return Mongo database port.
   */
  public int getMongodbPort() {
    return Integer.parseInt(config.getMongodbPort());
  }

  /**
   * Gets min fee multiplier
   *
   * @return Min fee multiplier.
   */
  public BigInteger getMinFeeMultiplier() {
    return new BigInteger(config.getMinFeeMultiplier());
  }

  /**
   * Gets socket timeout in milliseconds.
   *
   * @return Socket timeout in millisecond.
   */
  public int getSocketTimeoutInMilliseconds() {
    return Integer.parseInt(config.getSocketTimeoutInMilliseconds());
  }

  /**
   * Gets the database query timeout in seconds.
   *
   * @return Database query timeout in seconds.
   */
  public int getDatabaseQueryTimeoutInSeconds() {
    return Integer.parseInt(config.getDatabaseQueryTimeoutInSeconds());
  }

  /**
   * Gets the harvester public key.
   *
   * @return Public key.
   */
  public String getHarvesterPublicKey() {
    return config.getHarvesterPublicKey().toUpperCase();
  }

  /**
   * Gets the rest gateway url.
   *
   * @return Url for the rest gateway.
   */
  public String getRestGatewayUrl() {
    return config.getRestGatewayUrl();
  }

  /**
   * Gets the factory type to connect to catapult.
   *
   * @return Repository factory type.
   */
  public RepositoryFactoryType getRepositoryFactoryType() {
    return RepositoryFactoryType.valueOf(config.getRepositoryFactoryType().toUpperCase());
  }

  /**
   * Gets symbol config path.
   *
   * @return Symbol config path.
   */
  public String getSymbolConfigPath() {
    return "symbolConfigPath";
  }

  /**
   * Gets the harvester private key.
   *
   * @return Private key.
   */
  public String getHarvesterPrivateKey() {
    return "remoteHarvesterPrivateKey";
  }

  /**
   * Gets the harvester private key.
   *
   * @return Public key.
   */
  public String getNodePublicKey() {
    return "nodePublicKey";
  }

  /**
   * Gets the api server full certificate file.
   *
   * @return Api node certificate file.
   */
  public File getApiServerCertificateFile() {
    return new File("apiServerCertificateFile".toUpperCase());
  }

  /**
   * Gets the automation key file.
   *
   * @return Automation key file.
   */
  public File getAutomationKeyFile() {
    return new File("automationKeyFile".toUpperCase());
  }

  /**
   * Gets the automation certificate file.
   *
   * @return Automation certificate file.
   */
  public File getAutomationCertificateFile() {
    return new File("automationCertificateFile".toUpperCase());
  }
}

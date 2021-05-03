package io.nem.symbol.automationHelpers.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {
    @JsonProperty
    private String apiHost;

    @JsonProperty
    private String apiPort;

    @JsonProperty
    private String brokerHost;

    @JsonProperty
    private String brokerPort;

    @JsonProperty
    private String mongodbHost;

    @JsonProperty
    private String mongodbPort;

    @JsonProperty
    private String socketTimeoutInMilliseconds;

    @JsonProperty
    private String databaseQueryTimeoutInSeconds;

    @JsonProperty
    private String minFeeMultiplier;

    @JsonProperty
    private String restGatewayUrl;

    @JsonProperty
    private String repositoryFactoryType;

    @JsonProperty
    private String userPrivateKey;

    @JsonProperty
    private String harvesterPublicKey;

    public String getApiHost() {
        return this.apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getApiPort() {
        return this.apiPort;
    }

    public void setApiPort(String apiPort) {
        this.apiPort = apiPort;
    }

    public String getBrokerHost() {
        return this.brokerHost;
    }

    public void setBrokerHost(String brokerHost) {
        this.brokerHost = brokerHost;
    }

    public String getBrokerPort() {
        return this.brokerPort;
    }

    public void setBrokerPort(String brokerPort) {
        this.brokerPort = brokerPort;
    }

    public String getMongodbHost() {
        return this.mongodbHost;
    }

    public void setMongodbHost(String mongodbHost) {
        this.mongodbHost = mongodbHost;
    }

    public String getMongodbPort() {
        return this.mongodbPort;
    }

    public void setMongodbPort(String mongodbPort) {
        this.mongodbPort = mongodbPort;
    }

    public String getSocketTimeoutInMilliseconds() {
        return this.socketTimeoutInMilliseconds;
    }

    public void setSocketTimeoutInMilliseconds(String socketTimeoutInMilliseconds) {
        this.socketTimeoutInMilliseconds = socketTimeoutInMilliseconds;
    }

    public String getDatabaseQueryTimeoutInSeconds() {
        return this.databaseQueryTimeoutInSeconds;
    }

    public void setDatabaseQueryTimeoutInSeconds(String databaseQueryTimeoutInSeconds) {
        this.databaseQueryTimeoutInSeconds = databaseQueryTimeoutInSeconds;
    }

    public String getMinFeeMultiplier() {
        return this.minFeeMultiplier;
    }

    public void setMinFeeMultiplier(String minFeeMultiplier) {
        this.minFeeMultiplier = minFeeMultiplier;
    }

    public String getRestGatewayUrl() {
        return this.restGatewayUrl;
    }

    public void setRestGatewayUrl(String restGatewayUrl) {
        this.restGatewayUrl = restGatewayUrl;
    }

    public String getRepositoryFactoryType() {
        return this.repositoryFactoryType;
    }

    public void setRepositoryFactoryType(String repositoryFactoryType) {
        this.repositoryFactoryType = repositoryFactoryType;
    }

    public String getUserPrivateKey() {
        return this.userPrivateKey;
    }

    public void setUserPrivateKey(String userPrivateKey) {
        this.userPrivateKey = userPrivateKey;
    }

    public String getHarvesterPublicKey() {
        return this.harvesterPublicKey;
    }

    public void setHarvesterPublicKey(String harvesterPublicKey) {
        this.harvesterPublicKey = harvesterPublicKey;
    }
}

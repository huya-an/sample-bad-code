package com.autonation.vehiclemanagement.api.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Objects;

/** Represents a Token Entity Model
 *  for token exchange with Smart Car
 *
 */

@DynamoDbBean
public class Tokens {
    private String id;
    private String access_token;
    private String token_type;
    private String expires_in;
    private String refresh_token;
    private String userId;
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @DynamoDbAttribute(value = "access_token")
    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    @DynamoDbAttribute(value = "token_type")
    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    @DynamoDbAttribute(value = "expires_in")
    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    @DynamoDbAttribute(value = "refresh_token")
    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tokens tokens = (Tokens) o;
        return Objects.equals(id, tokens.id) && Objects.equals(access_token, tokens.access_token) && Objects.equals(token_type, tokens.token_type) && Objects.equals(expires_in, tokens.expires_in) && Objects.equals(refresh_token, tokens.refresh_token) && Objects.equals(userId, tokens.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, access_token, token_type, expires_in, refresh_token, userId);
    }
}
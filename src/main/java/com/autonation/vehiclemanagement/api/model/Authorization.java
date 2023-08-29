package com.autonation.vehiclemanagement.api.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;


/** Represents an Authorization Model
 *  for token exchange with Smart Car
 *
 */
@DynamoDbBean
public class Authorization {
    @DynamoDBHashKey
    private String authorization_code;
    private String userId;

    public String getAuthorization_code() {
        return authorization_code;
    }

    public void setAuthorization_code(String authorization_code) {
        this.authorization_code = authorization_code;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
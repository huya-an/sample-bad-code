package com.autonation.vehiclemanagement.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Objects;

/** Represents a Vehicle Entity Model
 *
 *
 */
@DynamoDbBean
public class Vehicle {

    private String id;
    private String make;
    private String model;
    private int year;
    private String vid;
    private String vin;
    private String userId;
    private String access_token;
    private String refresh_token;
    private String smartCarId;

    @DynamoDbPartitionKey
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @DynamoDbAttribute(value = "make")
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    @DynamoDbAttribute(value = "model")
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @DynamoDbAttribute(value = "year")
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @DynamoDbAttribute(value = "vid")
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    @DynamoDbAttribute(value = "vin")
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    @DynamoDbAttribute(value = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDbAttribute(value = "access_token")
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    @DynamoDbAttribute(value = "refresh_token")
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
    @DynamoDbAttribute(value = "smartCarId")
    @JsonInclude(JsonInclude.Include. NON_NULL)
    public String getSmartCarId() {
        return smartCarId;
    }

    public void setSmartCarId(String smartCarId) {
        this.smartCarId = smartCarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(getId(), vehicle.getId()) && Objects.equals(getMake(), vehicle.getMake()) && Objects.equals(getModel(), vehicle.getModel()) && Objects.equals(getYear(), vehicle.getYear()) && Objects.equals(getVid(), vehicle.getVid()) && Objects.equals(getVin(), vehicle.getVin()) && Objects.equals(getUserId(), vehicle.getUserId()) && Objects.equals(getAccess_token(), vehicle.getAccess_token()) && Objects.equals(getRefresh_token(), vehicle.getRefresh_token()) && Objects.equals(getSmartCarId(), vehicle.getSmartCarId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMake(), getModel(), getYear(), getVid(), getVin(), getUserId(), getAccess_token(), getRefresh_token(), getSmartCarId());
    }
}
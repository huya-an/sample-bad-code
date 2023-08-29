package com.autonation.vehiclemanagement.api.config;

import com.autonation.vehiclemanagement.api.model.Tokens;
import com.autonation.vehiclemanagement.api.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Logger;

import static software.amazon.awssdk.enhanced.dynamodb.internal.AttributeValues.stringValue;

@Component
@Slf4j
public class DynamodbClient {
    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${amazon.aws.amazonDynamoDBVehicleTable}")
    private String amazonDynamoVehicleTable;

    @Value("${amazon.aws.amazonDynamoDBTokensTable}")
    private String amazonDynamoTokensTable;
    private DynamoDbTable<Vehicle> vehicleDynamoDbVehicleTable;

    private DynamoDbTable<Tokens> tokensDynamoDbTokensTable;

    private static final Logger LOGGER = Logger.getLogger(DynamodbClient.class.getName());
    public DynamodbClient(DynamoDbEnhancedClient enhancedClient) {

        this.enhancedClient = enhancedClient;
    }
    @PostConstruct
    public void init() {
        vehicleDynamoDbVehicleTable = enhancedClient.table(amazonDynamoVehicleTable, TableSchema.fromBean(Vehicle.class));
        tokensDynamoDbTokensTable = enhancedClient.table(amazonDynamoTokensTable, TableSchema.fromBean(Tokens.class));
    }
    public void saveVehicle(Vehicle vehicle) {
        vehicleDynamoDbVehicleTable.putItem(vehicle);
    }

    public Vehicle getSampleDataFromDB(String id){
        LOGGER.info("called getSampleDataFromDB with Id ----------> " + id);
        Vehicle vehicle = null;
        try {
            Key key = Key.builder()
                    .partitionValue(id)
                    .build();
            // Get the item by using the key.
            vehicle = vehicleDynamoDbVehicleTable.getItem(
                    (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
            LOGGER.info("Result ----------> " + vehicle);
            return vehicle;
        } catch (DynamoDbException e) {
           LOGGER.info("Exception occurred "+e.getLocalizedMessage());
           e.printStackTrace();
        }
        LOGGER.info("Result ----------> " + vehicle);
        return vehicle;
    }

    public SdkIterable<Vehicle> getVehicle(String id){
        LOGGER.info("called getVehicle with Id ----------> " + id);
        QueryConditional keyEqual = QueryConditional.keyEqualTo(b->b.partitionValue(id));
        QueryEnhancedRequest tableQuery = QueryEnhancedRequest.builder().queryConditional(keyEqual).build();
        PageIterable<Vehicle> vehicles =  vehicleDynamoDbVehicleTable.query(tableQuery);
        LOGGER.info("total Count ----------> " +vehicles.stream().count());
        vehicles.items().stream().forEach(item->{
            LOGGER.info("Items ---> "+item);
        });
        return vehicles.items();
   }


    public SdkIterable<Vehicle> getVehicleScan(){
        LOGGER.info("called getVehicleScan with Id ----------> " );
        try {
       /* ScanEnhancedRequest request = ScanEnhancedRequest.builder().filterExpression(Expression.builder()
                .expression("smartCarId = :id").expressionValues(Map.of(":id",stringValue(smartCarId))).build()).build();*/

        PageIterable<Vehicle> vehicles =   vehicleDynamoDbVehicleTable.scan();
        if(vehicles!=null) {
            LOGGER.info("total Count ----------> " + vehicles.stream().count());
            return vehicles.items();
        }
        else{
            LOGGER.info("No Vehicles found" );
            return null;
            }
        } catch (DynamoDbException e) {
            LOGGER.info("Exception occurred "+e.getLocalizedMessage());
            e.printStackTrace();
        }
       return null;
    }

    public void saveTokens(Tokens tokens) {
        tokensDynamoDbTokensTable.putItem(tokens);
        // TODO Update vehicle table with access token;
    }

    public void updateVehicle(Vehicle vehicle,Tokens tokens) {
        try {
            Vehicle updateVehicle = new Vehicle();

            updateVehicle.setId(vehicle.getId());
            updateVehicle.setMake(vehicle.getMake());
            updateVehicle.setModel(vehicle.getModel());
            updateVehicle.setYear(vehicle.getYear());
            updateVehicle.setUserId(vehicle.getUserId());
            updateVehicle.setSmartCarId(vehicle.getSmartCarId());
            updateVehicle.setAccess_token(tokens.getAccess_token());
            updateVehicle.setRefresh_token(tokens.getRefresh_token());
            updateVehicle.setVid(vehicle.getVid());

            updateVehicle.setAccess_token(tokens.getAccess_token());
            updateVehicle.setRefresh_token(tokens.getRefresh_token());

            // Create an UpdateItemEnhancedRequest with the primary key and the updated item
            UpdateItemEnhancedRequest<Vehicle> updateItemEnhancedRequest = UpdateItemEnhancedRequest.builder(Vehicle.class)
                    .item(updateVehicle)
                    .build();
            // Update the item
            vehicleDynamoDbVehicleTable.updateItem(updateItemEnhancedRequest);

            log.info("Vehicle table updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating item: " + e.getMessage());
        }
    }
}
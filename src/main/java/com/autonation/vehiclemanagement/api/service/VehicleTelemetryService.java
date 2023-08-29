package com.autonation.vehiclemanagement.api.service;

import com.autonation.vehiclemanagement.api.config.DynamodbClient;
import com.autonation.vehiclemanagement.api.controller.VehicleMessageController;
import com.autonation.vehiclemanagement.api.model.Vehicle;
import com.smartcar.sdk.SmartcarException;
import com.smartcar.sdk.data.VehicleLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;

import java.util.Calendar;
import java.util.logging.Logger;

@Service
public class VehicleTelemetryService {
    @Autowired
    DynamodbClient dbClient;
    private static final Logger LOGGER = Logger.getLogger(VehicleTelemetryService.class.getName());

    public SdkIterable<Vehicle> getVehicleInfoFromDynamoDB() {
        LOGGER.info("vehicles retrieving");
        SdkIterable<Vehicle> vehicles = dbClient.getVehicleScan(); //todo: bad db scan
        LOGGER.info("vehicles retrieved - count: [" + vehicles.stream().count() + "]");
        return vehicles;
    }

    public String sendTelemetry(
            Vehicle item,
            VehicleLocation location
    ) throws SmartcarException {

        String requestBody = "{" + //todo: serialize to request object
                "  \"eventId\": {" +
                "    \"value\": \"" + Calendar.getInstance().getTimeInMillis() + "\"" +
                "  }," +
                "  \"aggregateId\": {" +
                "    \"value\": \"" + item.getVid() + "\"" +
                "  },\n" +
                "  \"version\": 0," +
                " \"mutableEvent\": {" +
                "      \"vehicleTelemetry\": {" +
                "          \"vid\": \"" + item.getVid() + "\"," +
                "          \"coords\": {" +
                "              \"latitude\": " + location.getLatitude() + "," +
                "              \"longitude\": " + location.getLongitude() +
                "          } " +
                "      }" +
                "  }" +
                "}";
        String contentType = "application/vnd.com.autonation.mydomain.evt.VehicleTelemetryPosted";
        String reqStr = getReqStrFor(contentType, requestBody);
        LOGGER.info("proxy telemetry requesting - " + reqStr);
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", contentType);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(
                    "http://amp-lb-sandbox-1393647344.us-east-1.elb.amazonaws.com/telemetry-stream-proxy-producer/evt",
                    requestEntity,
                    String.class
            );
            String resStr = "response: [" + response + "]";
            LOGGER.info("proxy telemetry requested - " + reqStr + " " + resStr);
            return response; //todo: must parse response code before checking response body
        } catch (Exception ex) {
            LOGGER.info("proxy telemetry requesting encountered an error - " + reqStr + " err: [" + ex.getMessage() + "]" );
            return ex.getMessage();
        }

    }

    private String getReqStrFor(
            String contentType,
            String requestBody
    ) {
        return "content-type :[" + contentType + "] " +
                "requestBody :[" + requestBody + "]";
    }

}

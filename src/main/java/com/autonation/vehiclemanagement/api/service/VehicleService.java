package com.autonation.vehiclemanagement.api.service;

import com.autonation.vehiclemanagement.api.controller.VehicleMessageController;
import com.autonation.vehiclemanagement.api.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class VehicleService {

    private static final Logger LOGGER = Logger.getLogger( VehicleMessageController.class.getName() );

    @Value("${redpanda.http.post.uri}")
    private String httpUrl;

    @Value("${redpanda.http.post.vehicleAssignedType}")
    private String vehicleAssignedType;
    public void vehicleAssignHttpPost(Vehicle vehicle) {

        String make = vehicle.getMake();
        String model = vehicle.getModel();
        int year = vehicle.getYear();
        String vin = vehicle.getVid();
        String vid = vehicle.getVid();
        String userId = vehicle.getUserId();
        String guid = String.valueOf(UUID.randomUUID());
        String requestBody = "{" +
                "  \"eventId\": {" +
                "    \"value\": \"" + guid + "\"" +
                "  }," +
                "  \"aggregateId\": {" +
                "    \"value\": \"" + userId + "\"" +
                "  }," +
                "  \"version\": 0," +
                "  \"mutableEvent\": {" +
                "    \"userId\": \"" + userId + "\"," +
                "    \"make\": \"" + make + "\"," +
                "    \"model\": \"" + model + "\"," +
                "    \"year\": \"" + year + "\"," +
                "    \"vin\": \"" + vin + "\"," +
                "    \"vid\": \"" + vid + "\"" +
                "  }" +
                "}";
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", vehicleAssignedType);
            // Create the HTTP entity with the payload and header
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            log.info("Vehicle assigned (data) event posting to Proxy ...  " + requestEntity);
            // Perform the POST request to Proxy
            String response = restTemplate.postForObject(httpUrl, requestEntity, String.class);
            log.info("Vehicle assigned (data) event posted to Proxy ...  " + requestEntity);
            log.info("Response received  : " + response);
        } catch ( Exception ex) {
            log.error(ex.getMessage());
        }
    }
}

package com.autonation.vehiclemanagement.api.controller;

import com.autonation.vehiclemanagement.api.config.DynamodbClient;
import com.autonation.vehiclemanagement.api.model.*;
import com.autonation.vehiclemanagement.api.service.VehicleService;
import com.autonation.vehiclemanagement.api.service.VehicleTelemetryService;
import com.autonation.vehiclemanagement.api.util.JavaUtil;
import com.smartcar.sdk.Smartcar;
import com.smartcar.sdk.SmartcarException;
import com.smartcar.sdk.data.VehicleAttributes;
import com.smartcar.sdk.data.VehicleIds;

import com.smartcar.sdk.data.VehicleLocation;
import com.smartcar.sdk.data.VehicleVin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;

import java.util.*;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the core functionality for API Rest Endpoint
 */

@RestController
@EnableAutoConfiguration
public class VehicleMessageController {
    @Autowired
    DynamodbClient dbClient;

    @Autowired
    VehicleService vehicleProxyService;
    @Autowired
    VehicleTelemetryService vehicleservice;
    @Value("${clientId}")
    private String clientId;
    @Value("${clientSecret}")
    private String clientSecret;
    @Value("${redirectUri}")
    private String redirectUri;
    @Value("${tokenUri}")
    private String tokenUri;

    private static final Logger LOGGER = Logger.getLogger(VehicleMessageController.class.getName());

    /**
     * Retrieves the access_token from smart car, in exchange for authorization_code.
     *
     * @return the access_token, token_type, expires_in, refresh_token
     */
    @PostMapping("/user-granted")
    @ResponseBody
    public ResponseEntity<String> userGranted(@RequestBody Authorization authorization) throws SmartcarException {

        String reqStr = getReqStr(authorization);
        LOGGER.info("granting access for vehicle user - " + reqStr);

        Tokens tokens = exchangeAuthorizationCodeForToken(authorization);

        if (tokens == null) {
            LOGGER.warning("granting access for vehicle user encountered an error - [token returned null] " + reqStr);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String tokenStr = getTokenStr(tokens);
        LOGGER.info("granting access for vehicle user - " + tokenStr);

        String[] smartCarVehicleIds = getSmartCarVehicleIds(tokens.getAccess_token());

        List<Vehicle> vehicles = vehicleservice.getVehicleInfoFromDynamoDB().stream().filter(c ->
                isValue(c.getSmartCarId())
        ).collect(Collectors.toList());

        for (String smartCarVehicleId : smartCarVehicleIds) {

            com.smartcar.sdk.Vehicle smartCarVehicle = getSmartCarVehicle(tokens.getAccess_token(), smartCarVehicleId);
            VehicleAttributes vehicleAttributes = smartCarVehicle.attributes();
            if (vehicleAttributes == null) {
                LOGGER.warning("granting access for vehicle user encountered an error - [vehicleAttributes returned null] " + reqStr);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            VehicleVin vehicleVin = smartCarVehicle.vin();
            if (vehicleVin == null) {
                LOGGER.warning("granting access for vehicle user encountered an error - [vehicleVin returned null] " + reqStr);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            boolean exists = vehicles.stream().anyMatch(c ->
                    c.getSmartCarId().equals(vehicleAttributes.getId())
            );

            if (!exists) {
                LOGGER.info("vehicle with smart car id [" + vehicleAttributes.getId() + "] does not exist");
                addVehicle(vehicleAttributes, tokens, vehicleVin);
            } else {
                LOGGER.info("vehicle with smart car id [" + vehicleAttributes.getId() + "] exists");
                updateVehicle(vehicleAttributes, tokens, vehicles.toArray(Vehicle[]::new));
            }
        }
        LOGGER.info("granted access for vehicle user - " + reqStr);
        return ResponseEntity.ok("Successfully validated and added vehicle from smart Car");
    }

    private String getTokenStr(Tokens tokens) {
        return "userId: [" + tokens.getUserId() + "]" +
                "access_token: [" + tokens.getAccess_token() + "]" +
                "refresh_token: [" + tokens.getRefresh_token() + "]" +
                "token_type: [" + tokens.getToken_type() + "]";
    }

    private String getReqStr(Authorization authorization) {
        return "userId: [" + authorization.getUserId() + "]" +
                "authorization_code: [" + authorization.getAuthorization_code() + "]";
    }

    /**
     * Builds a request object with the authorization_code to smart car,
     *
     * @return the access_token, refresh_token
     */
    private Tokens exchangeAuthorizationCodeForToken(Authorization authorization) {
        LOGGER.info("exchanging authorization code for - token: [" + authorization.getAuthorization_code() + "] uri: [" + redirectUri + "] clientId: [" + this.clientId + ": " + this.clientSecret + "]");
        ResponseEntity<Map> responseEntity = null;
        RestTemplate restTemplate = new RestTemplate();
        LOGGER.info("Smartcar Token Exchange call to get Access Token with authorization_token----> " + authorization.getAuthorization_code());
        LOGGER.info("Smartcar Token Exchange call to get Access Token with userId----> " + authorization.getUserId());

        // Set up the request to exchange the authorization code for an access token
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("code", authorization.getAuthorization_code());
        requestBody.add("redirect_uri", redirectUri);
        requestBody.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        String encodedString = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        LOGGER.info("Smartcar Token Exchange call --> authorization_Code ----------> " + authorization.getAuthorization_code());
        LOGGER.info("Smartcar Token Exchange call --> Bearer Token Authorization" + encodedString);
        headers.setBasicAuth(encodedString);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Make the request to third-party API to get Authorization token
        try {
            responseEntity = restTemplate.postForEntity(tokenUri, requestEntity, Map.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Map<String, String> response = responseEntity.getBody();

                if (response != null) {
                    Tokens tokens = new Tokens();
                    tokens.setId(JavaUtil.generateType1UUID().toString());
                    tokens.setAccess_token(response.get("access_token"));
                    tokens.setToken_type(response.get("token_type"));
                    tokens.setExpires_in(String.valueOf(response.get("expires_in")));
                    tokens.setRefresh_token(response.get("refresh_token"));
                    tokens.setUserId(authorization.getUserId());

                    dbClient.saveTokens(tokens);
                    return tokens;
                }
            }
        } catch (Exception ex) {
            LOGGER.info(ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Refreshes the access_token calling smart car using refresh_token
     *
     * @return the access_token, token_type, expires_in, refresh_token
     */
    @PostMapping("/user-refresh")
    @ResponseBody
    public ResponseEntity<String> refreshToken(@RequestBody Authorization authorization) throws SmartcarException {

        LOGGER.info("Call Refresh Token to refresh access token  every 7200sec" + authorization.getAuthorization_code() + "] uri: [" + redirectUri + "] clientId: [" + this.clientId + ": " + this.clientSecret + "]");
        ResponseEntity<Map> responseEntity = null;
        StringBuilder builder = new StringBuilder();
        RestTemplate restTemplate = new RestTemplate();
        LOGGER.info("Refresh Token to refresh access token --> " + authorization.getAuthorization_code());
        LOGGER.info("Smartcar Token Exchange call to get Access Token with userId----> " + authorization.getUserId());

        // Set up the request to exchange the authorization code for an access token
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("refresh_token", authorization.getAuthorization_code());
        requestBody.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        String encodedString = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        LOGGER.info("Smartcar Token Exchange call to refresh token--> authorization_Code ----------> " + authorization.getAuthorization_code());
        LOGGER.info("Smartcar Token Exchange call to refresh token---> Bearer Token Authorization" + encodedString);
        headers.setBasicAuth(encodedString);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Make the request to third-party API to get Authorization token
        try {
            responseEntity = restTemplate.postForEntity(tokenUri, requestEntity, Map.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Map<String, String> response = responseEntity.getBody();
                builder.append(response);
                if (response != null) {
                    Tokens tokens = new Tokens();
                    tokens.setId(JavaUtil.generateType1UUID().toString());
                    tokens.setAccess_token(response.get("access_token"));
                    tokens.setToken_type(response.get("token_type"));
                    tokens.setExpires_in(String.valueOf(response.get("expires_in")));
                    tokens.setRefresh_token(response.get("refresh_token"));
                    tokens.setUserId(authorization.getUserId());

                    dbClient.saveTokens(tokens);
                    return ResponseEntity.ok(builder.toString());
                }
            }
        } catch (Exception ex) {
            LOGGER.info(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        return null;
    }

    /**
     * Builds a request object for manually adding vehicles
     *
     * @returns a successful message on saving the vehicle
     */
    @PostMapping(value = "/vehicle-add", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> addManualVehicle(@RequestBody Vehicle vehicle) {
        if (vehicle != null) {
            try {
                String vehicleMake = vehicle.getMake();
                LOGGER.info("Vehicle Management Service : vehicleMake ----------> " + vehicleMake);
                vehicle.setId(JavaUtil.generateType1UUID().toString());
                vehicle.setVid(vehicle.getVin());
                dbClient.saveVehicle(vehicle);
                vehicleProxyService.vehicleAssignHttpPost(vehicle);
                LOGGER.info("VehicleAssigned Event posted to Proxy");
                // Return a response indicating successful save of Vehicle
                return ResponseEntity.ok("Vehicle save Successful in vehicle Database");
            } catch (Exception exc) {
                return new ResponseEntity(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves the list of vehicle ids from smart car, for an authorized token.
     *
     * @returns vehicle Ids associated with the token and OEM vendor
     */
    @GetMapping(value = "/list", produces = "application/json")
    @ResponseBody
    public ResponseEntity getVehicleList(@RequestParam String userId) throws SmartcarException {
        LOGGER.info("vehicles for user retrieving - userId: [" + userId + "]");
        ArrayList<Vehicle> vehicleList = new ArrayList<>();
        try {
            SdkIterable<Vehicle> vehicles = vehicleservice.getVehicleInfoFromDynamoDB();
            LOGGER.info("vehicles for user retrieving - " +
                    "userId: [" + userId + "] " +
                    "total-vehicle-count: [" + vehicles.stream().count() + "]");

            for (Vehicle item : vehicles) {

                if (item == null || isNotValue(item.getUserId()) || !item.getUserId().equalsIgnoreCase(userId)) {
                    continue;
                }

                Vehicle newVehicleListFromDb = new Vehicle();
                newVehicleListFromDb.setMake(item.getMake());
                newVehicleListFromDb.setModel(item.getModel());
                newVehicleListFromDb.setYear(item.getYear());
                newVehicleListFromDb.setUserId(item.getUserId());
                newVehicleListFromDb.setVid(item.getVid());
                newVehicleListFromDb.setVin(item.getVin());
                vehicleList.add(newVehicleListFromDb);
            }
        } catch (Exception e) {
            //todo: need middleware for error handling
            LOGGER.warning("vehicles for user retrieving encountered an error - " +
                    "userId: [" + userId + "] " +
                    "err: [" + Arrays.toString(e.getStackTrace()) + "]");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("vehicles for user retrieved - userId: [" + userId + "]");
        return new ResponseEntity<>(vehicleList, HttpStatus.OK);
    }

    private Boolean isNotValue(String str) {
        return str == null || str.trim().isEmpty();
    }

    private Boolean isValue(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public String[] getSmartCarVehicleIds(@RequestParam String bearerToken) throws SmartcarException {
        LOGGER.info("Vehicle Management Service : Get Vehicle list ----------> ");
        Smartcar smartcar = new Smartcar();
        VehicleIds vehicleIds = smartcar.getVehicles(bearerToken);
        // Return a response indicating successful list of Vehicle
        return vehicleIds.getVehicleIds();
    }

    /**
     * Retrieves the vehicle details from smart car, for a vehicle id that is requested.
     *
     * @returns the details of the vehicle
     */
    public com.smartcar.sdk.Vehicle getSmartCarVehicle(@RequestParam String bearerToken, String vehicleId) throws SmartcarException {
        return new com.smartcar.sdk.Vehicle(vehicleId, bearerToken);
    }

    private void updateVehicle(
            VehicleAttributes vehicleAttributes,
            Tokens tokens,
            Vehicle[] vehicles
    ) {
        for (Vehicle item : vehicles) {
            String smartCarId = item.getSmartCarId();
            if (isNotValue(smartCarId) || !smartCarId.equals(vehicleAttributes.getId())) {
                continue;
            }

            String vehicleStr = getVehicleStr(item);
            LOGGER.info("updating vehicle - " + vehicleStr);
            item.setAccess_token(tokens.getAccess_token());
            item.setRefresh_token(tokens.getRefresh_token());
            dbClient.updateVehicle(item, tokens);
            LOGGER.info("updated vehicle - " + vehicleStr);
        }
    }

    private void addVehicle(
            VehicleAttributes vehicleAttributes,
            Tokens tokens,
            VehicleVin vehicleVin
    ) {
        String vehicleAttributesStr = getVehicleAttributesStr(vehicleAttributes);
        String tokenStr = getTokenStr(tokens);
        String vinStr = "vin: [" + vehicleVin.getVin() + "]";
        LOGGER.info("adding vehicle - " + vehicleAttributesStr + " " + tokenStr + " " + vinStr);
        Vehicle vehicle = new Vehicle();
        vehicle.setId(JavaUtil.generateType1UUID().toString());
        vehicle.setSmartCarId(vehicleAttributes.getId());
        vehicle.setMake(vehicleAttributes.getMake());
        vehicle.setModel(vehicleAttributes.getModel());
        vehicle.setYear(vehicleAttributes.getYear());
        vehicle.setUserId(tokens.getUserId());
        vehicle.setAccess_token(tokens.getAccess_token());
        vehicle.setRefresh_token(tokens.getRefresh_token());
        vehicle.setVid(vehicleVin.getVin()); //todo: to encrypt
        vehicle.setVin(vehicleVin.getVin());
        dbClient.saveVehicle(vehicle);
        vehicleProxyService.vehicleAssignHttpPost(vehicle);
        String vehicleStr = getVehicleStr(vehicle);
        LOGGER.info("added vehicle - " + vehicleStr);
    }

    private String getVehicleAttributesStr(VehicleAttributes vehicleAttributes) {
        return "smartCarId: [" + vehicleAttributes.getId() + "]" +
                "make: [" + vehicleAttributes.getMake() + "]" +
                "model: [" + vehicleAttributes.getModel() + "]" +
                "year: [" + vehicleAttributes.getYear() + "]";
    }

    /**
     * Retrieves the vehicle Location details from smart car, for a vehicle id that is requested.
     *
     * @returns vehicle location of the vehicle
     */
    @GetMapping(value = "/telematics", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getSmartCarLocation() throws SmartcarException {
        StringBuilder builder = new StringBuilder();

        LOGGER.info("telematics processing");

        SdkIterable<Vehicle> vehicles = vehicleservice.getVehicleInfoFromDynamoDB();
        if (vehicles == null) {
            LOGGER.info("telematics processing encountered an error - [vehicles response is null]");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        for (Vehicle item : vehicles) {

            if (isNotValue(item.getAccess_token()) || isNotValue(item.getSmartCarId())) {
                continue;
            }

            String vehicleStr = getVehicleStr(item);
            LOGGER.info("telematics processing - " + vehicleStr);
            com.smartcar.sdk.Vehicle smartCarVehicle = new com.smartcar.sdk.Vehicle(
                    item.getSmartCarId(),
                    item.getAccess_token()
            );

            VehicleLocation location = smartCarVehicle.location();
            if (location == null) {
                LOGGER.warning("telematics processing encountered an error - " + vehicleStr + "err: [smartCarVehicle localtion returned null]");
                builder.append(item.getSmartCarId()) //todo: provide better response handling
                        .append(" location or vehicle not found ");
                continue;
            }

            String smartCarVehicleStr = getSmartCarVehicleStr(location);
            LOGGER.info("telematics processing - " + vehicleStr + " " + smartCarVehicleStr);

            String response = vehicleservice.sendTelemetry(item, location);
            LOGGER.info("telematics processed - " + vehicleStr);

            //todo: provide better response handling
            builder.append("{\"smarCarId\":")
                    .append(item.getSmartCarId()).append(":").append(response)
                    .append("}");
        }

        LOGGER.info("telematics processed");
        return ResponseEntity.ok(builder.toString());
    }

    private String getSmartCarVehicleStr(VehicleLocation location) throws SmartcarException {
        return "lat: [" + location.getLatitude() + "]" +
                "lon: [" + location.getLongitude() + "]";
    }

    private String getVehicleStr(Vehicle item) {
        return "userId: [" + item.getUserId() + "]" +
                "smartCarId: [" + item.getSmartCarId() + "]" +
                "vid: [" + item.getVid() + "]" +
                "vin: [" + item.getVin() + "]" +
                "year: [" + item.getYear() + "]" +
                "make: [" + item.getMake() + "]" +
                "model: [" + item.getModel() + "]" +
                "access_token: [" + item.getAccess_token() + "]" +
                "refresh_token: [" + item.getRefresh_token() + "]";
    }
}
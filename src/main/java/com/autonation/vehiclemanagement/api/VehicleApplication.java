package com.autonation.vehiclemanagement.api;

import com.autonation.vehiclemanagement.api.config.DynamoDBConfig;
import com.autonation.vehiclemanagement.api.controller.VehicleManagementController;
import com.autonation.vehiclemanagement.api.controller.VehicleMessageController;
import com.autonation.vehiclemanagement.api.service.VehicleService;
import com.autonation.vehiclemanagement.api.service.VehicleTelemetryService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;


@EnableAutoConfiguration
@SpringBootApplication
@AutoConfigurationPackage
@ComponentScan(basePackageClasses = {VehicleManagementController.class, VehicleMessageController.class,
                                     DynamoDBConfig.class, VehicleTelemetryService.class, VehicleService.class})
//@SpringBootApplication(scanBasePackages = "com.autonation.vehiclemanagement.api.*.*")
public class VehicleApplication extends SpringBootServletInitializer{

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(VehicleApplication.class, args);
    }

}

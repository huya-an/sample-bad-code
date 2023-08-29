package com.autonation.vehiclemanagement.api.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableAutoConfiguration
public class VehicleManagementController {

    //@RequestMapping(name="/hello", method= RequestMethod.GET)
    @GetMapping(value = "/hello", produces="text/plain")
    @ResponseBody
    public String hello() {
             return "Hello, World!";
    }

}

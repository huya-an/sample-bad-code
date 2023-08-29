package com.autonation.vehiclemanagement.api.Repository;

import com.autonation.vehiclemanagement.api.model.Vehicle;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;


@EnableScan
public interface VehicleRepository extends CrudRepository<Vehicle, String> {
}
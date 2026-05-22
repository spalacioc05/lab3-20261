package com.udea.vuelokbt.controller;

import com.udea.vuelokbt.exception.InvalidRating;
import com.udea.vuelokbt.exception.ModelNotFoundException;
import com.udea.vuelokbt.model.Flight;
import com.udea.vuelokbt.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/flight")
@CrossOrigin("*")   //http://localhost:8089/flight          ReactJS http://localhost:3000
public class FlightController {

    @Autowired
    private FlightService flightService;

    @PostMapping("/save")
    public Long save(@RequestBody Flight flight) throws InvalidRating {
        if(flight.getRating()>5)
            throw new InvalidRating ("Rating should be less than or equal 5");
        flightService.save(flight);
        return flight.getIdFlight();
    }

    @GetMapping("/listAll")
    public Iterable<Flight> listAllFlights() {
        return flightService.list();
    }

    @GetMapping("/list/{id}")
    public Flight listFlightById(@PathVariable("id") Long id) {
        Optional<Flight> flight = flightService.listId(id);
        if(flight.isPresent()){
            return flight.get();
        }
        throw new ModelNotFoundException("Flight not found");
    }


    @GetMapping("/topFlights")
    public ResponseEntity<List<Flight>> viewBestFlights() {
        List<Flight> list= flightService.viewBestFlight();
        return new ResponseEntity<List<Flight>>(list, HttpStatus.ACCEPTED);
    }


    @PutMapping
    public Flight updateFlight(@RequestBody Flight flight) {
        return flightService.update(flight);
    }


    @DeleteMapping("{id}")
    public String deleteFlight(@PathVariable Long id) {
        return flightService.delete(id);
    }

}

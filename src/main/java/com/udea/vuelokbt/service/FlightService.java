package com.udea.vuelokbt.service;

import com.udea.vuelokbt.dao.IFlightDAO;
import com.udea.vuelokbt.exception.FlightNotFoundException;
import com.udea.vuelokbt.model.Flight;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class FlightService {

@Autowired private IFlightDAO dao;

public Flight save(Flight flight) {
    return dao.save(flight);
}

public String delete(Long id) {
    dao.deleteById(id);
    return "Flight deleted";
}

public Iterable<Flight> list() {
    return dao.findAll();
}

public Optional<Flight> listId(Long id) {
    return dao.findById(id);
}


public Flight update(Flight flight) {
    Flight existingFlight = dao.findById(flight.getIdFlight()).orElse(null);
    existingFlight.setNombreAvion(flight.getNombreAvion());
    existingFlight.setNumeroVuelo(flight.getNumeroVuelo());
    existingFlight.setOrigen(flight.getOrigen());
    existingFlight.setDestino(flight.getDestino());
    existingFlight.setRating(flight.getRating());
    existingFlight.setPlanvuelo(flight.getPlanvuelo());
    existingFlight.setCapacidad(flight.getCapacidad());
    existingFlight.setCumplido(flight.getCumplido());
    return dao.save(existingFlight);
}

public List<Flight> viewBestFlight() throws FlightNotFoundException {
    List<Flight> flights = dao.viewBestFlight();
    if(flights.size()>0)
        return flights;
    else throw new FlightNotFoundException("No flight found ");
}



}

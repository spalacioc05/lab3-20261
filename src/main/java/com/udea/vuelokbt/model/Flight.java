package com.udea.vuelokbt.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import lombok.*;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name ="flight")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFlight")
    private Long idFlight;

    @Column(name = "nombreAvion", nullable = false, length = 80)
    private @NonNull String nombreAvion;

    @Column(name = "numerovuelo", nullable = false, length = 80)
    private @NonNull String numeroVuelo;

    @Column(name = "origen", nullable = false, length = 80)
    private @NonNull String origen;

    @Column(name = "destino", nullable = false, length = 80)
    private @NonNull String destino;

    @Column(name = "capacidad", nullable = false, length = 80)
    private @NonNull int capacidad;

    @Column(name = "rating", nullable = false, length = 80)
    @Min(value = 1, message = "id should be more or than equal 1")
    @Max(value = 5, message = "id should be less or than equal 5")
    private @NonNull int rating;

    @Column(name = "planvuelo", nullable = false, length = 80)
    private @NonNull long planvuelo;

    private Boolean cumplido;

    public Long getIdFlight() {
        return idFlight;
    }

    public void setIdFlight(Long idFlight) {
        this.idFlight = idFlight;
    }

    public @NonNull String getNombreAvion() {
        return nombreAvion;
    }

    public void setNombreAvion(@NonNull String nombreAvion) {
        this.nombreAvion = nombreAvion;
    }

    public @NonNull String getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(@NonNull String numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public @NonNull String getOrigen() {
        return origen;
    }

    public void setOrigen(@NonNull String origen) {
        this.origen = origen;
    }

    public @NonNull String getDestino() {
        return destino;
    }

    public void setDestino(@NonNull String destino) {
        this.destino = destino;
    }

    @NonNull
    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(@NonNull int capacidad) {
        this.capacidad = capacidad;
    }

    @Min(value = 1, message = "id should be more or than equal 1")
    @Max(value = 5, message = "id should be less or than equal 5")
    @NonNull
    public int getRating() {
        return rating;
    }

    public void setRating(@Min(value = 1, message = "id should be more or than equal 1") @Max(value = 5, message = "id should be less or than equal 5") @NonNull int rating) {
        this.rating = rating;
    }

    @NonNull
    public long getPlanvuelo() {
        return planvuelo;
    }

    public void setPlanvuelo(@NonNull long planvuelo) {
        this.planvuelo = planvuelo;
    }

    public Boolean getCumplido() {
        return cumplido;
    }

    public void setCumplido(Boolean cumplido) {
        this.cumplido = cumplido;
    }
}

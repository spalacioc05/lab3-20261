package com.udea.vuelokbt.exception;

public class InvalidRating extends RuntimeException{
    public InvalidRating(String message) {
        super(message);
    }
}

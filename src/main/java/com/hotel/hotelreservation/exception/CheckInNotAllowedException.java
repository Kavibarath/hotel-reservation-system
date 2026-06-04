package com.hotel.hotelreservation.exception;

public class CheckInNotAllowedException extends RuntimeException {
    public CheckInNotAllowedException(String message) {
        super(message);
    }
}

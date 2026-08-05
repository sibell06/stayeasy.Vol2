package com.softuni.stayeasy.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({PropertyNotFoundException.class, ReservationNotFoundException.class})
    public String handleNotFound(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/not-found";
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public String handleUnauthorized() {
        return "error/access-denied";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch() {
        return "error/not-found";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralError() {
        return "error/general-error";
    }
}
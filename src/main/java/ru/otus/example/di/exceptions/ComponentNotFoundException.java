package ru.otus.example.di.exceptions;

public class ComponentNotFoundException extends RuntimeException {

    private String message;

    public ComponentNotFoundException (String message) {
        super();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}

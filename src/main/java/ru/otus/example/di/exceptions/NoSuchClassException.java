package ru.otus.example.di.exceptions;

public class NoSuchClassException extends RuntimeException {
    private String message;

    public NoSuchClassException (String message) {
        super();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }


}

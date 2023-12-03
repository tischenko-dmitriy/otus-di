package ru.otus.exceptions;

public class NoSuchClassException extends RuntimeException {

    private String name;

    public NoSuchClassException(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}

package ru.otus.exceptions;

public class ComponentNotFoundException extends RuntimeException {

    private String name;

    public String getName() {
        return name;
    }

    public ComponentNotFoundException(String name) {
        this.name = name;
    }

}

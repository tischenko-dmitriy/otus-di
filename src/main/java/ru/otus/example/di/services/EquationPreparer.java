package ru.otus.example.di.services;

import ru.otus.example.di.model.Equation;

import java.util.List;

public interface EquationPreparer {
    List<Equation> prepareEquationsFor(int base);
}

package ru.otus.example.di.appcontainer;

import ru.otus.example.di.appcontainer.api.AppComponent;
import ru.otus.example.di.appcontainer.api.AppComponentsContainer;
import ru.otus.example.di.appcontainer.api.AppComponentsContainerConfig;
import ru.otus.example.di.exceptions.ComponentNotFoundException;
import ru.otus.example.di.exceptions.NoSuchClassException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) {
        processConfig(initialConfigClass);
    }

    private void processConfig(Class<?> configClass) {
        checkConfigClass(configClass);
        // You code here...

        List<Object> components = Stream.of(configClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(AppComponent.class))
                .collect(Collectors.toList());

        Map<String, Object> sorted = new HashMap<>();
        components.forEach(i -> sorted.put(i.getClass().getName(), i));

        for (int i = 0; i < components.size() - 1; i++) {
            for (Object component : components) {
                if ( ((Method) component).isAnnotationPresent(AppComponent.class) ) {
                    if ( ((Method) component).getAnnotation(AppComponent.class).order() == i ) {
                        appComponentsByName.put(component.getClass().getName(), component);
                        appComponents.add(component);
                    }

                }

            }

        }

    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> C getAppComponent(Class<C> componentClass) {
        List<Object> components = appComponents
                .stream()
                .filter(i -> ((Method) i).getReturnType().getName().equals(componentClass.getName()))
                .toList();

        if (components.size() == 0) {
            throw new NoSuchClassException(componentClass.getName());
        }

        return (C) components.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> C getAppComponent(String componentName) {
        if (!appComponentsByName.containsKey(componentName)) {
            throw new ComponentNotFoundException(componentName);
        }

        return (C) appComponentsByName.get(componentName);
    }
}

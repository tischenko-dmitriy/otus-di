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
                        Class<?> componentClass;
                        try {
                            componentClass = Class.forName(((Method) component).getReturnType().getName());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        appComponentsByName.put(((Method) component).getAnnotation(AppComponent.class).name(), componentClass);
                        appComponents.add(componentClass);
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
        C component = null;
        if (appComponents.contains(componentClass)) {
            try {
                String className = ((Class<C>) appComponents.get(appComponents.indexOf(componentClass))).getName();
                component = (C) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return component;
        } else if (componentClass.getInterfaces().length != 0) {
            List<Class<?>> interfaces = Stream.of(componentClass.getInterfaces()).toList();
            for (Class<?> cls : interfaces) {
                try {
                    String className = ((Class<C>) appComponents.get(appComponents.indexOf(cls))).getName();
                    component = (C) Class.forName(className);
                    break;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);

                }
            }
            return component;
        } else {
            throw new NoSuchClassException(componentClass.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> C getAppComponent(String componentName) {
        if (!appComponentsByName.containsKey(componentName)) {
            throw new ComponentNotFoundException(componentName);
        }

        Object component;
        try {
            String className = ((Class<C>) appComponentsByName.get(componentName)).getName();
            component = Class.forName(className).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return (C) component;
    }

}

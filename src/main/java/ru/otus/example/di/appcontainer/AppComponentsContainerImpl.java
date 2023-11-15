package ru.otus.example.di.appcontainer;

import org.reflections.Reflections;
import ru.otus.example.di.appcontainer.api.AppComponent;
import ru.otus.example.di.appcontainer.api.AppComponentsContainer;
import ru.otus.example.di.appcontainer.api.AppComponentsContainerConfig;
import ru.otus.example.di.exceptions.ComponentNotFoundException;
import ru.otus.example.di.exceptions.NoSuchClassException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections.scanners.Scanners.SubTypes;

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

        Reflections reflections = new Reflections("ru.otus.example.di");
        for (int i = 0; i < components.size() - 1; i++) {
            for (Object component : components) {
                if ( ((Method) component).isAnnotationPresent(AppComponent.class) ) {
                    if ( ((Method) component).getAnnotation(AppComponent.class).order() == i ) {
                        Class<?> componentClass = ((Method) component).getReturnType();
                        Set<Class<?>> subClasses = new HashSet<>();
                        if (componentClass.isInterface()) {
                            String beanName = String.format("%s%s",
                                    componentClass.getSimpleName().substring(0, 1).toLowerCase(),
                                    componentClass.getSimpleName().substring(1));
                            appComponentsByName.put(beanName, componentClass);
                            appComponentsByName.put(componentClass.getName(), componentClass);
                            appComponents.add(componentClass);
                            subClasses = reflections.get(SubTypes.of(componentClass).asClass());
                        } else {
                            subClasses.add(componentClass);
                        }

                        subClasses.forEach((subClass) -> {
                            appComponentsByName.put(subClass.getName(), subClass);
                            appComponents.add(subClass);
                        });

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
        Class<C> component = null;
        if (appComponents.contains(componentClass)) {
            String className = ((Class<C>) appComponents.get(appComponents.indexOf(componentClass))).getName();
            component = (Class<C>) appComponents.get(appComponents.indexOf(componentClass));
            return (C) component;

        } else if (componentClass.getInterfaces().length != 0) {
            List<Class<?>> interfaces = Stream.of(componentClass.getInterfaces()).toList();
            for (Class<?> cls : interfaces) {
                try {
                    String className = ((Class<C>) appComponents.get(appComponents.indexOf(cls))).getName();
                    component = (Class<C>) Class.forName(className);
                    break;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);

                }
            }

            return (C) component;
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

        C component = (C) appComponentsByName.get(componentName);

        return (C) component;
    }

}

package co.edu.escuelaing.microspring.server;

import co.edu.escuelaing.microspring.annotations.GetMapping;
import co.edu.escuelaing.microspring.annotations.RestController;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ComponentScanner {

    private static final Logger LOGGER = Logger.getLogger(ComponentScanner.class.getName());

    public Map<String, RouteHandler> scanComponents() {
        Map<String, RouteHandler> routes = new HashMap<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("");

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String path = URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8);
                File directory = new File(path);

                if (directory.exists() && directory.isDirectory()) {
                    List<Class<?>> classes = findClasses(directory, "");
                    for (Class<?> clazz : classes) {
                        registerControllerRoutes(clazz, routes);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error escaneando classpath: " + e.getMessage());
        }

        LOGGER.info("Componentes registrados: " + routes.size() + " rutas encontradas.");
        return routes;
    }

    public Map<String, RouteHandler> loadSingleComponent(String className) {
        Map<String, RouteHandler> routes = new HashMap<>();
        try {
            Class<?> clazz = Class.forName(className);
            registerControllerRoutes(clazz, routes);
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Clase no encontrada: " + className);
        }
        return routes;
    }

    private void registerControllerRoutes(Class<?> clazz, Map<String, RouteHandler> routes) {
        if (!clazz.isAnnotationPresent(RestController.class)) {
            return;
        }

        LOGGER.info("Registrando controlador: " + clazz.getName());

        try {
            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    String route = mapping.value();

                    routes.put(route, new RouteHandler(controllerInstance, method));
                    LOGGER.info("  Ruta registrada: GET " + route + " → " + method.getName() + "()");
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error instanciando controlador " + clazz.getName() + ": " + e.getMessage());
        }
    }

    private List<Class<?>> findClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) return classes;

        File[] files = directory.listFiles();
        if (files == null) return classes;

        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = packageName.isEmpty()
                        ? file.getName()
                        : packageName + "." + file.getName();
                classes.addAll(findClasses(file, subPackage));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName.isEmpty()
                        ? file.getName().replace(".class", "")
                        : packageName + "." + file.getName().replace(".class", "");
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    // Ignorar clases que no se puedan cargar (ej: clases internas)
                }
            }
        }
        return classes;
    }
}

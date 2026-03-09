package co.edu.escuelaing.microspring;

import co.edu.escuelaing.microspring.server.ComponentScanner;
import co.edu.escuelaing.microspring.server.HttpServer;
import co.edu.escuelaing.microspring.server.RouteHandler;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class MicroSpringBoot {

    private static final Logger LOGGER = Logger.getLogger(MicroSpringBoot.class.getName());

    public static void main(String[] args) throws IOException {
        LOGGER.info("Iniciando MicroSpring IoC Framework...");

        ComponentScanner scanner = new ComponentScanner();
        Map<String, RouteHandler> routes;

        if (args.length > 0) {
            LOGGER.info(() -> "Cargando componente: " + args[0]);
            routes = scanner.loadSingleComponent(args[0]);
        } else {
            LOGGER.info("Escaneando classpath...");
            routes = scanner.scanComponents();
        }

        if (routes.isEmpty()) {
            LOGGER.warning("No se encontraron controladores @RestController.");
        }

        HttpServer server = new HttpServer(8080, routes);
        server.start();
    }
}

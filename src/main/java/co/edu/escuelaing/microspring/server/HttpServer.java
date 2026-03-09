package co.edu.escuelaing.microspring.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HttpServer {

    private static final Logger LOGGER = Logger.getLogger(HttpServer.class.getName());
    private static final int DEFAULT_PORT = 8080;
    private static final String STATIC_FOLDER = "src/main/resources/static";

    private final int port;
    private final Map<String, RouteHandler> routes;
    private boolean running = true;

    public HttpServer(int port, Map<String, RouteHandler> routes) {
        this.port = port;
        this.routes = routes;
    }

    public HttpServer(Map<String, RouteHandler> routes) {
        this(DEFAULT_PORT, routes);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOGGER.info("==========================================================");
            LOGGER.info("  MicroSpring IoC Server iniciado en puerto " + port);
            LOGGER.info("  Rutas registradas: " + routes.size());
            routes.forEach((path, handler) ->
                    LOGGER.info("    GET " + path + " → " + handler.getControllerName() + "." + handler.getMethodName() + "()"));
            LOGGER.info("  Servidor de archivos estáticos: /" + STATIC_FOLDER);
            LOGGER.info("==========================================================");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        }
    }

    private void handleRequest(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
            OutputStream dataOut = clientSocket.getOutputStream()
        ) {
            // Leer primera línea: "GET /ruta?params HTTP/1.1"
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            LOGGER.info("Petición: " + requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;

            String method = parts[0];
            String fullPath = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);

            String path = fullPath;
            String queryString = "";

            if (fullPath.contains("?")) {
                int queryStart = fullPath.indexOf("?");
                path = fullPath.substring(0, queryStart);
                queryString = fullPath.substring(queryStart + 1);
            }

            Map<String, String> queryParams = parseQueryParams(queryString);

            if ("GET".equals(method) && routes.containsKey(path)) {
                serveControllerResponse(path, queryParams, out);
            } else {
                serveStaticFile(path, out, dataOut);
            }

        } catch (IOException e) {
            LOGGER.warning("Error manejando petición: " + e.getMessage());
        }
    }

    private void serveControllerResponse(String path, Map<String, String> queryParams, PrintWriter out) {
        RouteHandler handler = routes.get(path);
        try {
            String response = handler.invoke(queryParams);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain; charset=UTF-8");
            out.println("Access-Control-Allow-Origin: *");
            out.println("Connection: close");
            out.println();
            out.println(response);
            out.flush();
        } catch (Exception e) {
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("Error: " + e.getMessage());
            out.flush();
        }
    }

    private void serveStaticFile(String path, PrintWriter out, OutputStream dataOut) throws IOException {
        // La ruta "/" sirve index.html
        if ("/".equals(path)) {
            path = "/index.html";
        }

        File file = new File(STATIC_FOLDER + path);

        if (!file.exists() || file.isDirectory()) {
            String errorPage = buildErrorPage(path);
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html; charset=UTF-8");
            out.println("Content-Length: " + errorPage.getBytes().length);
            out.println();
            out.println(errorPage);
            out.flush();
            return;
        }

        String contentType = getContentType(file.getName());
        byte[] fileData = Files.readAllBytes(file.toPath());

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + fileData.length);
        out.println("Connection: close");
        out.println();
        out.flush();

        dataOut.write(fileData);
        dataOut.flush();
    }

    private Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) return params;

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(
                    URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                );
            } else if (keyValue.length == 1) {
                params.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), "");
            }
        }
        return params;
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css"))  return "text/css";
        if (fileName.endsWith(".js"))   return "application/javascript";
        if (fileName.endsWith(".png"))  return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".ico"))  return "image/x-icon";
        if (fileName.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }

    private String buildErrorPage(String path) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>404 - No encontrado</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #f5f5f5; }
                        h1 { color: #e74c3c; font-size: 72px; margin: 0; }
                        h2 { color: #333; }
                        p { color: #666; }
                        a { color: #3498db; text-decoration: none; }
                    </style>
                </head>
                <body>
                    <h1>404</h1>
                    <h2>Recurso no encontrado</h2>
                    <p>La ruta <code>%s</code> no existe en este servidor.</p>
                    <a href="/">Volver al inicio</a>
                </body>
                </html>
                """.formatted(path);
    }

    public void stop() {
        this.running = false;
    }
}

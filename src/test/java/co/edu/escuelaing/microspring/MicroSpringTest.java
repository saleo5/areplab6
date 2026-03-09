package co.edu.escuelaing.microspring;

import co.edu.escuelaing.microspring.annotations.GetMapping;
import co.edu.escuelaing.microspring.annotations.RequestParam;
import co.edu.escuelaing.microspring.annotations.RestController;
import co.edu.escuelaing.microspring.controllers.GreetingController;
import co.edu.escuelaing.microspring.controllers.HelloController;
import co.edu.escuelaing.microspring.controllers.MathController;
import co.edu.escuelaing.microspring.server.ComponentScanner;
import co.edu.escuelaing.microspring.server.RouteHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MicroSpringTest {

    @Test
    void testRestControllerAnnotationPresentInHelloController() {
        assertTrue(
            HelloController.class.isAnnotationPresent(RestController.class),
            "HelloController debe tener la anotación @RestController"
        );
    }

    @Test
    void testRestControllerAnnotationPresentInGreetingController() {
        assertTrue(
            GreetingController.class.isAnnotationPresent(RestController.class),
            "GreetingController debe tener la anotación @RestController"
        );
    }

    @Test
    void testGetMappingAnnotationOnHelloMethod() throws NoSuchMethodException {
        Method helloMethod = HelloController.class.getDeclaredMethod("hello");
        assertTrue(
            helloMethod.isAnnotationPresent(GetMapping.class),
            "El método hello() debe tener @GetMapping"
        );
        assertEquals("/hello", helloMethod.getAnnotation(GetMapping.class).value(),
            "La ruta del @GetMapping debe ser '/hello'"
        );
    }

    @Test
    void testRequestParamAnnotationOnGreetingMethod() throws NoSuchMethodException {
        Method greetingMethod = GreetingController.class.getDeclaredMethod("greeting", String.class);
        var params = greetingMethod.getParameters();
        assertTrue(
            params[0].isAnnotationPresent(RequestParam.class),
            "El parámetro 'name' debe tener @RequestParam"
        );
        assertEquals("name", params[0].getAnnotation(RequestParam.class).value());
        assertEquals("World", params[0].getAnnotation(RequestParam.class).defaultValue());
    }

    @Test
    void testHelloControllerReturnsExpectedString() throws Exception {
        HelloController controller = new HelloController();
        Method m = HelloController.class.getDeclaredMethod("hello");
        String result = (String) m.invoke(controller);
        assertEquals("Hello, World!", result);
    }

    @Test
    void testIndexControllerReturnsGreeting() throws Exception {
        HelloController controller = new HelloController();
        Method m = HelloController.class.getDeclaredMethod("index");
        String result = (String) m.invoke(controller);
        assertTrue(result.contains("MicroSpring"), "La respuesta debe mencionar MicroSpring");
    }

    @Test
    void testGreetingWithName() {
        GreetingController controller = new GreetingController();
        try {
            Method m = GreetingController.class.getDeclaredMethod("greeting", String.class);
            String result = (String) m.invoke(controller, "Samuel");
            assertEquals("Hola Samuel", result);
        } catch (Exception e) {
            fail("No debe lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    void testGreetingWithDefaultValue() {
        GreetingController controller = new GreetingController();
        try {
            Method m = GreetingController.class.getDeclaredMethod("greeting", String.class);
            String result = (String) m.invoke(controller, "World");
            assertEquals("Hola World", result);
        } catch (Exception e) {
            fail("No debe lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    void testRouteHandlerInvokesWithoutParams() throws NoSuchMethodException {
        HelloController instance = new HelloController();
        Method m = HelloController.class.getDeclaredMethod("hello");
        RouteHandler handler = new RouteHandler(instance, m);

        String result = handler.invoke(new HashMap<>());
        assertEquals("Hello, World!", result);
    }

    @Test
    void testRouteHandlerInjectsRequestParam() throws NoSuchMethodException {
        GreetingController instance = new GreetingController();
        Method m = GreetingController.class.getDeclaredMethod("greeting", String.class);
        RouteHandler handler = new RouteHandler(instance, m);

        Map<String, String> params = new HashMap<>();
        params.put("name", "Samuel");
        String result = handler.invoke(params);
        assertEquals("Hola Samuel", result);
    }

    @Test
    void testRouteHandlerUsesDefaultValueWhenParamMissing() throws NoSuchMethodException {
        GreetingController instance = new GreetingController();
        Method m = GreetingController.class.getDeclaredMethod("greeting", String.class);
        RouteHandler handler = new RouteHandler(instance, m);

        String result = handler.invoke(new HashMap<>());
        assertEquals("Hola World", result);
    }

    @Test
    void testComponentScannerFindsControllers() {
        ComponentScanner scanner = new ComponentScanner();
        Map<String, RouteHandler> routes = scanner.scanComponents();

        assertFalse(routes.isEmpty(), "El escáner debe encontrar al menos un controlador");
        assertTrue(routes.containsKey("/hello"), "Debe registrar la ruta /hello");
        assertTrue(routes.containsKey("/greeting"), "Debe registrar la ruta /greeting");
    }

    @Test
    void testLoadSingleComponent() {
        ComponentScanner scanner = new ComponentScanner();
        Map<String, RouteHandler> routes = scanner.loadSingleComponent(
            "co.edu.escuelaing.microspring.controllers.HelloController"
        );

        assertFalse(routes.isEmpty());
        assertTrue(routes.containsKey("/hello"));
        assertTrue(routes.containsKey("/pi"));
    }

    @Test
    void testMathSqrt() {
        MathController ctrl = new MathController();
        try {
            Method m = MathController.class.getDeclaredMethod("sqrt", String.class);
            String result = (String) m.invoke(ctrl, "16");
            assertTrue(result.contains("4.0"), "sqrt(16) debe contener 4.0");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testMathSqrtNegative() {
        MathController ctrl = new MathController();
        try {
            Method m = MathController.class.getDeclaredMethod("sqrt", String.class);
            String result = (String) m.invoke(ctrl, "-1");
            assertTrue(result.toLowerCase().contains("error"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}

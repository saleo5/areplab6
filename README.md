# MicroSpring

**Autor:** Samuel Albarracín · AREP · Escuela Colombiana de Ingeniería Julio Garavito

---

Básicamente lo que hice acá fue construir un mini-framework web en Java puro, inspirado en cómo funciona Spring Boot internamente. La idea era entender de verdad cómo funciona la Inversión de Control (IoC) usando reflexión y anotaciones, sin depender de ninguna librería externa.

## ¿Cómo funciona?

El punto de partida es simple: en vez de que el programador instancie sus controladores y los registre manualmente, el framework los detecta solo. Cuando arranca, escanea el classpath buscando clases que tengan la anotación @RestController, las instancia, revisa sus métodos con @GetMapping y registra las rutas automáticamente. Todo usando la API de reflexión de Java.

Una vez registradas las rutas, el HttpServer empieza a escuchar en el puerto 8080. Cuando llega una petición GET, busca el handler correspondiente y lo invoca. Si el método tiene parámetros con @RequestParam, el RouteHandler los extrae de la query string e inyecta los valores antes de llamar al método. Si no hay parámetros, llama directamente.

Para archivos estáticos (HTML, CSS, JS, imágenes) simplemente los sirve desde src/main/resources/static/.

## Anotaciones que implementé

- @RestController — va en la clase, le dice al framework que esa clase expone endpoints
- @GetMapping("/ruta") — va en el método, mapea esa ruta HTTP GET al método
- @RequestParam(value = "nombre", defaultValue = "World") — va en los parámetros del método, extrae el query param de la URL

## Requisitos

- Java 17+
- Maven 3.8+

## Cómo correrlo

Primero compilar:

bash
mvn clean package


Luego ejecutar. Hay dos modos:

**Modo automático** (escanea todo el classpath):
bash
java -cp target/classes co.edu.escuelaing.microspring.MicroSpringBoot


**Cargar un controlador específico** (útil para pruebas):
bash
java -cp target/classes co.edu.escuelaing.microspring.MicroSpringBoot co.edu.escuelaing.microspring.controllers.HelloController


Después de eso, abrir http://localhost:8080 y ya aparece la interfaz de prueba.

## Endpoints disponibles

 Ruta Parámetros  Respuesta 
----------------------------
 GET /  —  Greetings from MicroSpring Boot! 
 GET /hello  —  Hello, World! 
 GET /pi  —  El valor de PI es: 3.141592653589793 
 GET /greeting  name (default: World)  Hola Samuel 
 GET /greeting/formal  name (default: World)  Hello, Samuel! (petición #1) 
 GET /math/sqrt  value (default: 0)  La raíz cuadrada de 16.0 es 4.0 
 GET /math/pow  base, exp (defaults: 2,2)  2.0 ^ 8.0 = 256.0 


## Pruebas

bash
mvn test


Escribí 12 pruebas unitarias que cubren: detección de anotaciones con reflexión, invocación de controladores, inyección de @RequestParam, uso del defaultValue cuando no se pasa el parámetro, y el escaneo automático del classpath.


[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS


areplab6/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/co/edu/escuelaing/microspring/
    │   │   ├── MicroSpringBoot.java
    │   │   ├── annotations/
    │   │   │   ├── RestController.java
    │   │   │   ├── GetMapping.java
    │   │   │   └── RequestParam.java
    │   │   ├── server/
    │   │   │   ├── ComponentScanner.java
    │   │   │   ├── HttpServer.java
    │   │   │   └── RouteHandler.java
    │   │   └── controllers/
    │   │       ├── HelloController.java
    │   │       ├── GreetingController.java
    │   │       └── MathController.java
    │   └── resources/static/
    │       └── index.html
    └── test/
        └── java/co/edu/escuelaing/microspring/
            └── MicroSpringTest.java


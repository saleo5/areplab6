package co.edu.escuelaing.microspring.controllers;

import co.edu.escuelaing.microspring.annotations.GetMapping;
import co.edu.escuelaing.microspring.annotations.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Greetings from MicroSpring Boot!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

    @GetMapping("/pi")
    public String pi() {
        return "El valor de PI es: " + Math.PI;
    }
}

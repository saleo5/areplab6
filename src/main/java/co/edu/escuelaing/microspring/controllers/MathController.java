package co.edu.escuelaing.microspring.controllers;

import co.edu.escuelaing.microspring.annotations.GetMapping;
import co.edu.escuelaing.microspring.annotations.RequestParam;
import co.edu.escuelaing.microspring.annotations.RestController;

@RestController
public class MathController {

    @GetMapping("/math/sqrt")
    public String sqrt(@RequestParam(value = "value", defaultValue = "0") String value) {
        try {
            double number = Double.parseDouble(value);
            if (number < 0) return "Error: no se puede calcular raíz de número negativo";
            return String.format("La raíz cuadrada de %s es %s", value, Math.sqrt(number));
        } catch (NumberFormatException e) {
            return "Error: '" + value + "' no es un número válido";
        }
    }

    @GetMapping("/math/pow")
    public String pow(
            @RequestParam(value = "base", defaultValue = "2") String base,
            @RequestParam(value = "exp",  defaultValue = "2") String exp) {
        try {
            double b = Double.parseDouble(base);
            double e = Double.parseDouble(exp);
            return String.format("%s ^ %s = %s", base, exp, Math.pow(b, e));
        } catch (NumberFormatException ex) {
            return "Error: parámetros no válidos";
        }
    }
}

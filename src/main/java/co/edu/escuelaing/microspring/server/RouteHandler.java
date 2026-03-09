package co.edu.escuelaing.microspring.server;

import co.edu.escuelaing.microspring.annotations.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class RouteHandler {

    private final Object controllerInstance;
    private final Method method;

    public RouteHandler(Object controllerInstance, Method method) {
        this.controllerInstance = controllerInstance;
        this.method = method;
    }

    public String invoke(Map<String, String> queryParams) {
        try {
            Parameter[] parameters = method.getParameters();

            if (parameters.length == 0) {
                return (String) method.invoke(controllerInstance);
            }

            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Annotation[] annotations = param.getAnnotations();
                boolean resolved = false;

                for (Annotation annotation : annotations) {
                    if (annotation instanceof RequestParam requestParam) {
                        String paramName = requestParam.value();
                        String defaultValue = requestParam.defaultValue();

                        String value = queryParams.getOrDefault(paramName, defaultValue);
                        args[i] = value;
                        resolved = true;
                        break;
                    }
                }

                if (!resolved) {
                    args[i] = null;
                }
            }

            return (String) method.invoke(controllerInstance, args);

        } catch (Exception e) {
            return "Error invocando " + method.getName() + ": " + e.getCause();
        }
    }

    public String getMethodName() {
        return method.getName();
    }

    public String getControllerName() {
        return controllerInstance.getClass().getSimpleName();
    }
}

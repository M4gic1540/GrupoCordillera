package ${package};

/*
 * Application - Componente.
 * Responsibilities: Logica principal del modulo.
 * Patterns: N/A
 */


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

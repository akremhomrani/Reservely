package tn.reservely.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReservelyBackendApplication {

    public static void main(String[] args) {
        String pgHost = System.getenv("PGHOST");
        String pgUser = System.getenv("PGUSER");
        String port   = System.getenv("PORT");
        System.out.printf("[STARTUP] PORT=%s PGHOST=%s PGUSER=%s%n", port, pgHost, pgUser);
        SpringApplication.run(ReservelyBackendApplication.class, args);
    }
}

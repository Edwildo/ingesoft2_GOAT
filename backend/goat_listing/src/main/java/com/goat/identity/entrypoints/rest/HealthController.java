package com.goat.identity.entrypoints.rest;

import com.goat.identity.adapters.persistence.repository.RoleJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para verificar el estado de la aplicación y la conexión a la base de datos.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final RoleJpaRepository roleJpaRepository;

    public HealthController(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "GOAT Listing Service");
        
        try {
            // Intentar consultar los roles para verificar la conexión a la BD
            long roleCount = roleJpaRepository.count();
            response.put("database", "CONNECTED");
            response.put("roles_count", roleCount);
            response.put("message", "Conexión a la base de datos exitosa");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("database", "DISCONNECTED");
            response.put("error", e.getMessage());
            response.put("message", "Error al conectar con la base de datos");
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getRoles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var roles = roleJpaRepository.findAll();
            response.put("status", "success");
            response.put("count", roles.size());
            response.put("roles", roles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}


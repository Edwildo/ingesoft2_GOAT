package com.goat.navigation.entrypoints.rest;

import com.goat.identity.infrastructure.security.SecurityContextHelper;
import com.goat.navigation.application.dto.GetMenusResponse;
import com.goat.navigation.application.usecases.GetMenusUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de navegación y menús.
 * Entrypoint de la arquitectura hexagonal para Navigation.
 */
@RestController
@RequestMapping("/api/navigation")
@CrossOrigin(origins = "*")
public class NavigationController {
    private final GetMenusUseCase getMenusUseCase;
    private final SecurityContextHelper securityContextHelper;

    public NavigationController(
            GetMenusUseCase getMenusUseCase,
            SecurityContextHelper securityContextHelper) {
        this.getMenusUseCase = getMenusUseCase;
        this.securityContextHelper = securityContextHelper;
    }

    /**
     * Obtiene menús dinámicos según el rol del usuario.
     * Si no hay autenticación, retorna solo menús públicos.
     * Si hay autenticación (JWT válido), incluye automáticamente menús del rol del usuario.
     *
     * @return Respuesta con menús jerárquicos
     */
    @GetMapping("/menus")
    public ResponseEntity<GetMenusResponse> getMenus() {
        // Obtener roles automáticamente del JWT si el usuario está autenticado
        List<String> userRoles = securityContextHelper.getCurrentUserRoles();
        
        GetMenusResponse response = getMenusUseCase.execute(userRoles);
        return ResponseEntity.ok(response);
    }
}

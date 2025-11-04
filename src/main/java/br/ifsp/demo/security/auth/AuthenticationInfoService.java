package br.ifsp.demo.security.auth;

import br.ifsp.demo.security.user.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationInfoService {

   private User getAuthenticatedUserPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("Unauthorized user request.");
        }
        return (User) authentication.getPrincipal();
    }

    public UUID getAuthenticatedUserId() {
        return getAuthenticatedUserPrincipal().getId();
    }

    public String getAuthenticatedStudentId() {
        User user = getAuthenticatedUserPrincipal();
        if (user.getStudent() == null) {
            throw new IllegalStateException("Authenticated user is not linked to a student.");
        }
        return user.getStudent().getId();
    }
}
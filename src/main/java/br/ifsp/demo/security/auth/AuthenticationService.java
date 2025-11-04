package br.ifsp.demo.security.auth;

import br.ifsp.demo.domain.Student;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.exception.EntityAlreadyExistsException;
import br.ifsp.demo.repository.StudentRepository;
import br.ifsp.demo.security.config.JwtService;
import br.ifsp.demo.security.user.JpaUserRepository;
import br.ifsp.demo.security.user.Role;
import br.ifsp.demo.security.user.User;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final JpaUserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public RegisterUserResponse register(RegisterUserRequest request) {

        userRepository.findByEmail(request.email()).ifPresent(unused -> {
            throw new EntityAlreadyExistsException("Email already registered: " + request.email());
        });

        studentRepository.findById(request.studentId()).ifPresent(s -> {
            throw new EntityAlreadyExistsException("Student ID (RA) already registered: " + request.studentId());
        });

        Student newStudent = new Student(request.studentId(), request.name());
        studentRepository.save(newStudent);

        String encryptedPassword = passwordEncoder.encode(request.password());
        final UUID id = UUID.randomUUID();

        final User user = User.builder()
                .id(id)
                .name(request.name())
                .lastname(request.lastname())
                .email(request.email())
                .password(encryptedPassword)
                .role(Role.USER)
                .student(newStudent)
                .build();

        userRepository.save(user);
        return new RegisterUserResponse(id);
    }

    public AuthResponse authenticate(AuthRequest request) {
        final var authentication = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        authenticationManager.authenticate(authentication);

        final User user = userRepository.findByEmail(request.username())
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        final String token = jwtService.generateToken(user);

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getStudent().getId(),
                user.getName() + " " + user.getLastname()
        );

        return new AuthResponse(token, userInfo);
    }
}
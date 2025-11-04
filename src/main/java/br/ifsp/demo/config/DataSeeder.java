package br.ifsp.demo.config;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.ClassSchedule;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.StudentRepository;
import br.ifsp.demo.security.user.JpaUserRepository;
import br.ifsp.demo.security.user.Role;
import br.ifsp.demo.security.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (studentRepository.count() == 0 && userRepository.count() == 0) {
            log.info("Banco de dados vazio. Populando com dados mock...");

            Student student1 = new Student("SP123456", "Vinicius Silva");
            Student student2 = new Student("SP654321", "Maria Oliveira");

            Student student3 = new Student("SP987654", "Aluno Aprovado");
            student3.setCompletedCourses(List.of("MA201"));

            studentRepository.saveAll(List.of(student1, student2, student3));
            log.info("Alunos mock criados.");

            User user1 = User.builder()
                    .id(UUID.randomUUID())
                    .name("Vinicius")
                    .lastname("Silva")
                    .email("vini@email.com")
                    .student(student1)
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.USER)
                    .build();

            User user2 = User.builder()
                    .id(UUID.randomUUID())
                    .name("Maria")
                    .lastname("Oliveira")
                    .email("maria@email.com")
                    .student(student2)
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.USER)
                    .build();

            User user3 = User.builder()
                    .id(UUID.randomUUID())
                    .name("Aluno")
                    .lastname("Aprovado")
                    .email("aprovado@email.com")
                    .student(student3)
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.USER)
                    .build();

            userRepository.saveAll(List.of(user1, user2, user3));
            log.info("Usuários de autenticação mock criados.");

            Course c1 = new Course("CS101", "Introdução à Computação", 4, List.of(), 50);
            c1.setShift("Morning");
            c1.setSchedule(List.of(new ClassSchedule("Monday", "08:00", "10:00")));

            Course c2 = new Course("MA201", "Cálculo I", 4, List.of(), 30);
            c2.setShift("Night");
            c2.setSchedule(List.of(new ClassSchedule("Tuesday", "19:00", "21:00")));

            Course c3 = new Course("CS202", "Estrutura de Dados", 4, List.of("CS101"), 40);
            c3.setShift("Morning");
            c3.setSchedule(List.of(new ClassSchedule("Wednesday", "10:00", "12:00")));

            Course c4 = new Course("MA210", "Álgebra Linear", 4, List.of("MA201"), 35);
            c4.setShift("Night");
            c4.setSchedule(List.of(new ClassSchedule("Wednesday", "21:00", "23:00")));

            Course c5 = new Course("CS210", "Prog. Orientada a Objetos", 4, List.of("CS101"), 40);
            c5.setShift("Morning");
            c5.setSchedule(List.of(new ClassSchedule("Tuesday", "10:00", "12:00")));

            Course c6 = new Course("CS220", "Banco de Dados", 4, List.of("CS202"), 30);
            c6.setShift("Night");
            c6.setSchedule(List.of(new ClassSchedule("Thursday", "19:00", "21:00")));

            Course c7 = new Course("CS404", "Curso Lotado", 4, List.of(), 0);
            c7.setShift("Morning");
            c7.setSchedule(List.of(new ClassSchedule("Friday", "08:00", "10:00")));

            courseRepository.saveAll(List.of(c1, c2, c3, c4, c5, c6, c7));
            log.info("Cursos mock criados (Total: 7).");
        } else {
            log.info("Banco de dados já populado. Seeder não executado.");
        }
    }
}
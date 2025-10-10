// File: br.ifsp.demo.repository.StudentRepository.java

package br.ifsp.demo.repository;

import br.ifsp.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// A chave primária (ID) do Student é uma String, então usamos <Student, String>
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
}
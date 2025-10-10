package br.ifsp.demo.repository;

import br.ifsp.demo.infrastructure.persistence.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, String> { }

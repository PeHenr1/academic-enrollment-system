package br.ifsp.demo.service;

import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.infrastructure.persistence.EnrollmentEntity;
import br.ifsp.demo.mapper.EnrollmentMapper;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CancelEnrollmentService {

    private final EnrollmentRepository repository;

    public CancelEnrollmentService(EnrollmentRepository repository) {
        this.repository = repository;
    }

    public boolean cancelEnrollment(Long id) {
        if (id == null) {
            throw new NullPointerException("ID cannot be null");
        }

        Optional<EnrollmentEntity> entityOpt = repository.findById(id);

        if (entityOpt.isEmpty()) {
            return false;
        }

        Enrollment enrollment = EnrollmentMapper.toDomain(entityOpt.get());

        if (enrollment.isCanceled()) {
            throw new IllegalStateException("Enrollment is already cancelled");
        }

        if (enrollment.getCancellationDeadline().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cancellation deadline has expired");
        }

        enrollment.cancel();

        repository.save(EnrollmentMapper.toEntity(enrollment));
        return true;
    }
}

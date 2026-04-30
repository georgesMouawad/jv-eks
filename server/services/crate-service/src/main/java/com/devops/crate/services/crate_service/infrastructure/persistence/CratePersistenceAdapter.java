package com.devops.crate.services.crate_service.infrastructure.persistence;

import com.devops.crate.services.crate_service.domain.model.Crate;
import com.devops.crate.services.crate_service.domain.repository.CrateRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CratePersistenceAdapter implements CrateRepository {

    private final SpringDataCrateRepository jpaRepository;

    public CratePersistenceAdapter(SpringDataCrateRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Crate save(Crate crate) {
        return jpaRepository.save(crate);
    }

    @Override
    public Optional<Crate> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}

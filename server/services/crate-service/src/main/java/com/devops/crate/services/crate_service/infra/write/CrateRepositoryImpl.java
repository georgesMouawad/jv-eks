package com.devops.crate.services.crate_service.infra.write;

import com.devops.crate.services.crate_service.core.domain.Crate;
import com.devops.crate.services.crate_service.core.read.CrateRepository;
import com.devops.crate.services.crate_service.infra.read.SpringDataCrateRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CrateRepositoryImpl implements CrateRepository {

    private final SpringDataCrateRepository jpaRepository;

    public CrateRepositoryImpl(SpringDataCrateRepository jpaRepository) {
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

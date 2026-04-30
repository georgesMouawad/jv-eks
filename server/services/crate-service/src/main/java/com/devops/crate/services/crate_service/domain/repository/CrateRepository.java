package com.devops.crate.services.crate_service.domain.repository;

import com.devops.crate.services.crate_service.domain.model.Crate;
import java.util.Optional;
import java.util.UUID;

public interface CrateRepository {
    Crate save(Crate crate);

    Optional<Crate> findById(UUID id);
}

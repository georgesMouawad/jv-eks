package com.devops.crate.services.crate_service.core.read;

import com.devops.crate.services.crate_service.core.domain.Crate;
import java.util.Optional;
import java.util.UUID;

public interface CrateRepository {
    Crate save(Crate crate);

    Optional<Crate> findById(UUID id);
}

package com.devops.crate.services.crate_service.infra.read;

import com.devops.crate.services.crate_service.core.domain.Crate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCrateRepository extends JpaRepository<Crate, UUID> {
}

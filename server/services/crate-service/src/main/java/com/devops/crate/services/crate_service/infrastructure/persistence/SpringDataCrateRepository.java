package com.devops.crate.services.crate_service.infrastructure.persistence;

import com.devops.crate.services.crate_service.domain.model.Crate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataCrateRepository extends JpaRepository<Crate, UUID> {
}

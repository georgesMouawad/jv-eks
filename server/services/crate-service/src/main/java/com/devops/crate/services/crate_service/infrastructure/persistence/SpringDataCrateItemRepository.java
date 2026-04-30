package com.devops.crate.services.crate_service.infrastructure.persistence;

import com.devops.crate.services.crate_service.domain.model.CrateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataCrateItemRepository extends JpaRepository<CrateItem, UUID> {
    List<CrateItem> findByCrateId(UUID crateId);
}

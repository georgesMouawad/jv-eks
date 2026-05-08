package com.devops.crate.services.crate_service.infra.read;

import com.devops.crate.services.crate_service.core.domain.CrateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataCrateItemRepository extends JpaRepository<CrateItem, UUID> {
    List<CrateItem> findByCrateId(UUID crateId);
}

package com.devops.crate.services.crate_service.domain.repository;

import com.devops.crate.services.crate_service.domain.model.CrateItem;
import java.util.List;
import java.util.UUID;

public interface CrateItemRepository {
    CrateItem save(CrateItem item);

    List<CrateItem> findByCrateId(UUID crateId);
}

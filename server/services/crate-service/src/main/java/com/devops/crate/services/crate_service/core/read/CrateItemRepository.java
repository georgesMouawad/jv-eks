package com.devops.crate.services.crate_service.core.read;

import com.devops.crate.services.crate_service.core.domain.CrateItem;
import java.util.List;
import java.util.UUID;

public interface CrateItemRepository {
    CrateItem save(CrateItem item);

    List<CrateItem> findByCrateId(UUID crateId);
}

package com.devops.crate.services.crate_service.core.write;

import com.devops.crate.services.crate_service.core.domain.CrateItem;
import java.util.UUID;

public interface CrateEventPublisher {
    void publishTrackAdded(UUID crateId, CrateItem item);
}

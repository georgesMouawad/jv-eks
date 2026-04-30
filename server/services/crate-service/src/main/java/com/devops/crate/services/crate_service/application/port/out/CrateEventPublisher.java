package com.devops.crate.services.crate_service.application.port.out;

import com.devops.crate.services.crate_service.domain.model.CrateItem;
import java.util.UUID;

public interface CrateEventPublisher {
    void publishTrackAdded(UUID crateId, CrateItem item);
}

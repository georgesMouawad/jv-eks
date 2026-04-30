package com.devops.crate.services.crate_service.application.port.in;

import com.devops.crate.services.crate_service.application.dto.*;
import java.util.UUID;

public interface CrateUseCase {
    CrateResponse createCrate(CreateCrateRequest request, UUID ownerId);

    CrateResponse getCrate(UUID crateId);

    UploadUrlResponse getUploadUrl(UUID crateId, String trackName);

    CrateItemResponse addItem(UUID crateId, AddItemRequest request, UUID addedBy);
}

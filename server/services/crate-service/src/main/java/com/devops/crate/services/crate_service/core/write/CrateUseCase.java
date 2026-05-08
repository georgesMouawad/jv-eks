package com.devops.crate.services.crate_service.core.write;

import com.devops.crate.services.crate_service.presentation.read.dto.CrateResponse;
import com.devops.crate.services.crate_service.presentation.read.dto.CrateItemResponse;
import com.devops.crate.services.crate_service.presentation.read.dto.UploadUrlResponse;
import com.devops.crate.services.crate_service.presentation.write.dto.CreateCrateRequest;
import com.devops.crate.services.crate_service.presentation.write.dto.AddItemRequest;
import java.util.UUID;

public interface CrateUseCase {
    CrateResponse createCrate(CreateCrateRequest request, UUID ownerId);

    CrateResponse getCrate(UUID crateId);

    UploadUrlResponse getUploadUrl(UUID crateId, String trackName);

    CrateItemResponse addItem(UUID crateId, AddItemRequest request, UUID addedBy);
}

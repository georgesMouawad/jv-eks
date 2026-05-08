package com.devops.crate.services.crate_service.core.write;

import com.devops.crate.services.crate_service.presentation.read.dto.CrateResponse;
import com.devops.crate.services.crate_service.presentation.read.dto.CrateItemResponse;
import com.devops.crate.services.crate_service.presentation.read.dto.UploadUrlResponse;
import com.devops.crate.services.crate_service.presentation.write.dto.CreateCrateRequest;
import com.devops.crate.services.crate_service.presentation.write.dto.AddItemRequest;
import com.devops.crate.services.crate_service.core.domain.exceptions.CrateNotFoundException;
import com.devops.crate.services.crate_service.core.domain.Crate;
import com.devops.crate.services.crate_service.core.domain.CrateItem;
import com.devops.crate.services.crate_service.core.read.CrateItemRepository;
import com.devops.crate.services.crate_service.core.read.CrateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CrateUseCaseImpl implements CrateUseCase {

    private static final long PRESIGN_EXPIRY_SECONDS = 900; // 15 minutes

    private final CrateRepository crateRepository;
    private final CrateItemRepository crateItemRepository;
    private final Presign presignPort;
    private final CrateEventPublisher eventPublisher;

    public CrateUseCaseImpl(CrateRepository crateRepository,
            CrateItemRepository crateItemRepository,
            Presign presignPort,
            CrateEventPublisher eventPublisher) {
        this.crateRepository = crateRepository;
        this.crateItemRepository = crateItemRepository;
        this.presignPort = presignPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CrateResponse createCrate(CreateCrateRequest request, UUID ownerId) {
        Crate saved = crateRepository.save(Crate.create(request.name(), ownerId));
        return CrateResponse.from(saved, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public CrateResponse getCrate(UUID crateId) {
        Crate crate = crateRepository.findById(crateId)
                .orElseThrow(() -> new CrateNotFoundException(crateId));
        List<CrateItem> items = crateItemRepository.findByCrateId(crateId);
        return CrateResponse.from(crate, items);
    }

    @Override
    @Transactional(readOnly = true)
    public UploadUrlResponse getUploadUrl(UUID crateId, String trackName) {
        crateRepository.findById(crateId)
                .orElseThrow(() -> new CrateNotFoundException(crateId));
        String s3Key = presignPort.generateS3Key(crateId, trackName);
        String url = presignPort.generatePresignedPutUrl(s3Key, PRESIGN_EXPIRY_SECONDS);
        return new UploadUrlResponse(url, s3Key, PRESIGN_EXPIRY_SECONDS);
    }

    @Override
    public CrateItemResponse addItem(UUID crateId, AddItemRequest request, UUID addedBy) {
        crateRepository.findById(crateId)
                .orElseThrow(() -> new CrateNotFoundException(crateId));
        CrateItem saved = crateItemRepository.save(
                CrateItem.create(crateId, request.trackName(), request.s3Key(), addedBy));
        // Notify all clients subscribed to this crate via Redis Pub/Sub
        eventPublisher.publishTrackAdded(crateId, saved);
        return CrateItemResponse.from(saved);
    }
}

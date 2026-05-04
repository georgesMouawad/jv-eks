package com.devops.crate.services.crate_service.application.service;

import com.devops.crate.services.crate_service.application.dto.*;
import com.devops.crate.services.crate_service.application.port.in.CrateUseCase;
import com.devops.crate.services.crate_service.application.port.out.CrateEventPublisher;
import com.devops.crate.services.crate_service.application.port.out.PresignPort;
import com.devops.crate.services.crate_service.domain.exception.CrateNotFoundException;
import com.devops.crate.services.crate_service.domain.model.Crate;
import com.devops.crate.services.crate_service.domain.model.CrateItem;
import com.devops.crate.services.crate_service.domain.repository.CrateItemRepository;
import com.devops.crate.services.crate_service.domain.repository.CrateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CrateApplicationService implements CrateUseCase {

    private static final long PRESIGN_EXPIRY_SECONDS = 900; // 15 minutes

    private final CrateRepository crateRepository;
    private final CrateItemRepository crateItemRepository;
    private final PresignPort presignPort;
    private final CrateEventPublisher eventPublisher;

    public CrateApplicationService(CrateRepository crateRepository,
            CrateItemRepository crateItemRepository,
            PresignPort presignPort,
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

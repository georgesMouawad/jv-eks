package com.devops.crate.services.crate_service.infrastructure.persistence;

import com.devops.crate.services.crate_service.domain.model.CrateItem;
import com.devops.crate.services.crate_service.domain.repository.CrateItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CrateItemPersistenceAdapter implements CrateItemRepository {

    private final SpringDataCrateItemRepository jpaRepository;

    public CrateItemPersistenceAdapter(SpringDataCrateItemRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CrateItem save(CrateItem item) {
        return jpaRepository.save(item);
    }

    @Override
    public List<CrateItem> findByCrateId(UUID crateId) {
        return jpaRepository.findByCrateId(crateId);
    }
}

package com.devops.crate.services.crate_service.infra.write;

import com.devops.crate.services.crate_service.core.domain.CrateItem;
import com.devops.crate.services.crate_service.core.read.CrateItemRepository;
import com.devops.crate.services.crate_service.infra.read.SpringDataCrateItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CrateItemRepositoryImpl implements CrateItemRepository {

    private final SpringDataCrateItemRepository jpaRepository;

    public CrateItemRepositoryImpl(SpringDataCrateItemRepository jpaRepository) {
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

package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.MallProduct;
import com.campusgo.domain.repository.MallProductRepository;
import com.campusgo.infrastructure.persistence.entity.MallProductEntity;
import com.campusgo.infrastructure.persistence.jpa.MallProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MallProductRepositoryImpl implements MallProductRepository {

    private final MallProductJpaRepository jpaRepository;

    @Override
    public List<MallProduct> findAllEnabled() {
        return jpaRepository.findByEnabledTrue()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MallProduct> findById(long id) {
        return jpaRepository.findById(id).map(this::toModel);
    }

    @Override
    public boolean deductStock(long id, int delta) {
        return jpaRepository.findById(id)
                .map(entity -> {
                    if (entity.getStock() < delta) {
                        return false;
                    }
                    entity.setStock(entity.getStock() - delta);
                    jpaRepository.save(entity);
                    return true;
                })
                .orElse(false);
    }

    private MallProduct toModel(MallProductEntity entity) {
        return MallProduct.builder()
                .id(entity.getId())
                .name(entity.getName())
                .subtitle(entity.getSubtitle())
                .category(entity.getCategory())
                .pointsCost(entity.getPointsCost())
                .stock(entity.getStock())
                .emoji(entity.getEmoji())
                .flashSale(entity.isFlashSale())
                .enabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
package com.campusgo.infrastructure.persistence.repository;

import com.campusgo.domain.model.KnowledgeDocument;
import com.campusgo.domain.repository.KnowledgeDocumentRepository;
import com.campusgo.infrastructure.persistence.entity.KnowledgeDocumentEntity;
import com.campusgo.infrastructure.persistence.jpa.KnowledgeDocumentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class KnowledgeDocumentRepositoryImpl implements KnowledgeDocumentRepository {

    private final KnowledgeDocumentJpaRepository jpaRepository;

    @Override
    public KnowledgeDocument save(KnowledgeDocument document) {
        KnowledgeDocumentEntity entity = toEntity(document);
        KnowledgeDocumentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<KnowledgeDocument> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<KnowledgeDocument> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeDocument> findByCategory(String category) {
        return jpaRepository.findByCategory(category).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeDocument> search(String query, int limit) {
        return jpaRepository.searchByFullText(query, limit).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private KnowledgeDocumentEntity toEntity(KnowledgeDocument domain) {
        KnowledgeDocumentEntity entity = new KnowledgeDocumentEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setCategory(domain.getCategory());
        entity.setContent(domain.getContent());
        entity.setTags(domain.getTags());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private KnowledgeDocument toDomain(KnowledgeDocumentEntity entity) {
        return KnowledgeDocument.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .content(entity.getContent())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
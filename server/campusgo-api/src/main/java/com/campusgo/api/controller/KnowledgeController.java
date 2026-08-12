package com.campusgo.api.controller;

import com.campusgo.api.security.AuthUser;
import com.campusgo.application.ai.RagService;
import com.campusgo.domain.model.KnowledgeDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理 API（RAG 知识库）
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final RagService ragService;

    @GetMapping
    public ResponseEntity<List<KnowledgeDocument>> getAll(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(ragService.getDocumentsByCategory(category));
        }
        return ResponseEntity.ok(ragService.getAllDocuments());
    }

    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeDocument>> search(@RequestParam String q) {
        return ResponseEntity.ok(ragService.searchDocuments(q));
    }

    @PostMapping
    public ResponseEntity<KnowledgeDocument> add(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody Map<String, String> body) {
        KnowledgeDocument doc = ragService.addDocument(
                body.get("title"),
                body.getOrDefault("category", "GENERAL"),
                body.get("content"),
                body.get("tags"));
        return ResponseEntity.ok(doc);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        ragService.deleteDocument(id);
        return ResponseEntity.ok().build();
    }
}
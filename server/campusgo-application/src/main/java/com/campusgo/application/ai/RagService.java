package com.campusgo.application.ai;

import com.campusgo.domain.model.KnowledgeDocument;
import com.campusgo.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 检索服务：从知识库中检索相关文档，为 AI 提供上下文
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepo;

    private static final int MAX_RESULTS = 5;
    private static final int MAX_CONTEXT_LENGTH = 3000;

    /**
     * 根据用户问题检索相关知识库内容
     *
     * @param query 用户问题
     * @return 格式化的知识上下文文本
     */
    public String retrieveContext(String query) {
        try {
            List<KnowledgeDocument> results = knowledgeDocumentRepo.search(query, MAX_RESULTS);

            if (results.isEmpty()) {
                log.debug("No knowledge found for query: {}", query);
                return "";
            }

            StringBuilder context = new StringBuilder();
            context.append("以下是与用户问题相关的平台知识库内容：\n\n");

            int totalLength = 0;
            for (int i = 0; i < results.size(); i++) {
                KnowledgeDocument doc = results.get(i);
                String entry = String.format("[%d] %s（%s）\n%s\n\n",
                        i + 1, doc.getTitle(), doc.getCategory(), doc.getContent());

                if (totalLength + entry.length() > MAX_CONTEXT_LENGTH) {
                    break;
                }

                context.append(entry);
                totalLength += entry.length();
            }

            return context.toString();

        } catch (Exception e) {
            log.error("Knowledge retrieval failed", e);
            return "";
        }
    }

    /**
     * 获取所有知识库文档
     */
    public List<KnowledgeDocument> getAllDocuments() {
        return knowledgeDocumentRepo.findAll();
    }

    /**
     * 按分类获取文档
     */
    public List<KnowledgeDocument> getDocumentsByCategory(String category) {
        return knowledgeDocumentRepo.findByCategory(category);
    }

    /**
     * 添加知识文档
     */
    public KnowledgeDocument addDocument(String title, String category, String content, String tags) {
        KnowledgeDocument doc = KnowledgeDocument.builder()
                .title(title)
                .category(category)
                .content(content)
                .tags(tags)
                .build();
        return knowledgeDocumentRepo.save(doc);
    }

    /**
     * 删除知识文档
     */
    public void deleteDocument(Long id) {
        knowledgeDocumentRepo.deleteById(id);
    }

    /**
     * 搜索知识文档
     */
    public List<KnowledgeDocument> searchDocuments(String query) {
        return knowledgeDocumentRepo.search(query, 20);
    }
}
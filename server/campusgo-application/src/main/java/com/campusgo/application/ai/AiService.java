package com.campusgo.application.ai;

import com.campusgo.domain.model.AiConversation;
import com.campusgo.domain.repository.AiConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiConversationRepository aiConversationRepo;
    private final DeepSeekClient deepSeekClient;
    private final RagService ragService;

    @Value("${ai.system-prompt:你是一个校园跑腿平台的智能助手，帮助用户解答关于任务发布、接单、积分、优惠券等问题。}")
    private String systemPrompt;

    @Transactional
    public String chat(Long userId, String sessionId, String userMessage) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        saveMessage(userId, sessionId, "user", userMessage);

        List<AiConversation> history = aiConversationRepo
                .findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);

        List<DeepSeekClient.Message> messages = history.stream()
                .map(conv -> new DeepSeekClient.Message(conv.getRole(), conv.getContent()))
                .collect(Collectors.toList());

        // RAG：检索知识库，获取相关上下文
        String knowledgeContext = ragService.retrieveContext(userMessage);
        String effectiveSystemPrompt = systemPrompt;
        if (!knowledgeContext.isEmpty()) {
            effectiveSystemPrompt = systemPrompt + "\n\n" + knowledgeContext;
            log.info("RAG context added for query: {} ({} chars)", userMessage, knowledgeContext.length());
        }

        messages.add(0, new DeepSeekClient.Message("system", effectiveSystemPrompt));

        String assistantReply = deepSeekClient.chat(messages);

        saveMessage(userId, sessionId, "assistant", assistantReply);

        return assistantReply;
    }

    public List<AiConversation> getHistory(Long userId, String sessionId) {
        return aiConversationRepo.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
    }

    public List<String> getSessionIds(Long userId) {
        return aiConversationRepo.findDistinctSessionIdsByUserId(userId);
    }

    @Transactional
    public void clearSession(Long userId, String sessionId) {
        aiConversationRepo.deleteByUserIdAndSessionId(userId, sessionId);
    }

    private void saveMessage(Long userId, String sessionId, String role, String content) {
        AiConversation conversation = AiConversation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .createdAt(Instant.now())
                .build();
        aiConversationRepo.save(conversation);
    }
}

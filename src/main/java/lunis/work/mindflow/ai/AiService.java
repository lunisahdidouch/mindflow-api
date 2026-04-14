package lunis.work.mindflow.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lunis.work.mindflow.common.ApiException;
import lunis.work.mindflow.config.ChunkingProperties;
import lunis.work.mindflow.study.ChatMessage;
import lunis.work.mindflow.study.ChatMessageRepository;
import lunis.work.mindflow.study.DocumentChunk;
import lunis.work.mindflow.study.DocumentChunkRepository;
import lunis.work.mindflow.study.Flashcard;
import lunis.work.mindflow.study.FlashcardRepository;
import lunis.work.mindflow.study.LearningPlan;
import lunis.work.mindflow.study.LearningPlanRepository;
import lunis.work.mindflow.study.PracticeTest;
import lunis.work.mindflow.study.PracticeTestRepository;
import lunis.work.mindflow.study.QuizQuestion;
import lunis.work.mindflow.study.QuizQuestionRepository;
import lunis.work.mindflow.study.StudyDtos;
import lunis.work.mindflow.study.StudySet;
import lunis.work.mindflow.study.StudySetRepository;
import lunis.work.mindflow.study.StudySetService;
import lunis.work.mindflow.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiService {
    private static final String STUDY_SYSTEM_PROMPT =
            "Je bent een intelligente AI-studieassistent. Analyseer educatieve content en genereer gestructureerde studiematerialen in het Nederlands. Output mag ALTIJD alleen puur JSON zijn.";

    private static final AiChatOptions STRUCTURED_CHAT = AiChatOptions.jsonMode(0.4);
    private static final AiChatOptions STANDARD_CHAT = AiChatOptions.standard(0.4);

    private final AiChatClient aiChatClient;
    private final ObjectMapper objectMapper;
    private final AiJsonSanitizer aiJsonSanitizer;
    private final UserService userService;
    private final StudySetRepository studySetRepository;
    private final FlashcardRepository flashcardRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final PracticeTestRepository practiceTestRepository;
    private final LearningPlanRepository learningPlanRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DocumentChunkingService documentChunkingService;
    private final GeminiEmbeddingService embeddingService;
    private final StudySetService studySetService;
    private final ChunkingProperties chunkingProperties;

    public AiService(
            AiChatClient aiChatClient,
            ObjectMapper objectMapper,
            AiJsonSanitizer aiJsonSanitizer,
            UserService userService,
            StudySetRepository studySetRepository,
            FlashcardRepository flashcardRepository,
            QuizQuestionRepository quizQuestionRepository,
            PracticeTestRepository practiceTestRepository,
            LearningPlanRepository learningPlanRepository,
            DocumentChunkRepository documentChunkRepository,
            ChatMessageRepository chatMessageRepository,
            DocumentChunkingService documentChunkingService,
            GeminiEmbeddingService embeddingService,
            StudySetService studySetService,
            ChunkingProperties chunkingProperties) {
        this.aiChatClient = aiChatClient;
        this.objectMapper = objectMapper;
        this.aiJsonSanitizer = aiJsonSanitizer;
        this.userService = userService;
        this.studySetRepository = studySetRepository;
        this.flashcardRepository = flashcardRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.practiceTestRepository = practiceTestRepository;
        this.learningPlanRepository = learningPlanRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.documentChunkingService = documentChunkingService;
        this.embeddingService = embeddingService;
        this.studySetService = studySetService;
        this.chunkingProperties = chunkingProperties;
    }

    @Transactional
    public StudyDtos.StudySetBundle processDocument(Long userId, AiDtos.ProcessDocumentRequest request) {
        String prompt = """
                Analyseer de volgende studiecontent en geef alleen JSON terug met dit schema:
                {
                  "topic": "string",
                  "summary": "string",
                  "flashcards": [{ "front": "string", "back": "string" }],
                  "quizQuestions": [{ "question": "string", "options": ["a","b","c","d"], "correctAnswerIndex": 0, "explanation": "string" }]
                }
                Genereer 15-20 flashcards en 12-15 quizvragen.

                Bron (%s - %s):
                %s
                """.formatted(request.sourceName(), request.sourceType(), request.sourceText());

        GeneratedStudyMaterial generated = readJson(
                aiChatClient.chat(List.of(
                        new AiChatMessage("system", STUDY_SYSTEM_PROMPT),
                        new AiChatMessage("user", prompt)),
                        STRUCTURED_CHAT),
                GeneratedStudyMaterial.class);

        if (generated.flashcards() == null || generated.flashcards().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI-service leverde geen flashcards terug.");
        }
        if (generated.quizQuestions() == null || generated.quizQuestions().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI-service leverde geen quizvragen terug.");
        }

        StudySet studySet = new StudySet();
        studySet.setUser(userService.getRequiredUser(userId));
        studySet.setTopic(defaultIfBlank(generated.topic(), request.sourceName()));
        studySet.setSummary(defaultIfBlank(generated.summary(), "Samenvatting wordt voorbereid."));
        studySet.setOriginalContent(request.sourceText().trim());
        StudySet savedStudySet = studySetRepository.save(studySet);

        List<Flashcard> cards = generated.flashcards().stream().map(source -> {
            Flashcard card = new Flashcard();
            card.setStudySet(savedStudySet);
            card.setFront(source.front());
            card.setBack(source.back());
            return card;
        }).toList();
        flashcardRepository.saveAll(cards);

        List<QuizQuestion> questions = generated.quizQuestions().stream().map(source -> {
            QuizQuestion question = new QuizQuestion();
            question.setStudySet(savedStudySet);
            question.setQuestion(source.question());
            question.setOptions(source.options());
            question.setCorrectAnswerIndex(source.correctAnswerIndex());
            question.setExplanation(source.explanation());
            return question;
        }).toList();
        quizQuestionRepository.saveAll(questions);

        List<String> chunks = documentChunkingService.chunk(savedStudySet.getOriginalContent());
        List<DocumentChunk> documentChunks = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setStudySet(savedStudySet);
            chunk.setContent(chunks.get(index));
            chunk.setChunkIndex(index);
            chunk.setEmbedding(embeddingService.embed(chunks.get(index)));
            documentChunks.add(chunk);
        }
        documentChunkRepository.saveAll(documentChunks);

        return studySetService.getBundle(savedStudySet.getId(), userId);
    }

    @Transactional
    public StudyDtos.PracticeTestView generatePracticeTest(Long userId, AiDtos.PracticeTestRequest request) {
        StudySet studySet = studySetService.getRequiredStudySet(request.studySetId(), userId);
        int questionCount = request.questionCount() == null ? 10 : request.questionCount();
        String prompt = """
                Maak een practice test in het Nederlands voor dit onderwerp.
                Geef alleen JSON terug met dit schema:
                {
                  "title": "string",
                  "description": "string",
                  "questions": [
                    {
                      "type": "multipleChoice",
                      "question": "string",
                      "options": ["a","b","c","d"],
                      "correctAnswerIndex": 0,
                      "explanation": "string"
                    },
                    {
                      "type": "open",
                      "question": "string",
                      "modelAnswer": "string"
                    }
                  ]
                }
                Focus: %s
                Totaal aantal vragen: %s
                Context:
                %s
                """.formatted(request.focus(), questionCount, studySet.getOriginalContent());

        GeneratedPracticeTest generated = readJson(
                aiChatClient.chat(List.of(
                        new AiChatMessage("system", STUDY_SYSTEM_PROMPT),
                        new AiChatMessage("user", prompt)),
                        STRUCTURED_CHAT),
                GeneratedPracticeTest.class);

        PracticeTest test = new PracticeTest();
        test.setStudySet(studySet);
        test.setTitle(defaultIfBlank(generated.title(), "Practice Test"));
        test.setDescription(defaultIfBlank(generated.description(), "Oefentoets gebaseerd op je document."));
        test.setFocus(request.focus());
        test.setQuestions(generated.questions());

        return studySetService.toPracticeTest(practiceTestRepository.save(test));
    }

    @Transactional
    public StudyDtos.LearningPlanView generateLearningPlan(Long userId, AiDtos.LearningPlanRequest request) {
        StudySet studySet = studySetService.getRequiredStudySet(request.studySetId(), userId);
        int sectionPairs = request.sectionPairs() == null ? 4 : request.sectionPairs();
        String prompt = """
                Maak een leerplan in het Nederlands en geef alleen JSON terug met dit schema:
                {
                  "title": "string",
                  "description": "string",
                  "sections": [
                    { "type": "theory", "title": "string", "markdown": "string" },
                    { "type": "question", "prompt": "string", "modelAnswer": "string" }
                  ]
                }
                Zorg voor %s theorie-vraag paren.
                Context:
                %s
                """.formatted(sectionPairs, studySet.getOriginalContent());

        GeneratedLearningPlan generated = readJson(
                aiChatClient.chat(List.of(
                        new AiChatMessage("system", STUDY_SYSTEM_PROMPT),
                        new AiChatMessage("user", prompt)),
                        STRUCTURED_CHAT),
                GeneratedLearningPlan.class);

        LearningPlan plan = new LearningPlan();
        plan.setStudySet(studySet);
        plan.setTitle(defaultIfBlank(generated.title(), defaultIfBlank(request.title(), "Leerplan")));
        plan.setDescription(defaultIfBlank(generated.description(), "Stapsgewijze studieflow voor dit onderwerp."));
        plan.setSections(generated.sections());

        return studySetService.toLearningPlan(learningPlanRepository.save(plan));
    }

    public AiDtos.GradeQuestionResponse gradeQuestion(Long userId, AiDtos.GradeQuestionRequest request) {
        studySetService.getRequiredStudySet(request.studySetId(), userId);
        String prompt = """
                Beoordeel het studentantwoord en geef alleen JSON terug:
                {
                  "score": 0,
                  "feedback": "string"
                }
                Vraag: %s
                Modelantwoord: %s
                Studentantwoord: %s
                """.formatted(request.question(), request.modelAnswer(), request.studentAnswer());

        return readJson(
                aiChatClient.chat(List.of(
                        new AiChatMessage("system", STUDY_SYSTEM_PROMPT),
                        new AiChatMessage("user", prompt)),
                        STRUCTURED_CHAT),
                AiDtos.GradeQuestionResponse.class);
    }

    @Transactional
    public AiDtos.ChatResponse chat(Long userId, AiDtos.ChatRequest request) {
        StudySet studySet = studySetService.getRequiredStudySet(request.studySetId(), userId);
        float[] queryVector = embeddingService.embed(request.message());
        List<DocumentChunk> contextChunks = documentChunkRepository.findNearestChunks(
                studySet.getId(),
                userId,
                vectorLiteral(queryVector),
                chunkingProperties.retrievalTopK());

        List<ChatMessage> history = chatMessageRepository
                .findTop10ByStudySetIdAndStudySetUserIdOrderByCreatedAtDesc(studySet.getId(), userId).stream()
                .sorted((left, right) -> left.getCreatedAt().compareTo(right.getCreatedAt()))
                .toList();

        String context = contextChunks.isEmpty()
                ? truncate(studySet.getOriginalContent(), 4000)
                : contextChunks.stream().map(DocumentChunk::getContent).collect(Collectors.joining("\n\n"));

        List<AiChatMessage> messages = new ArrayList<>();
        messages.add(new AiChatMessage(
                "system",
                "Je bent een behulpzame AI-tutor die vragen beantwoordt over studiecontent in het Nederlands. Gebruik de volgende context: "
                        + context + " Wees behulpzaam en leg concepten eenvoudig uit."));
        history.forEach(message -> messages.add(new AiChatMessage(
                "model".equals(message.getRole()) ? "assistant" : "user",
                message.getContent())));
        messages.add(new AiChatMessage("user", request.message()));

        String reply = aiChatClient.chat(messages, STANDARD_CHAT);

        ChatMessage userMessage = new ChatMessage();
        userMessage.setStudySet(studySet);
        userMessage.setRole("user");
        userMessage.setContent(request.message());

        ChatMessage modelMessage = new ChatMessage();
        modelMessage.setStudySet(studySet);
        modelMessage.setRole("model");
        modelMessage.setContent(reply);

        chatMessageRepository.save(userMessage);
        chatMessageRepository.save(modelMessage);

        return new AiDtos.ChatResponse(reply);
    }

    private <T> T readJson(String raw, Class<T> type) {
        try {
            return objectMapper.readValue(aiJsonSanitizer.sanitize(raw), type);
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Kon AI-output niet verwerken als JSON.");
        }
    }

    private String vectorLiteral(float[] values) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}

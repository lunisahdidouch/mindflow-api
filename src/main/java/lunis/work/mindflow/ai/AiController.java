package lunis.work.mindflow.ai;

import jakarta.validation.Valid;
import lunis.work.mindflow.security.AuthenticatedUser;
import lunis.work.mindflow.study.StudyDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/process-document")
    StudyDtos.StudySetBundle processDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AiDtos.ProcessDocumentRequest request) {
        return aiService.processDocument(user.id(), request);
    }

    @PostMapping("/practice-test")
    StudyDtos.PracticeTestView practiceTest(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AiDtos.PracticeTestRequest request) {
        return aiService.generatePracticeTest(user.id(), request);
    }

    @PostMapping("/learning-plan")
    StudyDtos.LearningPlanView learningPlan(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AiDtos.LearningPlanRequest request) {
        return aiService.generateLearningPlan(user.id(), request);
    }

    @PostMapping("/grade-question")
    AiDtos.GradeQuestionResponse gradeQuestion(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AiDtos.GradeQuestionRequest request) {
        return aiService.gradeQuestion(user.id(), request);
    }

    @PostMapping("/chat")
    AiDtos.ChatResponse chat(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AiDtos.ChatRequest request) {
        return aiService.chat(user.id(), request);
    }
}

package lunis.work.mindflow.study;

import java.util.List;
import lunis.work.mindflow.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-sets")
public class StudySetController {
    private final StudySetService studySetService;

    public StudySetController(StudySetService studySetService) {
        this.studySetService = studySetService;
    }

    @GetMapping
    List<StudyDtos.StudySetListItem> listStudySets(@AuthenticationPrincipal AuthenticatedUser user) {
        return studySetService.listStudySets(user.id());
    }

    @GetMapping("/{studySetId}")
    StudyDtos.StudySetOverview overview(
            @PathVariable Long studySetId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return studySetService.getOverview(studySetId, user.id());
    }

    @GetMapping("/{studySetId}/flashcards")
    List<StudyDtos.FlashcardView> flashcards(
            @PathVariable Long studySetId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return studySetService.getFlashcards(studySetId, user.id());
    }

    @GetMapping("/{studySetId}/quiz")
    List<StudyDtos.QuizQuestionView> quiz(
            @PathVariable Long studySetId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return studySetService.getQuiz(studySetId, user.id());
    }

    @GetMapping("/{studySetId}/practice-tests/latest")
    StudyDtos.PracticeTestView latestPracticeTest(
            @PathVariable Long studySetId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return studySetService.getLatestPracticeTest(studySetId, user.id());
    }

    @GetMapping("/{studySetId}/learning-plans/latest")
    StudyDtos.LearningPlanView latestLearningPlan(
            @PathVariable Long studySetId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return studySetService.getLatestLearningPlan(studySetId, user.id());
    }

    @GetMapping("/{studySetId}/chat-messages")
    List<StudyDtos.ChatMessageView> chatMessages(
            @PathVariable Long studySetId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return studySetService.getChatMessages(studySetId, user.id());
    }

    @DeleteMapping("/{studySetId}")
    void deleteStudySet(
            @PathVariable Long studySetId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        studySetService.deleteStudySet(studySetId, user.id());
    }
}

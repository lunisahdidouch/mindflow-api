package lunis.work.mindflow.study;

import java.time.Instant;
import java.util.List;

public final class StudyDtos {
    private StudyDtos() {
    }

    public record StudySetListItem(
            Long id,
            String topic,
            String summary,
            Instant createdAt,
            long flashcardCount,
            long quizQuestionCount) {
    }

    public record StudySetOverview(Long id, String topic, String summary, String originalContent, Instant createdAt) {
    }

    public record FlashcardView(Long id, String front, String back) {
    }

    public record QuizQuestionView(
            Long id,
            String question,
            List<String> options,
            Integer correctAnswerIndex,
            String explanation) {
    }

    public record PracticeTestView(
            Long id,
            String title,
            String description,
            String focus,
            List<PracticeTestQuestionData> questions,
            Instant createdAt) {
    }

    public record LearningPlanView(
            Long id,
            String title,
            String description,
            List<LearningPlanSectionData> sections,
            Instant createdAt) {
    }

    public record ChatMessageView(Long id, String role, String content, Instant createdAt) {
    }

    public record StudySetBundle(
            StudySetOverview studySet,
            List<FlashcardView> flashcards,
            List<QuizQuestionView> quizQuestions) {
    }
}

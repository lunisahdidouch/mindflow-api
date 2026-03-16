package lunis.work.mindflow.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneratedStudyMaterial(
        String topic,
        String summary,
        List<GeneratedFlashcard> flashcards,
        List<GeneratedQuizQuestion> quizQuestions) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeneratedFlashcard(String front, String back) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeneratedQuizQuestion(
            String question,
            List<String> options,
            Integer correctAnswerIndex,
            String explanation) {
    }
}

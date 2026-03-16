package lunis.work.mindflow.ai;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AiDtos {
    private AiDtos() {
    }

    public record ProcessDocumentRequest(
            @NotBlank(message = "Brontekst is verplicht.") String sourceText,
            @NotBlank(message = "Bestandsnaam is verplicht.") String sourceName,
            @NotBlank(message = "Bronsoort is verplicht.") String sourceType) {
    }

    public record PracticeTestRequest(
            @NotNull(message = "Study set ID is verplicht.") Long studySetId,
            @NotBlank(message = "Focus is verplicht.") String focus,
            @Min(value = 4, message = "Minstens 4 vragen.") @Max(value = 20, message = "Maximaal 20 vragen.") Integer questionCount) {
    }

    public record LearningPlanRequest(
            @NotNull(message = "Study set ID is verplicht.") Long studySetId,
            String title,
            @Min(value = 2, message = "Minstens 2 sectieparen.") @Max(value = 8, message = "Maximaal 8 sectieparen.") Integer sectionPairs) {
    }

    public record GradeQuestionRequest(
            @NotNull(message = "Study set ID is verplicht.") Long studySetId,
            @NotBlank(message = "Vraag is verplicht.") String question,
            @NotBlank(message = "Modelantwoord is verplicht.") String modelAnswer,
            @NotBlank(message = "Studentantwoord is verplicht.") String studentAnswer) {
    }

    public record ChatRequest(
            @NotNull(message = "Study set ID is verplicht.") Long studySetId,
            @NotBlank(message = "Bericht is verplicht.") String message) {
    }

    public record GradeQuestionResponse(int score, String feedback) {
    }

    public record ChatResponse(String reply) {
    }
}

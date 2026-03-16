package lunis.work.mindflow.study;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PracticeTestQuestionData(
        String type,
        String question,
        List<String> options,
        Integer correctAnswerIndex,
        String explanation,
        String modelAnswer) {
}

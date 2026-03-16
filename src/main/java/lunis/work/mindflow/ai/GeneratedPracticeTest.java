package lunis.work.mindflow.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lunis.work.mindflow.study.PracticeTestQuestionData;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneratedPracticeTest(String title, String description, List<PracticeTestQuestionData> questions) {
}

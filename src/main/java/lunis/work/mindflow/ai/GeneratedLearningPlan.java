package lunis.work.mindflow.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lunis.work.mindflow.study.LearningPlanSectionData;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneratedLearningPlan(String title, String description, List<LearningPlanSectionData> sections) {
}

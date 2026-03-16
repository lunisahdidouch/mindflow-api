package lunis.work.mindflow.study;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LearningPlanSectionData(
        String type,
        String title,
        String markdown,
        String prompt,
        String modelAnswer) {
}

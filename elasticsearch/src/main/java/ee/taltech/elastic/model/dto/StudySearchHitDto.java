package ee.taltech.elastic.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public final class StudySearchHitDto {

    private String id;
    private String seriesTitle;
    private String universeLabel;
    private String title;
    private String purpose;
    private String referenceArea;
    private String timeCoverage;
    private List<VariableSearchHitDto> variables;
}

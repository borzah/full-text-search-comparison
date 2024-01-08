package ee.taltech.fulltextsearchcomparison.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class StudySearchHitDto {

    private UUID studyId;
    private String seriesTitle;
    private String universeLabel;
    private String title;
    private String purpose;
    private String referenceArea;
    private String timeCoverage;
    private List<VariableSearchHitDto> variables;

    public StudySearchHitDto(StudySearchHitDto studyHit, List<VariableSearchHitDto> variables) {
        this.studyId = studyHit.getStudyId();
        this.seriesTitle = studyHit.getSeriesTitle();
        this.universeLabel = studyHit.getUniverseLabel();
        this.title = studyHit.getTitle();
        this.purpose = studyHit.getPurpose();
        this.referenceArea = studyHit.getReferenceArea();
        this.timeCoverage = studyHit.getTimeCoverage();
        this.variables = variables;
    }
}

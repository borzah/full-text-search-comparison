package ee.taltech.fulltextsearchcomparison.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}

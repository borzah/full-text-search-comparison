package ee.taltech.fulltextsearchcomparison.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudySearchDataDto {

    private UUID studyId;
    private String seriesTitle;
    private String universeLabel;
    private String title;
    private String purpose;
    private String referenceArea;
    private String timeCoverage;
}

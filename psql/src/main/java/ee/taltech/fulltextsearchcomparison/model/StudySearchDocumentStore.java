package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StudySearchDocumentStore {

    private UUID studyId;
    private String seriesTitle;
    private String universeLabel;
    private String title;
    private String purpose;
    private String referenceArea;
    private String timeCoverage;
    private String studySearchDocument;
}

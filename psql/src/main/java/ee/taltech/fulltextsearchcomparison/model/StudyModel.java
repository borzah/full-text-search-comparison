package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StudyModel {

    private UUID id;

    private UUID metadataDomainId;

    private UUID seriesId;

    private UUID subdomainId;

    private String seriesTitle;

    private UUID universeId;

    private String universeLabel;

    private Boolean isAdminData;

    private String title;

    private String summary;

    private String purpose;

    private String studyCode;

    private String contactName;

    private String contactEmailAddress;

    private String sectorCoverage;

    private String referenceArea;

    private String timeCoverage;

    private String otherDissemination;

    private String documentationOnMethodology;

    private String geographicalComparability;

    private String comparabilityOverTime;

    private String sourceData;

    private String frequencyOfDataCollection;

    private String dataCollection;

    private String dataValidation;

    private String dataCompilation;

}

package ee.taltech.fulltextsearchcomparison.metadata;

import java.util.List;

public record MetadataStudy(
        String id,
        String domainId,
        String subdomainId,
        String seriesId,
        String seriesTitle,
        String universeId,
        String universeLabel,
        Boolean isAdminData,
        String title,
        String summary,
        String purpose,
        String studyCode,
        String contactName,
        String contactEmailAddress,
        String sectorCoverage,
        String referenceArea,
        String timeCoverage,
        String otherDissemination,
        String documentationOnMethodology,
        String geographicalComparability,
        String comparabilityOverTime,
        String sourceData,
        String frequencyOfDataCollection,
        String dataCollection,
        String dataValidation,
        String dataCompilation,
        List<MetadataOtherMaterial> otherMaterials,
        List<MetadataDataFile> dataFiles
) {
    public MetadataStudy(MetadataStudy study) {
        this(
                study.id(),
                study.domainId(),
                study.subdomainId(),
                study.seriesId(),
                study.seriesTitle(),
                study.universeId(),
                study.universeLabel(),
                study. isAdminData(),
                study.title(),
                study.summary(),
                study.purpose(),
                study.studyCode(),
                study.contactName(),
                study.contactEmailAddress(),
                study.sectorCoverage(),
                study.referenceArea(),
                study.timeCoverage(),
                study.otherDissemination(),
                study.documentationOnMethodology(),
                study.geographicalComparability(),
                study.comparabilityOverTime(),
                study.sourceData(),
                study.frequencyOfDataCollection(),
                study.dataCollection(),
                study.dataValidation(),
                study.dataCompilation(),
                null,
                null
                );
    }
}

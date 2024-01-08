package ee.taltech.elastic.model.metadata;

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
) { }

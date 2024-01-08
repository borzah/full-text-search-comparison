package ee.taltech.elastic.model.metadata;

import java.util.List;

public record MetadataDataFile(
        String id,
        String title,
        MetadataTemporalCoverage temporalCoverage,
        List<MetadataSubject> subjects,
        List<MetadataKeyword> keywords,
        List<MetadataLogicalRecord> logicalRecords
) { }

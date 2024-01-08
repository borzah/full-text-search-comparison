package ee.taltech.fulltextsearchcomparison.metadata;

import java.util.List;

public record MetadataLogicalRecord(
        String id,
        String name,
        String label,
        String description,
        String databaseUrl,
        Long numberOfEntries,
        List<MetadataQualityIndicator> qualityIndicators,
        List<MetadataVariable> variables
) { }

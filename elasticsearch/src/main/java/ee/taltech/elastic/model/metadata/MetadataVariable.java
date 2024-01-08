package ee.taltech.elastic.model.metadata;

import java.util.List;

public record MetadataVariable(
        String id,
        String codeListId,
        String unitTypeId,
        String unitTypeLabel,
        String name,
        String label,
        String description,
        String representationType,
        String type,
        Boolean variableIsAWeight,
        Boolean blankvaluesRepresentMissingvalues,
        String missingvalues,
        String measurementUnit,
        String variableRole,
        String representedVariableLabel,
        String conceptualVariableLabel,
        Double percentageOfFilledEntries,
        List<MetadataConcept> concepts,
        List<MetadataQualityIndicator> qualityIndicators
) { }

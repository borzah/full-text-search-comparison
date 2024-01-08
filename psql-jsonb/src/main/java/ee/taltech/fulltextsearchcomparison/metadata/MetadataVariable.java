package ee.taltech.fulltextsearchcomparison.metadata;

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
) {
    public MetadataVariable(MetadataVariable variableDto) {
        this(
                variableDto.id(),
                variableDto.codeListId(),
                variableDto.unitTypeId(),
                variableDto.unitTypeLabel(),
                variableDto.name(),
                variableDto.label(),
                variableDto.description(),
                variableDto.representationType(),
                variableDto.type(),
                variableDto.variableIsAWeight(),
                variableDto.blankvaluesRepresentMissingvalues(),
                variableDto.missingvalues(),
                variableDto.measurementUnit(),
                variableDto.variableRole(),
                variableDto.representedVariableLabel(),
                variableDto.conceptualVariableLabel(),
                variableDto.percentageOfFilledEntries(),
                null,
                null
        );
    }
}

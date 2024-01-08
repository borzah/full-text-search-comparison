package ee.taltech.elastic.model.metadata;

public record MetadataCategory(
        String id,
        String label,
        String description,
        String codeValue
) { }

package ee.taltech.fulltextsearchcomparison.metadata;

public record MetadataCategory(
        String id,
        String label,
        String description,
        String codeValue
) {
    public MetadataCategory(MetadataCategory category) {
        this(category.id(), category.label(), category.description(), category.codeValue());
    }
}

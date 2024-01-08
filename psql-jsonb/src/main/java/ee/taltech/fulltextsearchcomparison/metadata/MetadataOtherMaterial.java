package ee.taltech.fulltextsearchcomparison.metadata;

public record MetadataOtherMaterial(
        String title,
        String url
) {
    public MetadataOtherMaterial(MetadataOtherMaterial otherMaterial) {
        this(otherMaterial.title(), otherMaterial.url());
    }
}

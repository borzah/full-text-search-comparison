package ee.taltech.fulltextsearchcomparison.metadata;

import java.util.List;

public record MetadataCodeList(
        String id,
        String label,
        String description,
        List<MetadataCategory> categories
) {
    public MetadataCodeList(MetadataCodeList codeList) {
        this(codeList.id(), codeList.label(), codeList.description(), null);
    }
}

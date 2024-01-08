package ee.taltech.elastic.model.metadata;

import java.util.List;

public record MetadataCodeList(
        String id,
        String label,
        String description,
        List<MetadataCategory> categories
) { }

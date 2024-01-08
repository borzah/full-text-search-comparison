package ee.taltech.fulltextsearchcomparison.metadata;

import java.util.List;

public record MetadataSeries(
        String id,
        String title,
        List<MetadataStudy> studies
) { }



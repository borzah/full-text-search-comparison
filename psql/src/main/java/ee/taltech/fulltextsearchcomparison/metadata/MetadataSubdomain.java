package ee.taltech.fulltextsearchcomparison.metadata;

import java.util.List;

public record MetadataSubdomain(
        String id,
        String label,
        List<MetadataSeries> series
) { }

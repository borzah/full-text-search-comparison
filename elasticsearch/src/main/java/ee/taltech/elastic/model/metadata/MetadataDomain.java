package ee.taltech.elastic.model.metadata;

import java.util.List;

public record MetadataDomain(
        String id,
        String label,
        List<MetadataSubdomain> subdomains
) { }

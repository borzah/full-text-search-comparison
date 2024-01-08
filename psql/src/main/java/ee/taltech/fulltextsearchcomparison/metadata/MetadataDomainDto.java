package ee.taltech.fulltextsearchcomparison.metadata;

import java.util.List;

public record MetadataDomainDto(
        String id,
        String label,
        List<MetadataSubdomain> subdomains
) { }

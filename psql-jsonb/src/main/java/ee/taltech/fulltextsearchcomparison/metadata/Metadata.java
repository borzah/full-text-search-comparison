package ee.taltech.fulltextsearchcomparison.metadata;


import java.util.List;

public record Metadata(
        List<MetadataDomainDto> domains,
        List<MetadataCodeList> codeLists
) {
}

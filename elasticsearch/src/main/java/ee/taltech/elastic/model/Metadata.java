package ee.taltech.elastic.model;

import ee.taltech.elastic.model.metadata.MetadataCodeList;
import ee.taltech.elastic.model.metadata.MetadataDomain;

import java.util.List;

public record Metadata(
        List<MetadataDomain> domains,
        List<MetadataCodeList> codeLists
) {
}

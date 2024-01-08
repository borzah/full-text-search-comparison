package ee.taltech.fulltextsearchcomparison.mapper;

import ee.taltech.fulltextsearchcomparison.metadata.MetadataCategory;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataCodeList;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataConcept;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataDataFile;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataDomainDto;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataKeyword;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataLogicalRecord;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataOtherMaterial;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataQualityIndicator;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataSeries;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataStudy;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataSubdomain;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataSubject;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataTemporalCoverage;
import ee.taltech.fulltextsearchcomparison.metadata.MetadataVariable;
import ee.taltech.fulltextsearchcomparison.model.CategoryModel;
import ee.taltech.fulltextsearchcomparison.model.CodeListModel;
import ee.taltech.fulltextsearchcomparison.model.ConceptModel;
import ee.taltech.fulltextsearchcomparison.model.DataFileModel;
import ee.taltech.fulltextsearchcomparison.model.KeywordModel;
import ee.taltech.fulltextsearchcomparison.model.LogicalRecordModel;
import ee.taltech.fulltextsearchcomparison.model.MetadataDomainModel;
import ee.taltech.fulltextsearchcomparison.model.OtherMaterialModel;
import ee.taltech.fulltextsearchcomparison.model.QualityIndicatorModel;
import ee.taltech.fulltextsearchcomparison.model.SeriesModel;
import ee.taltech.fulltextsearchcomparison.model.StudyModel;
import ee.taltech.fulltextsearchcomparison.model.SubdomainModel;
import ee.taltech.fulltextsearchcomparison.model.SubjectModel;
import ee.taltech.fulltextsearchcomparison.model.VariableModel;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;

import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = IGNORE)
public interface DtoMapper {

    CodeListModel dtoToCodeListModel(MetadataCodeList dto);

    CategoryModel dtoToCategoryModel(MetadataCategory dto);

    MetadataDomainModel dtoToMetadataDomainModel(MetadataDomainDto dto);

    SubdomainModel dtoToSubdomainModel(MetadataSubdomain dto);

    SeriesModel dtoToSeriesModel(MetadataSeries dto);

    StudyModel dtoToStudyModel(MetadataStudy dto);

    OtherMaterialModel dtoToOtherMaterialModel(MetadataOtherMaterial dto);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "beginDate", source = "temporalCoverage.beginDate")
    @Mapping(target = "endDate", source = "temporalCoverage.endDate")
    DataFileModel dtoToDataFileModel(MetadataDataFile dto, MetadataTemporalCoverage temporalCoverage);

    KeywordModel dtoToKeywordModel(MetadataKeyword dto);

    SubjectModel dtoToSubjectModel(MetadataSubject dto);

    LogicalRecordModel dtoToLogicalRecordModel(MetadataLogicalRecord dto);

    VariableModel dtoToVariableModel(MetadataVariable dto);

    ConceptModel dtoToConceptModel(MetadataConcept dto);

    QualityIndicatorModel dtoToQualityIndicatorModel(MetadataQualityIndicator dto);
}

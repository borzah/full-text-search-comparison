package ee.taltech.elastic.model.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.index.nested.DataFileNested;
import ee.taltech.elastic.model.index.nested.OtherMaterialNested;
import ee.taltech.elastic.model.metadata.MetadataStudy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Document(indexName = "study")
@Setting(settingPath = "/elastic_settings.json")
@Getter
@Setter
@JsonInclude(NON_NULL)
public class StudyIndex {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String seriesId;

    @Field(type = FieldType.Keyword)
    private String subdomainId;

    @Field(type = FieldType.Keyword)
    private String domainId;

    @Field(type = FieldType.Text)
    private String seriesTitle;

    @Field(type = FieldType.Text)
    private String universeLabel;

    @Field(type = FieldType.Boolean, index = false)
    private Boolean isAdminData;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Text)
    private String purpose;

    @Field(type = FieldType.Text)
    private String studyCode;

    @Field(type = FieldType.Text, index = false)
    private String contactName;

    @Field(type = FieldType.Keyword, index = false)
    private String contactEmailAddress;

    @Field(type = FieldType.Text)
    private String sectorCoverage;

    @Field(type = FieldType.Text, index = false)
    private String referenceArea;

    @Field(type = FieldType.Text, index = false)
    private String timeCoverage;

    @Field(type = FieldType.Text)
    private String otherDissemination;

    @Field(type = FieldType.Text)
    private String documentationOnMethodology;

    @Field(type = FieldType.Text)
    private String geographicalComparability;

    @Field(type = FieldType.Text)
    private String comparabilityOverTime;

    @Field(type = FieldType.Text)
    private String sourceData;

    @Field(type = FieldType.Text)
    private String frequencyOfDataCollection;

    @Field(type = FieldType.Text)
    private String dataCollection;

    @Field(type = FieldType.Text)
    private String dataValidation;

    @Field(type = FieldType.Text)
    private String dataCompilation;

    @Field(type = FieldType.Object)
    private List<OtherMaterialNested> otherMaterials;

    @Field(type = FieldType.Nested)
    private List<DataFileNested> dataFiles;

    public static StudyIndex createStudyIndex(MetadataStudy metadataStudy) {
        StudyIndex studyIndex = new StudyIndex();
        studyIndex.setId(metadataStudy.id());
        studyIndex.setSeriesId(metadataStudy.seriesId());
        studyIndex.setSubdomainId(metadataStudy.subdomainId());
        studyIndex.setDomainId(metadataStudy.domainId());
        studyIndex.setSeriesTitle(metadataStudy.seriesTitle());
        studyIndex.setUniverseLabel(metadataStudy.universeLabel());
        studyIndex.setIsAdminData(metadataStudy.isAdminData());
        studyIndex.setTitle(metadataStudy.title());
        studyIndex.setSummary(metadataStudy.summary());
        studyIndex.setPurpose(metadataStudy.purpose());
        studyIndex.setStudyCode(metadataStudy.studyCode());
        studyIndex.setContactName(metadataStudy.contactName());
        studyIndex.setContactEmailAddress(metadataStudy.contactEmailAddress());
        studyIndex.setSectorCoverage(metadataStudy.sectorCoverage());
        studyIndex.setReferenceArea(metadataStudy.referenceArea());
        studyIndex.setTimeCoverage(metadataStudy.timeCoverage());
        studyIndex.setOtherDissemination(metadataStudy.otherDissemination());
        studyIndex.setDocumentationOnMethodology(metadataStudy.documentationOnMethodology());
        studyIndex.setGeographicalComparability(metadataStudy.geographicalComparability());
        studyIndex.setComparabilityOverTime(metadataStudy.comparabilityOverTime());
        studyIndex.setSourceData(metadataStudy.sourceData());
        studyIndex.setFrequencyOfDataCollection(metadataStudy.frequencyOfDataCollection());
        studyIndex.setDataCollection(metadataStudy.dataCollection());
        studyIndex.setDataValidation(metadataStudy.dataValidation());
        studyIndex.setDataCompilation(metadataStudy.dataCompilation());
        studyIndex.setOtherMaterials(metadataStudy.otherMaterials().stream().map(OtherMaterialNested::createOtherMaterialNested).toList());
        studyIndex.setDataFiles(metadataStudy.dataFiles().stream().map(DataFileNested::createDataFileNested).toList());
        return studyIndex;
    }
}

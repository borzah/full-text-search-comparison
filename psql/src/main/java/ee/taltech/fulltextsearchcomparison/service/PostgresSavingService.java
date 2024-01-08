package ee.taltech.fulltextsearchcomparison.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.fulltextsearchcomparison.metadata.Metadata;
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
import ee.taltech.fulltextsearchcomparison.model.StudySearchDocumentStore;
import ee.taltech.fulltextsearchcomparison.mapper.DtoMapper;
import ee.taltech.fulltextsearchcomparison.model.SubdomainModel;
import ee.taltech.fulltextsearchcomparison.model.SubjectModel;
import ee.taltech.fulltextsearchcomparison.model.VariableModel;
import ee.taltech.fulltextsearchcomparison.model.VariableSearchDocumentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class PostgresSavingService {

    private static final String CATEGORY_INSERT = "insert into category (code_list_id,code_value,description,label,category_id) values (?,?,?,?,?)";
    private static final String CODE_LIST_INSERT = "insert into code_list (description,label,code_list_id) values (?,?,?)";
    private static final String METADATA_DOMAIN_INSERT = "insert into metadata_domain (label,metadata_domain_id) values (?,?)";
    private static final String SUBDOMAIN_INSERT = "insert into subdomain (label,metadata_domain_id,subdomain_id) values (?,?,?)";
    private static final String SERIES_INSERT = "insert into series (subdomain_id,title,series_id) values (?,?,?)";
    private static final String STUDY_INSERT = "insert into study (comparability_over_time,contact_email_address,contact_name,data_collection,data_compilation,data_validation,documentation_on_methodology,frequency_of_data_collection,geographical_comparability,is_admin_data,metadata_domain_id,other_dissemination,purpose,reference_area,sector_coverage,series_id,series_title,source_data,study_code,subdomain_id,summary,time_coverage,title,universe_id,universe_label,study_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String OTHER_MATERIAL_INSERT = "insert into other_material (study_id,title,other_material_id) values (?,?,?)";
    private static final String DATA_FILE_INSERT = "insert into data_file (begin_date,end_date,study_id,title,data_file_id) values (?,?,?,?,?)";
    private static final String KEYWORD_INSERT = "insert into keyword (data_file_id,name,keyword_id) values (?,?,?)";
    private static final String SUBJECT_INSERT = "insert into subject (data_file_id,name,subject_id) values (?,?,?)";
    private static final String LOGICAL_RECORD_INSERT = "insert into logical_record (data_file_id,database_url,description,label,name,number_of_entries,logical_record_id) values (?,?,?,?,?,?,?)";
    private static final String QUALITY_INDICATOR_INSERT = "insert into quality_indicator (logical_record_id,variable_id,name,label,quality_indicator_id) values (?,?,?,?,?)";
    private static final String VARIABLE_INSERT = "insert into variable (blank_values_represent_missing_values,code_list_id,conceptual_variable_label,description,label,logical_record_id,measurement_unit,missing_values,name,percentage_of_filled_entries,representation_type,represented_variable_label,type,unit_type_id,unit_type_label,variable_is_a_weight,variable_role,variable_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String CONCEPT_INSERT = "insert into concept (label,variable_id,concept_id) values (?,?,?)";

    private static final String STUDY_SEARCH_DOCUMENT_STORE_INSERT = "insert into study_search_document_store (study_id, series_title, universe_label, title, purpose, reference_area, time_coverage, study_search_document) values (?, ?, ?, ?, ?, ?, ?, to_tsvector(?::text))";
    private static final String VARIABLE_SEARCH_DOCUMENT_STORE_INSERT = "insert into variable_search_document_store (variable_id, study_id, logical_record_id, name, label, representation_type, variable_search_document) values (?, ?, ?, ?, ?, ?, to_tsvector(?::text))";

    private static final String CATEGORY_TRUNCATE = "truncate table category cascade";
    private static final String CODE_LIST_TRUNCATE = "truncate table code_list cascade";
    private static final String METADATA_DOMAIN_TRUNCATE = "truncate table metadata_domain cascade";
    private static final String SUBDOMAIN_TRUNCATE = "truncate table subdomain cascade";
    private static final String SERIES_TRUNCATE = "truncate table series cascade";
    private static final String STUDY_TRUNCATE = "truncate table study cascade";
    private static final String OTHER_MATERIAL_TRUNCATE = "truncate table other_material cascade";
    private static final String DATA_FILE_TRUNCATE = "truncate table data_file cascade";
    private static final String KEYWORD_TRUNCATE = "truncate table keyword cascade";
    private static final String SUBJECT_TRUNCATE = "truncate table subject cascade";
    private static final String LOGICAL_RECORD_TRUNCATE = "truncate table logical_record cascade";
    private static final String QUALITY_INDICATOR_TRUNCATE = "truncate table quality_indicator cascade";
    private static final String VARIABLE_TRUNCATE = "truncate table variable cascade";
    private static final String CONCEPT_TRUNCATE = "truncate table concept cascade";

    private static final String STUDY_SEARCH_DOCUMENT_STORE_TRUNCATE = "truncate table study_search_document_store cascade";
    private static final String VARIABLE_SEARCH_DOCUMENT_STORE_TRUNCATE = "truncate table variable_search_document_store cascade";


    private final JdbcTemplate jdbcTemplate;
    private final DtoMapper mapper;

    @Transactional
    public void saveMetadata(Metadata metadata) {
//        jdbcTemplate.execute(
//                String.join(";",
//                        STUDY_SEARCH_DOCUMENT_STORE_TRUNCATE,
//                        VARIABLE_SEARCH_DOCUMENT_STORE_TRUNCATE,
//                        QUALITY_INDICATOR_TRUNCATE,
//                        CONCEPT_TRUNCATE,
//                        VARIABLE_TRUNCATE,
//                        LOGICAL_RECORD_TRUNCATE,
//                        SUBJECT_TRUNCATE,
//                        KEYWORD_TRUNCATE,
//                        DATA_FILE_TRUNCATE,
//                        OTHER_MATERIAL_TRUNCATE,
//                        STUDY_TRUNCATE,
//                        SERIES_TRUNCATE,
//                        SUBDOMAIN_TRUNCATE,
//                        METADATA_DOMAIN_TRUNCATE,
//                        CODE_LIST_TRUNCATE,
//                        CATEGORY_TRUNCATE
//                )
//        );
//        jdbcTemplate.execute(STUDY_SEARCH_DOCUMENT_STORE_TRUNCATE);
//        jdbcTemplate.execute(VARIABLE_SEARCH_DOCUMENT_STORE_TRUNCATE);
//
        jdbcTemplate.execute(QUALITY_INDICATOR_TRUNCATE);
        jdbcTemplate.execute(CONCEPT_TRUNCATE);
        jdbcTemplate.execute(VARIABLE_TRUNCATE);
        jdbcTemplate.execute(LOGICAL_RECORD_TRUNCATE);
        jdbcTemplate.execute(SUBJECT_TRUNCATE);
        jdbcTemplate.execute(KEYWORD_TRUNCATE);
        jdbcTemplate.execute(DATA_FILE_TRUNCATE);
        jdbcTemplate.execute(OTHER_MATERIAL_TRUNCATE);
        jdbcTemplate.execute(STUDY_TRUNCATE);
        jdbcTemplate.execute(SERIES_TRUNCATE);
        jdbcTemplate.execute(SUBDOMAIN_TRUNCATE);
        jdbcTemplate.execute(METADATA_DOMAIN_TRUNCATE);

        jdbcTemplate.execute(CODE_LIST_TRUNCATE);
        jdbcTemplate.execute(CATEGORY_TRUNCATE);

        List<CodeListModel> codeListsToSave = new ArrayList<>();
        List<CategoryModel> categoriesToSave = new ArrayList<>();

        metadata.codeLists().forEach(metadataCodeList -> {
            codeListsToSave.add(mapper.dtoToCodeListModel(metadataCodeList));
            categoriesToSave.addAll(metadataCodeList.categories().stream()
                    .map(metadataCategory -> {
                        CategoryModel category = mapper.dtoToCategoryModel(metadataCategory);
                        category.setId(UUID.randomUUID());
                        category.setCodeListId(UUID.fromString(metadataCodeList.id()));
                        return category;
                    })
                    .toList());
        });

        batchInsertCodeList(codeListsToSave);
        batchInsertCategory(categoriesToSave);

        List<MetadataDomainModel> domainsToSave = new ArrayList<>();
        List<SubdomainModel> subdomainsToSave = new ArrayList<>();
        List<SeriesModel> seriesToSave = new ArrayList<>();
        List<StudyModel> studiesToSave = new ArrayList<>();
        List<OtherMaterialModel> otherMaterialsToSave = new ArrayList<>();
        List<DataFileModel> dataFilesToSave = new ArrayList<>();
        List<KeywordModel> keywordsToSave = new ArrayList<>();
        List<SubjectModel> subjectsToSave = new ArrayList<>();
        List<LogicalRecordModel> logicalRecordsToSave = new ArrayList<>();
        List<VariableModel> variablesToSave = new ArrayList<>();
        List<ConceptModel> conceptsToSave = new ArrayList<>();
        List<QualityIndicatorModel> qualityIndicatorsToSave = new ArrayList<>();

        Map<UUID, List<DataFileModel>> studyIdDataFileMap = new HashMap<>();
        Map<UUID, List<KeywordModel>> studyIdKeywordMap = new HashMap<>();
        Map<UUID, List<SubjectModel>> studyIdSubjectMap = new HashMap<>();
        Map<UUID, List<LogicalRecordModel>> studyIdLogicaRecodlMap = new HashMap<>();
        Map<UUID, List<VariableModel>> studyIdVariableMap = new HashMap<>();
        Map<UUID, List<ConceptModel>> studyIdConceptMap = new HashMap<>();

        Map<UUID, List<ConceptModel>> variableIdConceptMap = new HashMap<>();

        metadata.domains().forEach(domainDto -> {
            domainsToSave.add(mapper.dtoToMetadataDomainModel(domainDto));
            domainDto.subdomains().forEach(subdomainDto -> {
                SubdomainModel subdomain = mapper.dtoToSubdomainModel(subdomainDto);
                subdomain.setMetadataDomainId(UUID.fromString(domainDto.id()));
                subdomainsToSave.add(subdomain);
                subdomainDto.series().forEach(seriesDto -> {
                    SeriesModel series = mapper.dtoToSeriesModel(seriesDto);
                    series.setSubdomainId(UUID.fromString(subdomainDto.id()));
                    seriesToSave.add(series);
                    seriesDto.studies().forEach(studyDto -> {
                        StudyModel study = mapper.dtoToStudyModel(studyDto);
                        study.setMetadataDomainId(UUID.fromString(domainDto.id()));
                        study.setSubdomainId(UUID.fromString(subdomainDto.id()));
                        study.setSeriesId(UUID.fromString(seriesDto.id()));
                        studiesToSave.add(study);
                        studyIdDataFileMap.put(study.getId(), new ArrayList<>());
                        studyIdKeywordMap.put(study.getId(), new ArrayList<>());
                        studyIdSubjectMap.put(study.getId(), new ArrayList<>());
                        studyIdLogicaRecodlMap.put(study.getId(), new ArrayList<>());
                        studyIdVariableMap.put(study.getId(), new ArrayList<>());
                        studyIdConceptMap.put(study.getId(), new ArrayList<>());
                        studyDto.otherMaterials().forEach(otherMaterialDto -> {
                            OtherMaterialModel otherMaterial = mapper.dtoToOtherMaterialModel(otherMaterialDto);
                            otherMaterial.setStudyId(UUID.fromString(studyDto.id()));
                            otherMaterial.setId(UUID.randomUUID());
                            otherMaterialsToSave.add(otherMaterial);
                        });
                        studyDto.dataFiles().forEach(dataFileDto -> {
                            DataFileModel dataFile = mapper.dtoToDataFileModel(dataFileDto, dataFileDto.temporalCoverage());
                            dataFile.setStudyId(UUID.fromString(studyDto.id()));
                            dataFilesToSave.add(dataFile);
                            studyIdDataFileMap.get(study.getId()).add(dataFile);
                            dataFileDto.keywords().forEach(keywordDto -> {
                                KeywordModel keyword = mapper.dtoToKeywordModel(keywordDto);
                                keyword.setDataFileId(UUID.fromString(dataFileDto.id()));
                                keyword.setId(UUID.randomUUID());
                                keywordsToSave.add(keyword);
                                studyIdKeywordMap.get(study.getId()).add(keyword);
                            });
                            dataFileDto.subjects().forEach(subjectDto -> {
                                SubjectModel subject = mapper.dtoToSubjectModel(subjectDto);
                                subject.setDataFileId(UUID.fromString(dataFileDto.id()));
                                subject.setId(UUID.randomUUID());
                                subjectsToSave.add(subject);
                                studyIdSubjectMap.get(study.getId()).add(subject);
                            });
                            dataFileDto.logicalRecords().forEach(logicalRecordDto -> {
                                LogicalRecordModel logicalRecord = mapper.dtoToLogicalRecordModel(logicalRecordDto);
                                logicalRecord.setDataFileId(UUID.fromString(dataFileDto.id()));
                                logicalRecordsToSave.add(logicalRecord);
                                studyIdLogicaRecodlMap.get(study.getId()).add(logicalRecord);
                                logicalRecordDto.qualityIndicators().forEach(qualityIndicatorsDto -> {
                                    QualityIndicatorModel qualityIndicator = mapper.dtoToQualityIndicatorModel(qualityIndicatorsDto);
                                    qualityIndicator.setLogicalRecordId(UUID.fromString(logicalRecordDto.id()));
                                    qualityIndicator.setId(UUID.randomUUID());
                                    qualityIndicatorsToSave.add(qualityIndicator);
                                });
                                logicalRecordDto.variables().forEach(variableDto -> {
                                    VariableModel variable = mapper.dtoToVariableModel(variableDto);
                                    variable.setLogicalRecordId(UUID.fromString(logicalRecordDto.id()));
                                    variable.setStudyId(study.getId());
                                    variablesToSave.add(variable);
                                    studyIdVariableMap.get(study.getId()).add(variable);
                                    variableIdConceptMap.put(variable.getId(), new ArrayList<>());
                                    variableDto.concepts().forEach(conceptDto -> {
                                        ConceptModel concept = mapper.dtoToConceptModel(conceptDto);
                                        concept.setVariableId(UUID.fromString(variableDto.id()));
                                        concept.setId(UUID.randomUUID());
                                        conceptsToSave.add(concept);
                                        studyIdConceptMap.get(study.getId()).add(concept);
                                        variableIdConceptMap.get(variable.getId()).add(concept);
                                    });
                                    variableDto.qualityIndicators().forEach(qualityIndicatorDto -> {
                                        QualityIndicatorModel qualityIndicator = mapper.dtoToQualityIndicatorModel(qualityIndicatorDto);
                                        qualityIndicator.setVariableId(UUID.fromString(variableDto.id()));
                                        qualityIndicator.setId(UUID.randomUUID());
                                        qualityIndicatorsToSave.add(qualityIndicator);
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });

        batchInsertMetadataDomain(domainsToSave);
        batchInsertSubdomain(subdomainsToSave);
        batchInsertSeries(seriesToSave);
        batchInsertStudy(studiesToSave);
        batchInsertOtherMaterial(otherMaterialsToSave);
        batchInsertDataFile(dataFilesToSave);
        batchInsertKeyword(keywordsToSave);
        batchInsertSubject(subjectsToSave);
        batchInsertLogicalRecord(logicalRecordsToSave);
        batchInsertVariable(variablesToSave);
        batchInsertConcept(conceptsToSave);
        batchInsertQualityIndicator(qualityIndicatorsToSave);

        List<StudySearchDocumentStore> studySearchDocumentStores =
                studiesToSave.stream()
                        .map(studyModel -> {
                            StudySearchDocumentStore studySearchDocumentStore = new StudySearchDocumentStore();
                            studySearchDocumentStore.setStudyId(studyModel.getId());
                            studySearchDocumentStore.setSeriesTitle(studyModel.getSeriesTitle());
                            studySearchDocumentStore.setUniverseLabel(studyModel.getUniverseLabel());
                            studySearchDocumentStore.setTitle(studyModel.getTitle());
                            studySearchDocumentStore.setPurpose(studyModel.getPurpose());
                            studySearchDocumentStore.setReferenceArea(studyModel.getReferenceArea());
                            studySearchDocumentStore.setTimeCoverage(studyModel.getTimeCoverage());

                            String studyPart = String.join(" ", List.of(
                                    studyModel.getSeriesTitle(),
                                    studyModel.getSeriesTitle(),
                                    studyModel.getUniverseLabel(),
                                    studyModel.getTitle(),
                                    studyModel.getSummary(),
                                    studyModel.getPurpose(),
                                    studyModel.getStudyCode(),
                                    studyModel.getSectorCoverage(),
                                    studyModel.getOtherDissemination(),
                                    studyModel.getDocumentationOnMethodology(),
                                    studyModel.getGeographicalComparability(),
                                    studyModel.getComparabilityOverTime(),
                                    studyModel.getSourceData(),
                                    studyModel.getFrequencyOfDataCollection(),
                                    studyModel.getDataCollection(),
                                    studyModel.getDataValidation(),
                                    studyModel.getDataCompilation()));

                            String dataFilePart = studyIdDataFileMap.get(studyModel.getId()).stream()
                                    .map(DataFileModel::getTitle).collect(Collectors.joining(" "));

                            String keywordPart = studyIdKeywordMap.get(studyModel.getId()).stream()
                                    .map(KeywordModel::getName).collect(Collectors.joining(" "));

                            String subjectPart = studyIdSubjectMap.get(studyModel.getId()).stream()
                                    .map(SubjectModel::getName).collect(Collectors.joining(" "));

                            String logicalRecordPart = String.join(" ", studyIdLogicaRecodlMap.get(studyModel.getId()).stream()
                                    .map(lr -> String.join(" ", lr.getName(), lr.getLabel(), lr.getDescription())).toList());

                            String variablePart = String.join(" ", studyIdVariableMap.get(studyModel.getId()).stream()
                                    .map(v -> String.join(" ", v.getName(), v.getLabel(), v.getDescription(), v.getRepresentedVariableLabel(), v.getConceptualVariableLabel())).toList());

                            String conceptPart = studyIdConceptMap.get(studyModel.getId()).stream()
                                    .map(ConceptModel::getLabel).collect(Collectors.joining(" "));


                            studySearchDocumentStore.setStudySearchDocument(
                                    new StringBuilder()
                                            .append(studyPart)
                                            .append(dataFilePart)
                                            .append(keywordPart)
                                            .append(subjectPart)
                                            .append(logicalRecordPart)
                                            .append(variablePart)
                                            .append(conceptPart).toString());

                            return studySearchDocumentStore;
                        }).toList();

        List<VariableSearchDocumentStore> variableSearchDocumentStores = variablesToSave.stream()
                        .map(variableModel -> {
                            VariableSearchDocumentStore variableSearchDocumentStore = new VariableSearchDocumentStore();
                            variableSearchDocumentStore.setVariableId(variableModel.getId());
                            variableSearchDocumentStore.setStudyId(variableModel.getStudyId());
                            variableSearchDocumentStore.setLogicalRecordId(variableModel.getLogicalRecordId());
                            variableSearchDocumentStore.setName(variableModel.getName());
                            variableSearchDocumentStore.setLabel(variableModel.getLabel());
                            variableSearchDocumentStore.setRepresentationType(variableModel.getRepresentationType());

                            String variablePart = String.join(" ", List.of(
                                    variableModel.getName(),
                                    variableModel.getLabel(),
                                    variableModel.getDescription(),
                                    variableModel.getRepresentedVariableLabel(),
                                    variableModel.getConceptualVariableLabel()));

                            variableSearchDocumentStore.setVariableSearchDocument(
                                    new StringBuilder()
                                            .append(variablePart)
                                            .toString()
                            );
                            return variableSearchDocumentStore;
                        }).toList();

        batchInsertStudySearchDocumentStore(studySearchDocumentStores);
        batchInsertVariableSearchDocumentStore(variableSearchDocumentStores);
    }

    private void batchInsertStudySearchDocumentStore(List<StudySearchDocumentStore> models) {
        jdbcTemplate.batchUpdate(STUDY_SEARCH_DOCUMENT_STORE_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                StudySearchDocumentStore model = models.get(i);
                ps.setObject(1, model.getStudyId());
                ps.setString(2, model.getSeriesTitle());
                ps.setString(3, model.getUniverseLabel());
                ps.setString(4, model.getTitle());
                ps.setString(5, model.getPurpose());
                ps.setString(6, model.getReferenceArea());
                ps.setString(7, model.getTimeCoverage());
                ps.setString(8, model.getStudySearchDocument());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertVariableSearchDocumentStore(List<VariableSearchDocumentStore> models) {
        jdbcTemplate.batchUpdate(VARIABLE_SEARCH_DOCUMENT_STORE_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                VariableSearchDocumentStore model = models.get(i);
                ps.setObject(1, model.getVariableId());
                ps.setObject(2, model.getStudyId());
                ps.setObject(3, model.getLogicalRecordId());
                ps.setString(4, model.getName());
                ps.setString(5, model.getLabel());
                ps.setString(6, model.getRepresentationType());
                ps.setString(7, model.getVariableSearchDocument());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertCategory(List<CategoryModel> models) {
        jdbcTemplate.batchUpdate(CATEGORY_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CategoryModel model = models.get(i);
                ps.setObject(1, model.getCodeListId());
                ps.setString(2, model.getCodeValue());
                ps.setString(3, model.getDescription());
                ps.setString(4, model.getLabel());
                ps.setObject(5, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertCodeList(List<CodeListModel> models) {
        jdbcTemplate.batchUpdate(CODE_LIST_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CodeListModel model = models.get(i);
                ps.setString(1, model.getDescription());
                ps.setString(2, model.getLabel());
                ps.setObject(3, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertMetadataDomain(List<MetadataDomainModel> models) {
        jdbcTemplate.batchUpdate(METADATA_DOMAIN_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MetadataDomainModel model = models.get(i);
                ps.setString(1, model.getLabel());
                ps.setObject(2, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertSubdomain(List<SubdomainModel> models) {
        jdbcTemplate.batchUpdate(SUBDOMAIN_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SubdomainModel model = models.get(i);
                ps.setString(1, model.getLabel());
                ps.setObject(2, model.getMetadataDomainId());
                ps.setObject(3, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertSeries(List<SeriesModel> models) {
        jdbcTemplate.batchUpdate(SERIES_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SeriesModel model = models.get(i);
                ps.setObject(1, model.getSubdomainId());
                ps.setString(2, model.getTitle());
                ps.setObject(3, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertStudy(List<StudyModel> models) {
        jdbcTemplate.batchUpdate(STUDY_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                StudyModel model = models.get(i);
                ps.setString(1, model.getComparabilityOverTime());
                ps.setString(2, model.getContactEmailAddress());
                ps.setString(3, model.getContactName());
                ps.setString(4, model.getDataCollection());
                ps.setString(5, model.getDataCompilation());
                ps.setString(6, model.getDataValidation());
                ps.setString(7, model.getDocumentationOnMethodology());
                ps.setString(8, model.getFrequencyOfDataCollection());
                ps.setString(9, model.getGeographicalComparability());
                ps.setBoolean(10, model.getIsAdminData());
                ps.setObject(11, model.getMetadataDomainId());
                ps.setString(12, model.getOtherDissemination());
                ps.setString(13, model.getPurpose());
                ps.setString(14, model.getReferenceArea());
                ps.setString(15, model.getSectorCoverage());
                ps.setObject(16, model.getSeriesId());
                ps.setString(17, model.getSeriesTitle());
                ps.setString(18, model.getSourceData());
                ps.setString(19, model.getStudyCode());
                ps.setObject(20, model.getSubdomainId());
                ps.setString(21, model.getSummary());
                ps.setString(22, model.getTimeCoverage());
                ps.setString(23, model.getTitle());
                ps.setObject(24, model.getUniverseId());
                ps.setString(25, model.getUniverseLabel());
                ps.setObject(26, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }


    private void batchInsertOtherMaterial(List<OtherMaterialModel> models) {
        jdbcTemplate.batchUpdate(OTHER_MATERIAL_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OtherMaterialModel model = models.get(i);
                ps.setObject(1, model.getStudyId());
                ps.setString(2, model.getTitle());
                ps.setObject(3, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertDataFile(List<DataFileModel> models) {
        jdbcTemplate.batchUpdate(DATA_FILE_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DataFileModel model = models.get(i);
                ps.setDate(1, toSqlDate(model.getBeginDate()));
                ps.setDate(2, toSqlDate(model.getEndDate()));
                ps.setObject(3, model.getStudyId());
                ps.setString(4, model.getTitle());
                ps.setObject(5, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertKeyword(List<KeywordModel> models) {
        jdbcTemplate.batchUpdate(KEYWORD_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                KeywordModel model = models.get(i);
                ps.setObject(1, model.getDataFileId());
                ps.setString(2, model.getName());
                ps.setObject(3, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertSubject(List<SubjectModel> models) {
        jdbcTemplate.batchUpdate(SUBJECT_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SubjectModel model = models.get(i);
                ps.setObject(1, model.getDataFileId());
                ps.setString(2, model.getName());
                ps.setObject(3, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertLogicalRecord(List<LogicalRecordModel> models) {
        jdbcTemplate.batchUpdate(LOGICAL_RECORD_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                LogicalRecordModel model = models.get(i);
                ps.setObject(1, model.getDataFileId());
                ps.setString(2, model.getDatabaseUrl());
                ps.setString(3, model.getDescription());
                ps.setString(4, model.getLabel());
                ps.setString(5, model.getName());
                ps.setLong(6, model.getNumberOfEntries());
                ps.setObject(7, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertQualityIndicator(List<QualityIndicatorModel> models) {
        jdbcTemplate.batchUpdate(QUALITY_INDICATOR_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                QualityIndicatorModel model = models.get(i);
                ps.setObject(1, model.getLogicalRecordId());
                ps.setObject(2, model.getVariableId());
                ps.setString(3, model.getName());
                ps.setString(4, model.getLabel());
                ps.setObject(5, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private void batchInsertVariable(List<VariableModel> models) {
        jdbcTemplate.batchUpdate(VARIABLE_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                VariableModel model = models.get(i);
                ps.setBoolean(1, model.getBlankvaluesRepresentMissingvalues());
                ps.setObject(2, model.getCodeListId());
                ps.setString(3, model.getConceptualVariableLabel());
                ps.setString(4, model.getDescription());
                ps.setString(5, model.getLabel());
                ps.setObject(6, model.getLogicalRecordId());
                ps.setString(7, model.getMeasurementUnit());
                ps.setString(8, model.getMissingvalues());
                ps.setString(9, model.getName());
                ps.setInt(10, model.getPercentageOfFilledEntries());
                ps.setString(11, model.getRepresentationType());
                ps.setString(12, model.getRepresentedVariableLabel());
                ps.setString(13, model.getType());
                ps.setString(14, model.getUnitTypeId());
                ps.setString(15, model.getUnitTypeLabel());
                ps.setBoolean(16, model.getVariableIsAWeight());
                ps.setString(17, model.getVariableRole());
                ps.setObject(18, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }
    private void batchInsertConcept(List<ConceptModel> models) {
        jdbcTemplate.batchUpdate(CONCEPT_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ConceptModel model = models.get(i);
                ps.setString(1, model.getLabel());
                ps.setObject(2, model.getVariableId());
                ps.setObject(3, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }

    private static java.sql.Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : java.sql.Date.valueOf(localDate);
    }
}

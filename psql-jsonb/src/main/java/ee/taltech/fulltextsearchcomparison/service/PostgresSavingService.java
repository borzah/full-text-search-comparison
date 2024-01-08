package ee.taltech.fulltextsearchcomparison.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.fulltextsearchcomparison.metadata.*;
import ee.taltech.fulltextsearchcomparison.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostgresSavingService {

    private static final String CATEGORY_INSERT = "insert into category (code_list_id,category_data,category_id) values (?,?::jsonb,?)";
    private static final String CODE_LIST_INSERT = "insert into code_list (code_list_data,code_list_id) values (?::jsonb,?)";
    private static final String STUDY_INSERT = "insert into study (study_data,study_id) values (?::jsonb,?)";
    private static final String OTHER_MATERIAL_INSERT = "insert into other_material (study_id,other_material_data,other_material_id) values (?,?::jsonb,?)";
    private static final String DATA_FILE_INSERT = "insert into data_file (study_id,data_file_data,data_file_id) values (?,?::jsonb,?)";
    private static final String KEYWORD_INSERT = "insert into keyword (data_file_id,keyword_data,keyword_id) values (?,?::jsonb,?)";
    private static final String SUBJECT_INSERT = "insert into subject (data_file_id,subject_data,subject_id) values (?,?::jsonb,?)";
    private static final String LOGICAL_RECORD_INSERT = "insert into logical_record (data_file_id,logical_record_data,logical_record_id) values (?,?::jsonb,?)";
    private static final String VARIABLE_INSERT = "insert into variable (logical_record_id,code_list_id,variable_data,variable_id) values (?,?,?::jsonb,?)";
    private static final String QUALITY_INDICATOR_INSERT = "insert into quality_indicator (variable_id,quality_indicator_data,quality_indicator_id) values (?,?::jsonb,?)";
    private static final String CONCEPT_INSERT = "insert into concept (variable_id,concept_data,concept_id) values (?,?::jsonb,?)";

    private static final String CATEGORY_TRUNCATE = "truncate table category cascade";
    private static final String CODE_LIST_TRUNCATE = "truncate table code_list cascade";
    private static final String STUDY_TRUNCATE = "truncate table study cascade";
    private static final String OTHER_MATERIAL_TRUNCATE = "truncate table other_material cascade";
    private static final String DATA_FILE_TRUNCATE = "truncate table data_file cascade";
    private static final String KEYWORD_TRUNCATE = "truncate table keyword cascade";
    private static final String SUBJECT_TRUNCATE = "truncate table subject cascade";
    private static final String LOGICAL_RECORD_TRUNCATE = "truncate table logical_record cascade";
    private static final String QUALITY_INDICATOR_TRUNCATE = "truncate table quality_indicator cascade";
    private static final String VARIABLE_TRUNCATE = "truncate table variable cascade";
    private static final String CONCEPT_TRUNCATE = "truncate table concept cascade";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveMetadataJson(Metadata metadata) {
        jdbcTemplate.execute("TRUNCATE table domain_json");
        jdbcTemplate.update("INSERT INTO domain_json (data)  VALUES (?::jsonb)", ps -> {
            try {
                ps.setObject(1, objectMapper.writeValueAsString(metadata));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Transactional
    public void saveMetadataJsonSeparateTables(Metadata metadata) {

        jdbcTemplate.execute(QUALITY_INDICATOR_TRUNCATE);
        jdbcTemplate.execute(CONCEPT_TRUNCATE);
        jdbcTemplate.execute(VARIABLE_TRUNCATE);
        jdbcTemplate.execute(LOGICAL_RECORD_TRUNCATE);
        jdbcTemplate.execute(SUBJECT_TRUNCATE);
        jdbcTemplate.execute(KEYWORD_TRUNCATE);
        jdbcTemplate.execute(DATA_FILE_TRUNCATE);
        jdbcTemplate.execute(OTHER_MATERIAL_TRUNCATE);
        jdbcTemplate.execute(STUDY_TRUNCATE);

        jdbcTemplate.execute(CODE_LIST_TRUNCATE);
        jdbcTemplate.execute(CATEGORY_TRUNCATE);

        List<CodeListModel> codeListsToSave = new ArrayList<>();
        List<CategoryModel> categoriesToSave = new ArrayList<>();

        metadata.codeLists().forEach(metadataCodeList -> {
            UUID codeListId = UUID.fromString(metadataCodeList.id());
            try {

                MetadataCodeList codeListToJson = new MetadataCodeList(metadataCodeList);
                codeListsToSave.add(new CodeListModel(codeListId, objectMapper.writeValueAsString(codeListToJson)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            categoriesToSave.addAll(metadataCodeList.categories().stream()
                    .map(metadataCategory -> {
                        CategoryModel category = new CategoryModel();
                        category.setId(UUID.randomUUID());
                        category.setCodeListId(codeListId);
                        try {
                            MetadataCategory metadataCategoryToJson = new MetadataCategory(metadataCategory);
                            category.setData(objectMapper.writeValueAsString(metadataCategoryToJson));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        return category;
                    })
                    .toList());
        });

        List<StudyModel> studiesToSave = new ArrayList<>();
        List<OtherMaterialModel> otherMaterialsToSave = new ArrayList<>();
        List<DataFileModel> dataFilesToSave = new ArrayList<>();
        List<KeywordModel> keywordsToSave = new ArrayList<>();
        List<SubjectModel> subjectsToSave = new ArrayList<>();
        List<LogicalRecordModel> logicalRecordsToSave = new ArrayList<>();
        List<VariableModel> variablesToSave = new ArrayList<>();
        List<ConceptModel> conceptsToSave = new ArrayList<>();
        List<QualityIndicatorModel> qualityIndicatorsToSave = new ArrayList<>();

        metadata.domains().forEach(domainDto -> {
            domainDto.subdomains().forEach(subdomainDto -> {
                subdomainDto.series().forEach(seriesDto -> {
                    seriesDto.studies().forEach(studyDto -> {
                        StudyModel study = new StudyModel();
                        study.setId(UUID.fromString(studyDto.id()));
                        try {
                            MetadataStudy studyToJson = new MetadataStudy(studyDto);
                            study.setData(objectMapper.writeValueAsString(studyToJson));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        studiesToSave.add(study);
                        studyDto.otherMaterials().forEach(otherMaterialDto -> {
                            OtherMaterialModel otherMaterial = new OtherMaterialModel();
                            otherMaterial.setStudyId(UUID.fromString(studyDto.id()));
                            otherMaterial.setId(UUID.randomUUID());
                            try {
                                MetadataOtherMaterial otherMaterialToJson = new MetadataOtherMaterial(otherMaterialDto);
                                otherMaterial.setData(objectMapper.writeValueAsString(otherMaterialToJson));
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            otherMaterialsToSave.add(otherMaterial);
                        });
                        studyDto.dataFiles().forEach(dataFileDto -> {
                            DataFileModel dataFile = new DataFileModel();
                            dataFile.setId(UUID.fromString(dataFileDto.id()));
                            dataFile.setStudyId(UUID.fromString(studyDto.id()));
                            try {
                                MetadataDataFile dataFileToJson = new MetadataDataFile(dataFileDto);
                                dataFile.setData(objectMapper.writeValueAsString(dataFileToJson));
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            dataFilesToSave.add(dataFile);
                            dataFileDto.keywords().forEach(keywordDto -> {
                                KeywordModel keyword = new KeywordModel();
                                keyword.setDataFileId(UUID.fromString(dataFileDto.id()));
                                keyword.setId(UUID.randomUUID());
                                try {
                                    keyword.setData(objectMapper.writeValueAsString(keywordDto));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                keywordsToSave.add(keyword);
                            });
                            dataFileDto.subjects().forEach(subjectDto -> {
                                SubjectModel subject = new SubjectModel();
                                subject.setDataFileId(UUID.fromString(dataFileDto.id()));
                                subject.setId(UUID.randomUUID());
                                try {
                                    subject.setData(objectMapper.writeValueAsString(subjectDto));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                subjectsToSave.add(subject);
                            });
                            dataFileDto.logicalRecords().forEach(logicalRecordDto -> {
                                LogicalRecordModel logicalRecord = new LogicalRecordModel();
                                logicalRecord.setDataFileId(UUID.fromString(dataFileDto.id()));
                                logicalRecord.setId(UUID.fromString(logicalRecordDto.id()));
                                try {
                                    MetadataLogicalRecord logicalRecordToJson = new MetadataLogicalRecord(logicalRecordDto);
                                    logicalRecord.setData(objectMapper.writeValueAsString(logicalRecordToJson));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                logicalRecordsToSave.add(logicalRecord);
                                logicalRecordDto.qualityIndicators().forEach(qualityIndicatorsDto -> {
                                    QualityIndicatorModel qualityIndicator = new QualityIndicatorModel();
                                    qualityIndicator.setId(UUID.randomUUID());
                                    try {
                                        qualityIndicator.setData(objectMapper.writeValueAsString(qualityIndicatorsDto));
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                                    qualityIndicatorsToSave.add(qualityIndicator);
                                });
                                logicalRecordDto.variables().forEach(variableDto -> {
                                    VariableModel variable = new VariableModel();
                                    variable.setId(UUID.fromString(variableDto.id()));
                                    variable.setLogicalRecordId(UUID.fromString(logicalRecordDto.id()));
                                    try {
                                        MetadataVariable variableToJson = new MetadataVariable(variableDto);
                                        variable.setData(objectMapper.writeValueAsString(variableToJson));
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                                    variablesToSave.add(variable);
                                    variableDto.concepts().forEach(conceptDto -> {
                                        ConceptModel concept = new ConceptModel();
                                        concept.setVariableId(UUID.fromString(variableDto.id()));
                                        concept.setId(UUID.randomUUID());
                                        try {
                                            concept.setData(objectMapper.writeValueAsString(conceptDto));
                                        } catch (JsonProcessingException e) {
                                            throw new RuntimeException(e);
                                        }
                                        conceptsToSave.add(concept);
                                    });
                                    variableDto.qualityIndicators().forEach(qualityIndicatorDto -> {
                                        QualityIndicatorModel qualityIndicator = new QualityIndicatorModel();
                                        qualityIndicator.setVariableId(UUID.fromString(variableDto.id()));
                                        qualityIndicator.setId(UUID.randomUUID());
                                        try {
                                            qualityIndicator.setData(objectMapper.writeValueAsString(qualityIndicatorDto));
                                        } catch (JsonProcessingException e) {
                                            throw new RuntimeException(e);
                                        }
                                        qualityIndicatorsToSave.add(qualityIndicator);
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });

        batchInsertCodeList(codeListsToSave);
        batchInsertCategory(categoriesToSave);
        batchInsertStudy(studiesToSave);
        batchInsertOtherMaterial(otherMaterialsToSave);
        batchInsertDataFile(dataFilesToSave);
        batchInsertKeyword(keywordsToSave);
        batchInsertSubject(subjectsToSave);
        batchInsertLogicalRecord(logicalRecordsToSave);
        batchInsertVariable(variablesToSave);
        batchInsertConcept(conceptsToSave);
        batchInsertQualityIndicator(qualityIndicatorsToSave);
    }

    private void batchInsertCategory(List<CategoryModel> models) {
        jdbcTemplate.batchUpdate(CATEGORY_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CategoryModel model = models.get(i);
                ps.setObject(1, model.getCodeListId());
                ps.setString(2, model.getData());
                ps.setObject(3, model.getId());
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
                ps.setString(1, model.getData());
                ps.setObject(2, model.getId());
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
                ps.setString(1, model.getData());
                ps.setObject(2, model.getId());
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
                ps.setString(2, model.getData());
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
                ps.setObject(1, model.getStudyId());
                ps.setString(2, model.getData());
                ps.setObject(3, model.getId());
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
                ps.setString(2, model.getData());
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
                ps.setString(2, model.getData());
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
                ps.setString(2, model.getData());
                ps.setObject(3, model.getId());
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
                ps.setObject(1, model.getLogicalRecordId());
                ps.setObject(2, model.getCodeListId());
                ps.setString(3, model.getData());
                ps.setObject(4, model.getId());
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
                ps.setObject(1, model.getVariableId());
                ps.setString(2, model.getData());
                ps.setObject(3, model.getId());
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
                ps.setObject(1, model.getVariableId());
                ps.setString(2, model.getData());
                ps.setObject(3, model.getId());
            }

            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }
}

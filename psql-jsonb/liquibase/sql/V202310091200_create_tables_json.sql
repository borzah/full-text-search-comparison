CREATE TABLE domain_json (
    id BIGSERIAL PRIMARY KEY,
    data JSONB
);

CREATE EXTENSION pg_trgm;

-- CREATE INDEX metadata_json_gin_studies_series_title_idx ON domain_json USING  GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'seriesTitle') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_universe_label_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'universeLabel') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_title_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'title') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_summary_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'summary') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_purpose_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'purpose') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_study_code_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'studyCode') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_sector_coverage_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'sectorCoverage') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_other_dissemination_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'otherDissemination') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_documentation_on_methodology_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'documentationOnMethodology') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_geographical_comparability_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'geographicalComparability') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_comparability_over_time_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'comparabilityOverTime') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_source_data_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'sourceData') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_frequency_of_data_collection_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'frequencyOfDataCollection') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_data_collection_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'dataCollection') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_data_validation_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'dataValidation') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_studies_data_compilation_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'dataCompilation') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_data_files_title_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' ->> 'title') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_keyword_name_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'keywords'->> 'name') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_subject_name_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'subjects'->> 'name') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_logical_record_name_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->> 'name') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_logical_record_label_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->> 'label') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_logical_record_description_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->> 'description') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_variable_name_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'name') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_variable_label_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'label') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_variable_description_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'description') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_variable_represented_variable_label_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'representedVariableLabel') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_variable_conceptual_variable_label ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'conceptualVariableLabel') gin_trgm_ops);
-- CREATE INDEX metadata_json_gin_concept_label ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' -> 'concepts' ->> 'label') gin_trgm_ops);

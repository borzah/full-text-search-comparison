CREATE TABLE code_list
(
    code_list_id UUID PRIMARY KEY,
    code_list_data JSONB NOT NULL
);

CREATE TABLE category
(
    category_id UUID PRIMARY KEY,
    code_list_id UUID NOT NULL,
    category_data JSONB NOT NULL,
    CONSTRAINT category_code_list_fk FOREIGN KEY (code_list_id) REFERENCES code_list (code_list_id)
);

CREATE INDEX idx_category_code_list_id ON category (code_list_id);

CREATE TABLE study
(
    study_id UUID PRIMARY KEY,
    study_data JSONB NOT NULL
);

CREATE TABLE other_material
(
    other_material_id UUID PRIMARY KEY,
    study_id UUID NOT NULL,
    other_material_data JSONB NOT NULL,
    CONSTRAINT other_material_study_fk FOREIGN KEY (study_id) REFERENCES study (study_id)
);

CREATE INDEX idx_other_material_study_id ON other_material (study_id);

CREATE TABLE data_file
(
    data_file_id UUID PRIMARY KEY,
    study_id UUID NOT NULL,
    data_file_data JSONB NOT NULL,
    CONSTRAINT data_file_study_fk FOREIGN KEY (study_id) REFERENCES study (study_id)
);

CREATE INDEX idx_data_file_study_id ON data_file (study_id);

CREATE TABLE subject
(
    subject_id UUID PRIMARY KEY,
    data_file_id UUID NOT NULL,
    subject_data JSONB NOT NULL,
    CONSTRAINT subject_data_file_fk FOREIGN KEY (data_file_id) REFERENCES data_file (data_file_id)
);

CREATE INDEX idx_subject_data_file_id ON subject (data_file_id);

CREATE TABLE keyword
(
    keyword_id UUID PRIMARY KEY,
    data_file_id UUID NOT NULL,
    keyword_data JSONB NOT NULL,
    CONSTRAINT keyword_data_file_fk FOREIGN KEY (data_file_id) REFERENCES data_file (data_file_id)
);

CREATE INDEX idx_keyword_data_file_id ON keyword (data_file_id);

CREATE TABLE logical_record
(
    logical_record_id UUID PRIMARY KEY,
    data_file_id UUID NOT NULL,
    logical_record_data JSONB NOT NULL,
    CONSTRAINT logical_record_data_file_fk FOREIGN KEY (data_file_id) REFERENCES data_file (data_file_id)
);

CREATE INDEX idx_logical_record_data_file_id ON logical_record (data_file_id);

CREATE TABLE variable
(
    variable_id UUID PRIMARY KEY,
    logical_record_id UUID NOT NULL,
    code_list_id UUID,
    variable_data JSONB NOT NULL,
    CONSTRAINT variable_logical_record_fk FOREIGN KEY (logical_record_id) REFERENCES logical_record (logical_record_id),
    CONSTRAINT variable_code_list_fk FOREIGN KEY (code_list_id) REFERENCES code_list (code_list_id)
);

CREATE INDEX idx_variable_logical_record_id ON variable (logical_record_id);
CREATE INDEX idx_variable_code_list_id ON variable (code_list_id);

CREATE TABLE concept
(
    concept_id UUID PRIMARY KEY,
    variable_id UUID NOT NULL,
    concept_data JSONB NOT NULL,
    CONSTRAINT concept_variable_fk FOREIGN KEY (variable_id) REFERENCES variable (variable_id)
);

CREATE INDEX idx_concept_variable_id ON concept (variable_id);

CREATE TABLE quality_indicator
(
    quality_indicator_id UUID PRIMARY KEY,
    variable_id UUID,
    quality_indicator_data JSONB NOT NULL,
    CONSTRAINT quality_indicator_variable_fk FOREIGN KEY (variable_id) REFERENCES variable (variable_id)
);

CREATE INDEX idx_quality_indicator_variable_id ON quality_indicator (variable_id);

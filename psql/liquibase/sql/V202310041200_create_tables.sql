CREATE TABLE code_list
(
    code_list_id UUID PRIMARY KEY,
    label TEXT,
    description TEXT
);

CREATE TABLE category
(
    category_id UUID PRIMARY KEY,
    code_list_id UUID NOT NULL,
    label TEXT,
    description TEXT,
    code_value TEXT,
    CONSTRAINT category_code_list_fk FOREIGN KEY (code_list_id) REFERENCES code_list (code_list_id)
);

CREATE TABLE metadata_domain
(
    metadata_domain_id UUID PRIMARY KEY,
    label TEXT
);

CREATE TABLE subdomain
(
    subdomain_id UUID PRIMARY KEY,
    metadata_domain_id UUID NOT NULL,
    label TEXT,
    CONSTRAINT subdomain_metadata_domain_fk FOREIGN KEY (metadata_domain_id) REFERENCES metadata_domain (metadata_domain_id)
);

CREATE TABLE series
(
    series_id UUID PRIMARY KEY,
    subdomain_id UUID NOT NULL,
    title TEXT,
    CONSTRAINT series_subdomain_fk FOREIGN KEY (subdomain_id) REFERENCES subdomain (subdomain_id)
);

CREATE TABLE study
(
    study_id UUID PRIMARY KEY,
    metadata_domain_id UUID NOT NULL,
    subdomain_id UUID NOT NULL,
    series_id UUID NOT NULL,
    series_title TEXT,
    universe_id UUID NOT NULL,
    universe_label TEXT NOT NULL,
    is_admin_data BOOLEAN,
    title TEXT,
    summary TEXT,
    purpose TEXT,
    study_code TEXT,
    contact_name TEXT,
    contact_email_address TEXT,
    sector_coverage TEXT,
    reference_area TEXT,
    time_coverage TEXT,
    other_dissemination TEXT,
    documentation_on_methodology TEXT,
    geographical_comparability TEXT,
    comparability_over_time TEXT,
    source_data TEXT,
    frequency_of_data_collection TEXT,
    data_collection TEXT,
    data_validation TEXT,
    data_compilation TEXT,
    CONSTRAINT study_series_fk FOREIGN KEY (series_id) REFERENCES series (series_id),
    CONSTRAINT subdomain_series_fk FOREIGN KEY (subdomain_id) REFERENCES subdomain (subdomain_id),
    CONSTRAINT metadata_domain_series_fk FOREIGN KEY (metadata_domain_id) REFERENCES metadata_domain (metadata_domain_id)
);

CREATE TABLE other_material
(
    other_material_id UUID PRIMARY KEY,
    study_id UUID NOT NULL,
    title TEXT,
    CONSTRAINT other_material_study_fk FOREIGN KEY (study_id) REFERENCES study (study_id)
);

CREATE TABLE data_file
(
    data_file_id UUID PRIMARY KEY,
    study_id UUID NOT NULL,
    title TEXT,
    begin_date DATE,
    end_date DATE,
    CONSTRAINT data_file_study_fk FOREIGN KEY (study_id) REFERENCES study (study_id)
);

CREATE TABLE subject
(
    subject_id UUID PRIMARY KEY,
    data_file_id UUID NOT NULL,
    name TEXT,
    CONSTRAINT subject_data_file_fk FOREIGN KEY (data_file_id) REFERENCES data_file (data_file_id)
);

CREATE TABLE keyword
(
    keyword_id UUID PRIMARY KEY,
    data_file_id UUID NOT NULL,
    name TEXT,
    CONSTRAINT keyword_data_file_fk FOREIGN KEY (data_file_id) REFERENCES data_file (data_file_id)
);

CREATE TABLE logical_record
(
    logical_record_id UUID PRIMARY KEY,
    data_file_id UUID NOT NULL,
    name TEXT,
    label TEXT,
    description TEXT,
    database_url TEXT,
    number_of_entries BIGINT,
    CONSTRAINT logical_record_data_file_fk FOREIGN KEY (data_file_id) REFERENCES data_file (data_file_id)
);

CREATE TABLE variable
(
    variable_id UUID PRIMARY KEY,
    logical_record_id UUID NOT NULL,
    code_list_id UUID,
    unit_type_id TEXT,
    unit_type_label TEXT,
    name TEXT,
    label TEXT,
    description TEXT,
    representation_type TEXT,
    type TEXT,
    variable_is_a_weight BOOLEAN,
    blank_values_represent_missing_values BOOLEAN,
    missing_values TEXT,
    measurement_unit TEXT,
    variable_role TEXT,
    represented_variable_label TEXT,
    conceptual_variable_label TEXT,
    percentage_of_filled_entries INTEGER,
    CONSTRAINT variable_logical_record_fk FOREIGN KEY (logical_record_id) REFERENCES logical_record (logical_record_id),
    CONSTRAINT variable_code_list_fk FOREIGN KEY (code_list_id) REFERENCES code_list (code_list_id)
);

CREATE TABLE concept
(
    concept_id UUID PRIMARY KEY,
    variable_id UUID NOT NULL,
    label TEXT,
    CONSTRAINT concept_variable_fk FOREIGN KEY (variable_id) REFERENCES variable (variable_id)
);

CREATE TABLE quality_indicator
(
    quality_indicator_id UUID PRIMARY KEY,
    logical_record_id UUID,
    variable_id UUID,
    name TEXT,
    label TEXT,
    CONSTRAINT quality_indicator_variable_fk FOREIGN KEY (variable_id) REFERENCES variable (variable_id)
);

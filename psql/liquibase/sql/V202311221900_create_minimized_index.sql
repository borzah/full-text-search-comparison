CREATE TABLE study_search_document_store
(
    study_id UUID PRIMARY KEY,
    series_title TEXT,
    universe_label TEXT NOT NULL,
    title TEXT,
    purpose TEXT,
    reference_area TEXT,
    time_coverage TEXT,
    study_search_document TSVECTOR,
    CONSTRAINT study_search_document_store_study_fk FOREIGN KEY (study_id) REFERENCES study (study_id)
);

CREATE INDEX idx_study_search_document_store_study_search_document ON study_search_document_store USING gin(study_search_document);

CREATE TABLE variable_search_document_store
(
    variable_id UUID PRIMARY KEY,
    study_id UUID NOT NULL,
    logical_record_id UUID NOT NULL,
    name TEXT,
    label TEXT,
    representation_type TEXT,
    variable_search_document TSVECTOR,
    CONSTRAINT variable_search_document_study_fk FOREIGN KEY (study_id) REFERENCES study (study_id)
);

CREATE INDEX idx_variable_search_document_store_study_id ON variable_search_document_store (study_id);
CREATE INDEX idx_variable_search_document_store_variable_search_document ON variable_search_document_store USING gin(variable_search_document);

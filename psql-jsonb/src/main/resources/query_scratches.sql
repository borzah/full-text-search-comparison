--####################################
-- MATERIALIZED VIEW WAY
--####################################
-- refresh view when data is updated
REFRESH MATERIALIZED VIEW study_search_index;

ANALYZE study_search_index;

-- search query
-- select * from (
--     select distinct on (study_id) study_id,
--                     series_title as series_title,
--                     universe_label as universe_label,
--                     title as title,
--                     purpose as purpose,
--                     reference_area as reference_area,
--                     time_coverage as time_coverage,
--                     ts_rank(document, websearch_to_tsquery(:search)) as rank
--     from study_search_index
--     where document @@ websearch_to_tsquery(:search)
--     order by study_id, rank
--     ) sub
-- order by rank desc;
--
--
-- -- for variables
-- select distinct on (variable_id) variable_id,
--                     study_id as study_id,
--                     logical_record_id as logical_record_id,
--                     ts_headline(variable_label, websearch_to_tsquery(:search)) as variable_label,
--                     ts_headline(variable_name, websearch_to_tsquery(:search)) as variable_name,
--                     representation_type as representation_type,
--                     ts_rank(variable_document, websearch_to_tsquery(:search)) as rank
-- from study_search_index
-- where variable_document @@ websearch_to_tsquery(:search)
-- and study_id in :ids
-- order by variable_id;

-- search query compressed indexex
select study_id as study_id,
       series_title as series_title,
       universe_label as universe_label,
       title as title,
       purpose as purpose,
       reference_area as reference_area,
       time_coverage as time_coverage,
       ts_rank(study_search_document, websearch_to_tsquery(:search)) as rank
from study_search_document_store
where study_search_document @@ websearch_to_tsquery(:search)
order by rank desc
fetch first 10 rows only;


-- for variables
select variable_id,
       study_id as study_id,
       logical_record_id as logical_record_id,
       ts_headline(label, websearch_to_tsquery(:search)) as variable_label,
       ts_headline(name, websearch_to_tsquery(:search)) as variable_name,
       representation_type as representation_type,
       ts_rank(variable_search_document, websearch_to_tsquery(:search)) as rank
from variable_search_document_store
where variable_search_document @@ websearch_to_tsquery(:search)
and study_id in :ids
order by rank desc;

ANALYZE study_search_document_store;
ANALYZE variable_search_document_store;
--- with agg and subqueries

WITH variables_match AS (
    SELECT variable_id AS variable_id,
           study_id AS study_id,
           logical_record_id AS logical_record_id,
           ts_headline(label, websearch_to_tsquery(:search)) AS variable_label,
           ts_headline(name, websearch_to_tsquery(:search)) AS variable_name,
           representation_type AS representation_type,
           ts_rank(variable_search_document, websearch_to_tsquery(:search)) AS variable_rank
    FROM variable_search_document_store
    WHERE variable_search_document @@ websearch_to_tsquery(:search)
    ),
    variables_selected AS (
        SELECT variable_id,
               study_id,
               logical_record_id,
               variable_label,
               variable_name,
               representation_type,
               variable_rank
        FROM variables_match
        ORDER BY variable_rank DESC
    )
SELECT                        study_id as study_id,
                              series_title AS series_title,
                              universe_label AS universe_label,
                              title AS title,
                              purpose AS purpose,
                              reference_area AS reference_area,
                              time_coverage AS time_coverage,
                              ts_rank(study_search_document, websearch_to_tsquery(:search)) AS study_rank,
                              json_agg(jsonb_build_object(
                                      'variable', variable_id,
                                      'logicalRecordId', logical_record_id,
                                      'variableLabel', variable_label,
                                      'variableName', variable_name,
                                      'representationType', representation_type,
                                      'variableRank', variable_rank) ORDER BY variable_rank ) FILTER ( WHERE variable_rank > 0 ) AS variables
FROM study_search_document_store LEFT JOIN variables_selected USING (study_id)
WHERE study_search_document @@ websearch_to_tsquery(:search)
GROUP BY study_id, series_title, universe_label, title, purpose, reference_area, time_coverage, ts_rank(study_search_document, websearch_to_tsquery(:search))
ORDER BY study_rank DESC;

select count(*) FROM study_search_document_store
WHERE study_search_document @@ websearch_to_tsquery(:search);

-- no rank
WITH variables_match AS (
    SELECT variable_id AS match_variable_id,
           study_id AS study_id,
           logical_record_id AS match_logical_record_id,
           ts_headline(variable_label, websearch_to_tsquery(:search)) AS match_variable_label,
           ts_headline(variable_name, websearch_to_tsquery(:search)) AS match_variable_name,
           representation_type AS match_representation_type
    FROM study_search_index
    WHERE variable_document @@ websearch_to_tsquery(:search)
),
     variables_selected AS (
         SELECT match_variable_id,
                study_id,
                match_logical_record_id,
                match_variable_label,
                match_variable_name,
                match_representation_type
         FROM variables_match
         FETCH FIRST 10 ROWS ONLY
     )
SELECT                        study_id as study_id,
                              series_title AS series_title,
                              universe_label AS universe_label,
                              title AS title,
                              purpose AS purpose,
                              reference_area AS reference_area,
                              time_coverage AS time_coverage,
                              json_agg(jsonb_build_object(
                                      'variable', match_variable_id,
                                      'logicalRecordId', match_logical_record_id,
                                      'variableLabel', match_variable_label,
                                      'variableName', match_variable_name,
                                      'representationType', match_representation_type)) AS variables
FROM study_search_index LEFT JOIN variables_selected USING (study_id)
WHERE document @@ websearch_to_tsquery(:search)
GROUP BY study_id, series_title, universe_label, title, purpose, reference_area, time_coverage;

ANALYZE study_search_index;
-- with agg
-- search query
--todo sort in aggregation
SELECT * FROM (
                  SELECT DISTINCT ON (study_id) study_id,
                                                series_title AS series_title,
                                                universe_label AS universe_label,
                                                title AS title,
                                                purpose AS purpose,
                                                reference_area AS reference_area,
                                                time_coverage AS time_coverage,
                                                json_agg(DISTINCT jsonb_build_object(
                                                        'variable', variable_id,
                                                        'logicalRecordId', logical_record_id,
                                                        'variableLabel', ts_headline(variable_label, websearch_to_tsquery(:search)),
                                                        'variableName', ts_headline(variable_name, websearch_to_tsquery(:search)),
                                                        'representationType', representation_type,
                                                        'variableRank', ts_rank(variable_document, websearch_to_tsquery(:search))
                                                         )) FILTER (WHERE variable_document @@ websearch_to_tsquery(:search)) AS variables,
                                                ts_rank(document, websearch_to_tsquery(:search)) AS rank
                  FROM study_search_index
                  WHERE document @@ websearch_to_tsquery(:search)
                  GROUP BY study_id, series_title, universe_label, title, purpose, reference_area, time_coverage, ts_rank(document, websearch_to_tsquery(:search))
              ) sub
ORDER BY rank DESC;


-- count for search query
select count(*) from (
    select distinct on (study_id) study_id,
                    ts_rank(document, websearch_to_tsquery('gadzook')) as rank
    from study_search_index
    where document @@ websearch_to_tsquery('gadzook')) as total;

-- select count(distinct on (study_id) as study_id, series_title, universe_label, title, purpose, reference_area, time_coverage,
--              ts_rank(document, websearch_to_tsquery('gadzook'))) as total
-- from study_search_index
-- where document @@ websearch_to_tsquery('gadzook');

-- query without search input
select distinct on (study_id) study_id, series_title, universe_label, title, purpose, reference_area, time_coverage
from study_search_index
offset 0
fetch first 20 rows only;

-- count without search input
select count(distinct study_id) as total
from study_search_index;


with studies AS (select study_id, title,
                        ts_rank(document, websearch_to_tsquery(:search)) as rank
                     from study_search_index
                     where document @@ websearch_to_tsquery(:search)
) ,
     variables AS (select variable_id,
                          study_id,
                          logical_record_id,
                          ts_headline(variable_label, websearch_to_tsquery(:search)) as variable_label,
                          ts_headline(variable_name, websearch_to_tsquery(:search)) as variable_name,
                          representation_type,
                          concept_label
                   from study_search_index
                   where variable_document @@ websearch_to_tsquery(:search))
    SELECT distinct studies.*, variables.*
FROM studies INNER JOIN variables USING (study_id)
ORDER BY rank;

ANALYZE domain_json;

ANALYZE concept;
ANALYZE variable;
ANALYZE logical_record;
ANALYZE keyword;
ANALYZE subject;
ANALYZE study;
ANALYZE series;
ANALYZE subdomain;
ANALYZE metadata_domain;
--####################################
-- TSVECTOR STORED IN COLUMNS WAY
--####################################
-- TODO try index on foreign key
SELECT * FROM (
SELECT DISTINCT ON
    (s.study_id)  s.study_id as study_id,
    s.series_title as series_title,
    s.universe_label as universe_label,
    s.title as title,
    s.purpose as purpose,
    s.reference_area as reference_area,
    s.time_coverage as time_coverage,
    json_agg(DISTINCT jsonb_build_object(
            'variable', v.variable_id,
            'logicalRecordId', v.logical_record_id,
            'variableLabel', ts_headline(v.label, websearch_to_tsquery(:search)),
            'variableName', ts_headline(v.name, websearch_to_tsquery(:search)),
            'representationType', v.representation_type,
            'variableRank', ts_rank(v.variable_search || c.concept_search, websearch_to_tsquery(:search))
                      )) filter (where v.variable_search || c.concept_search @@ websearch_to_tsquery(:search)) AS variables,
    ts_rank(s.study_search ||
            df.data_file_search ||
            kw.keyword_search ||
            sb.subject_search ||
            lr.logical_record_search ||
            v.variable_search ||
            c.concept_search, websearch_to_tsquery(:search)) as rank
FROM concept c
         JOIN variable v USING (variable_id)
         JOIN logical_record lr USING (logical_record_id)
         JOIN data_file df USING (data_file_id)
         JOIN keyword kw USING (data_file_id)
         JOIN subject sb USING (data_file_id)
         JOIN study s USING (study_id)
         JOIN series sr ON sr.series_id = s.series_id
         JOIN subdomain sd ON sd.subdomain_id = s.subdomain_id
         JOIN metadata_domain md ON md.metadata_domain_id = s.metadata_domain_id
WHERE s.study_search ||
      df.data_file_search ||
      kw.keyword_search ||
      sb.subject_search ||
      lr.logical_record_search ||
      v.variable_search ||
      c.concept_search @@ websearch_to_tsquery(:search)
GROUP BY s.study_id, s.series_title, s.universe_label, s.title, s.purpose, s.reference_area, s.time_coverage, ts_rank(s.study_search ||
            df.data_file_search ||
            kw.keyword_search ||
            sb.subject_search ||
            lr.logical_record_search ||
            v.variable_search ||
            c.concept_search, websearch_to_tsquery(:search))
) sub
ORDER BY rank DESC
OFFSET 0
FETCH FIRST 10 ROWS ONLY;


SELECT * FROM (
                  SELECT DISTINCT ON
                      (s.study_id)  s.study_id as study_id,
                                    s.series_title as series_title,
                                    s.universe_label as universe_label,
                                    s.title as title,
                                    s.purpose as purpose,
                                    s.reference_area as reference_area,
                                    s.time_coverage as time_coverage,
                                    ts_rank((s.study_search::text || ' ' ||
                                            df.data_file_search::text || ' ' ||
                                            kw.keyword_search::text || ' ' ||
                                            sb.subject_search::text || ' ' ||
                                            lr.logical_record_search::text || ' ' ||
                                            v.variable_search::text || ' ' ||
                                            c.concept_search::text)::tsvector, websearch_to_tsquery(:search)) as rank
                  FROM concept c
                           JOIN variable v USING (variable_id)
                           JOIN logical_record lr USING (logical_record_id)
                           JOIN data_file df USING (data_file_id)
                           JOIN keyword kw USING (data_file_id)
                           JOIN subject sb USING (data_file_id)
                           JOIN study s USING (study_id)
                           JOIN series sr ON sr.series_id = s.series_id
                           JOIN subdomain sd ON sd.subdomain_id = s.subdomain_id
                           JOIN metadata_domain md ON md.metadata_domain_id = s.metadata_domain_id
                  WHERE (s.study_search::text || ' ' ||
                         df.data_file_search::text || ' ' ||
                         kw.keyword_search::text || ' ' ||
                         sb.subject_search::text || ' ' ||
                         lr.logical_record_search::text || ' ' ||
                         v.variable_search::text || ' ' ||
                         c.concept_search::text)::tsvector @@ websearch_to_tsquery(:search)) sub
ORDER BY rank DESC;

--####################################
-- JSON WAY
--####################################

ANALYZE study_json;

select distinct studies.data-> 'id' as studyId,
                studies.data->'seriesTitle' as seriesTitle,
                studies.data->'universeLabel' as universeLabel,
                studies.data->'title' as title,
                studies.data->'purpose' as purpose,
                studies.data->'referenceArea' as referenceArea,
                studies.data->'timeCoverage' as timeCoverage
from study_json as studies,
     jsonb_array_elements(studies.data->'dataFiles') as dataFiles,
     jsonb_array_elements(dataFiles->'subjects') as subjects,
     jsonb_array_elements(dataFiles->'keywords') as keywords,
     jsonb_array_elements(dataFiles->'logicalRecords') as logicalRecords,
     jsonb_array_elements(logicalRecords->'variables') as variables,
     jsonb_array_elements(variables->'concepts') as concepts
where
studies.data->>'seriesTitle' ILIKE :search
or studies.data->>'universeLabel' ILIKE :search
or studies.data->>'title' ILIKE :search
or studies.data->>'summary' ILIKE :search
or studies.data->>'purpose' ILIKE :search
or studies.data->>'studyCode' ILIKE :search
or studies.data->>'sectorCoverage' ILIKE :search
or studies.data->>'otherDissemination' ILIKE :search
or studies.data->>'documentationOnMethodology' ILIKE :search
or studies.data->>'geographicalComparability' ILIKE :search
or studies.data->>'comparabilityOverTime' ILIKE :search
or studies.data->>'sourceData' ILIKE :search
or studies.data->>'frequencyOfDataCollection' ILIKE :search
or studies.data->>'dataCollection' ILIKE :search
or studies.data->>'dataValidation' ILIKE :search
or studies.data->>'dataCompilation' ILIKE :search
or dataFiles->>'title' ILIKE :search
or keywords->>'name' ILIKE :search
or subjects->>'name' ILIKE :search
or logicalRecords->>'name' ILIKE :search
or logicalRecords->>'label' ILIKE :search
or logicalRecords->>'description' ILIKE :search
or variables->>'name' ILIKE :search
or variables->>'label' ILIKE :search
or variables->>'description' ILIKE :search
or variables->>'representedVariableLabel' ILIKE :search
or variables->>'conceptualVariableLabel' ILIKE :search
or concepts->>'label' ILIKE :search;

SELECT DISTINCT study.data-> 'id' as studyId,
       study.data->'seriesTitle' as seriesTitle,
       study.data->'universeLabel' as universeLabel,
       study.data->'title' as title,
       study.data->'purpose' as purpose,
       study.data->'referenceArea' as referenceArea,
       study.data->'timeCoverage' as timeCoverage
FROM study_json study
         LEFT JOIN LATERAL jsonb_array_elements(study.data->'dataFiles') AS dataFiles ON true
         LEFT JOIN LATERAL jsonb_array_elements(dataFiles->'keywords') AS keywords ON true
         LEFT JOIN LATERAL jsonb_array_elements(dataFiles->'subjects') AS subjects ON true
         LEFT JOIN LATERAL jsonb_array_elements(dataFiles->'logicalRecords') AS logicalRecords ON true
         LEFT JOIN LATERAL jsonb_array_elements(logicalRecords->'variables') AS variables ON true
         LEFT JOIN LATERAL jsonb_array_elements(variables->'concepts') AS concepts ON true
WHERE
            study.data->>'seriesTitle' ILIKE :search
   or study.data->>'universeLabel' ILIKE :search
   or study.data->>'title' ILIKE :search
   or study.data->>'summary' ILIKE :search
   or study.data->>'purpose' ILIKE :search
   or study.data->>'studyCode' ILIKE :search
   or study.data->>'sectorCoverage' ILIKE :search
   or study.data->>'otherDissemination' ILIKE :search
   or study.data->>'documentationOnMethodology' ILIKE :search
   or study.data->>'geographicalComparability' ILIKE :search
   or study.data->>'comparabilityOverTime' ILIKE :search
   or study.data->>'sourceData' ILIKE :search
   or study.data->>'frequencyOfDataCollection' ILIKE :search
   or study.data->>'dataCollection' ILIKE :search
   or study.data->>'dataValidation' ILIKE :search
   or study.data->>'dataCompilation' ILIKE :search
   or dataFiles->>'title' ILIKE :search
   or keywords->>'name' ILIKE :search
   or subjects->>'name' ILIKE :search
   or logicalRecords->>'name' ILIKE :search
   or logicalRecords->>'label' ILIKE :search
   or logicalRecords->>'description' ILIKE :search
   or variables->>'name' ILIKE :search
   or variables->>'label' ILIKE :search
   or variables->>'description' ILIKE :search
   or variables->>'representedVariableLabel' ILIKE :search
   or variables->>'conceptualVariableLabel' ILIKE :search
   or concepts->>'label' ILIKE :search;

SELECT DISTINCT study.data-> 'id' as studyId,
                study.data->'seriesTitle' as seriesTitle,
                study.data->'universeLabel' as universeLabel,
                study.data->'title' as title,
                study.data->'purpose' as purpose,
                study.data->'referenceArea' as referenceArea,
                study.data->'timeCoverage' as timeCoverage
FROM study_json study
         LEFT JOIN LATERAL jsonb_array_elements(study.data->'dataFiles') AS dataFiles ON true
         LEFT JOIN LATERAL jsonb_array_elements(dataFiles->'keywords') AS keywords ON true
         LEFT JOIN LATERAL jsonb_array_elements(dataFiles->'subjects') AS subjects ON true
         LEFT JOIN LATERAL jsonb_array_elements(dataFiles->'logicalRecords') AS logicalRecords ON true
         LEFT JOIN LATERAL jsonb_array_elements(logicalRecords->'variables') AS variables ON true
         LEFT JOIN LATERAL jsonb_array_elements(variables->'concepts') AS concepts ON true
WHERE
    to_tsvector(
      coalesce(study.data->>'seriesTitle', '') || '' ||
      coalesce(study.data->>'universeLabel', '') || '' ||
      coalesce(study.data->>'title', '') || '' ||
      coalesce(study.data->>'summary', '') || '' ||
      coalesce(study.data->>'purpose', '') || '' ||
      coalesce(study.data->>'studyCode', '') || '' ||
      coalesce(study.data->>'sectorCoverage', '') || '' ||
      coalesce(study.data->>'otherDissemination', '') || '' ||
      coalesce(study.data->>'documentationOnMethodology', '') || '' ||
      coalesce(study.data->>'geographicalComparability', '') || '' ||
      coalesce(study.data->>'comparabilityOverTime', '') || '' ||
      coalesce(study.data->>'sourceData', '') || '' ||
      coalesce(study.data->>'frequencyOfDataCollection', '') || '' ||
      coalesce(study.data->>'dataCollection', '') || '' ||
      coalesce(study.data->>'dataValidation', '') || '' ||
      coalesce(study.data->>'dataCompilation', '') || '' ||
      coalesce(dataFiles->>'title', '') || '' ||
      coalesce(keywords->>'name', '') || '' ||
      coalesce(subjects->>'name', '') || '' ||
      coalesce(logicalRecords->>'name', '')  || '' ||
      coalesce(logicalRecords->>'label', '') || '' ||
      coalesce(logicalRecords->>'description', '') || '' ||
      coalesce(variables->>'name', '') || '' ||
      coalesce(variables->>'label', '')  || '' ||
      coalesce(variables->>'description', '') || '' ||
      coalesce(variables->>'representedVariableLabel', '') || '' ||
      coalesce(variables->>'conceptualVariableLabel', '') || '' ||
      coalesce(concepts->>'label', ''))  @@ websearch_to_tsquery(:search1);


select jsonb_array_elements_text(data -> 'domains') as domains from domain_json

WITH hiring AS (SELECT deptno, ename, DENSE_RANK() OVER(PARTITION BY deptno ORDER BY hiredate) AS rank
                FROM Emp
                WHERE job='SALESMAN'),
     selected AS (SELECT deptno, ename, rank
                  FROM hiring
                  WHERE rank<=2
                  ORDER BY rank
                      FETCH FIRST 2 ROWS ONLY)
SELECT deptno, dname, string_agg(ename, ',' ORDER BY rank) AS persons
FROM Dept LEFT JOIN selected USING (deptno)
GROUP BY deptno, dname;

WITH hiring AS (SELECT deptno, ename, DENSE_RANK() OVER(PARTITION BY deptno ORDER BY hiredate) AS rank
                FROM Emp
                WHERE job='SALESMAN')
SELECT deptno, dname, string_agg(ename, ',' ORDER BY rank) FILTER (WHERE rank<=2) AS persons
FROM Dept LEFT JOIN hiring USING (deptno)
GROUP BY deptno, dname;

ANALYZE domain_json;

refresh materialized view study_search_json_index;
refresh materialized view study_search_json_text_index;

select distinct studyId,
                seriesTitle,
                universeLabel,
                title,
                purpose,
                referenceArea,
                timeCoverage
from study_search_json_index
where
    studies->>'seriesTitle' ILIKE :search
   or studies->>'universeLabel' ILIKE :search
   or studies->>'title' ILIKE :search
   or studies->>'summary' ILIKE :search
   or studies->>'purpose' ILIKE :search
   or studies->>'studyCode' ILIKE :search
   or studies->>'sectorCoverage' ILIKE :search
   or studies->>'otherDissemination' ILIKE :search
   or studies->>'documentationOnMethodology' ILIKE :search
   or studies->>'geographicalComparability' ILIKE :search
   or studies->>'comparabilityOverTime' ILIKE :search
   or studies->>'sourceData' ILIKE :search
   or studies->>'frequencyOfDataCollection' ILIKE :search
   or studies->>'dataCollection' ILIKE :search
   or studies->>'dataValidation' ILIKE :search
   or studies->>'dataCompilation' ILIKE :search
   or dataFiles->>'title' ILIKE :search
   or keywords->>'name' ILIKE :search
   or subjects->>'name' ILIKE :search
   or logicalRecords->>'name' ILIKE :search
   or logicalRecords->>'label' ILIKE :search
   or logicalRecords->>'description' ILIKE :search
   or variables->>'name' ILIKE :search
   or variables->>'label' ILIKE :search
   or variables->>'description' ILIKE :search
   or variables->>'representedVariableLabel' ILIKE :search
   or variables->>'conceptualVariableLabel' ILIKE :search
   or concepts->>'label' ILIKE :search;

select distinct studyId,
                seriesTitle,
                universeLabel,
                title,
                purpose,
                referenceArea,
                timeCoverage
from study_search_json_text_index
where
    studies_text ILIKE :search
   or dataFiles_text ILIKE :search
   or keywords_text ILIKE :search
   or subjects_text ILIKE :search
   or logicalRecords_text ILIKE :search
   or variables_text ILIKE :search
   or concepts_text ILIKE :search;

analyse domain_json

explain select distinct studies-> 'id' as studyId,
                studies->'seriesTitle' as seriesTitle,
                studies->'universeLabel' as universeLabel,
                studies->'title' as title,
                studies->'purpose' as purpose,
                studies->'referenceArea' as referenceArea,
                studies->'timeCoverage' as timeCoverage
from domain_json,
     jsonb_array_elements(data->'domains') as domains,
     jsonb_array_elements(domains->'subdomains') as subdomains,
     jsonb_array_elements(subdomains->'series') as series,
     jsonb_array_elements(series->'studies') as studies,
     jsonb_array_elements(studies->'dataFiles') as dataFiles,
     jsonb_array_elements(dataFiles->'subjects') as subjects,
     jsonb_array_elements(dataFiles->'keywords') as keywords,
     jsonb_array_elements(dataFiles->'logicalRecords') as logicalRecords,
     jsonb_array_elements(logicalRecords->'variables') as variables,
     jsonb_array_elements(variables->'concepts') as concepts
where
            studies->>'seriesTitle' ILIKE :search
   or studies->>'universeLabel' ILIKE :search
   or studies->>'title' ILIKE :search
   or studies->>'summary' ILIKE :search
   or studies->>'purpose' ILIKE :search
   or studies->>'studyCode' ILIKE :search
   or studies->>'sectorCoverage' ILIKE :search
   or studies->>'otherDissemination' ILIKE :search
   or studies->>'documentationOnMethodology' ILIKE :search
   or studies->>'geographicalComparability' ILIKE :search
   or studies->>'comparabilityOverTime' ILIKE :search
   or studies->>'sourceData' ILIKE :search
   or studies->>'frequencyOfDataCollection' ILIKE :search
   or studies->>'dataCollection' ILIKE :search
   or studies->>'dataValidation' ILIKE :search
   or studies->>'dataCompilation' ILIKE :search
   or dataFiles->>'title' ILIKE :search
   or keywords->>'name' ILIKE :search
   or subjects->>'name' ILIKE :search
   or logicalRecords->>'name' ILIKE :search
   or logicalRecords->>'label' ILIKE :search
   or logicalRecords->>'description' ILIKE :search
   or variables->>'name' ILIKE :search
   or variables->>'label' ILIKE :search
   or variables->>'description' ILIKE :search
   or variables->>'representedVariableLabel' ILIKE :search
   or variables->>'conceptualVariableLabel' ILIKE :search
   or concepts->>'label' ILIKE :search;

select distinct studies-> 'id' as studyId,
                studies->'seriesTitle' as seriesTitle,
                studies->'universeLabel' as universeLabel,
                studies->'title' as title,
                studies->'purpose' as purpose,
                studies->'referenceArea' as referenceArea,
                studies->'timeCoverage' as timeCoverage
from domain_json,
     jsonb_array_elements(data->'domains') as domains,
     jsonb_array_elements(domains->'subdomains') as subdomains,
     jsonb_array_elements(subdomains->'series') as series,
     jsonb_array_elements(series->'studies') as studies,
     jsonb_array_elements(studies->'dataFiles') as dataFiles,
     jsonb_array_elements(dataFiles->'subjects') as subjects,
     jsonb_array_elements(dataFiles->'keywords') as keywords,
     jsonb_array_elements(dataFiles->'logicalRecords') as logicalRecords,
     jsonb_array_elements(logicalRecords->'variables') as variables,
     jsonb_array_elements(variables->'concepts') as concepts
where
      to_tsvector(studies->>'seriesTitle') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'universeLabel') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'title') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'summary') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'purpose') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'studyCode') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'sectorCoverage') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'otherDissemination') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'documentationOnMethodology') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'geographicalComparability') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'comparabilityOverTime') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'sourceData') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'frequencyOfDataCollection') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'dataCollection') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'dataValidation') @@ websearch_to_tsquery(:search)
   or to_tsvector(studies->>'dataCompilation') @@ websearch_to_tsquery(:search)
   or to_tsvector(dataFiles->>'title') @@ websearch_to_tsquery(:search)
   or to_tsvector(keywords->>'name') @@ websearch_to_tsquery(:search)
   or to_tsvector(subjects->>'name') @@ websearch_to_tsquery(:search)
   or to_tsvector(logicalRecords->>'name') @@ websearch_to_tsquery(:search)
   or to_tsvector(logicalRecords->>'label') @@ websearch_to_tsquery(:search)
   or to_tsvector(logicalRecords->>'description') @@ websearch_to_tsquery(:search)
   or to_tsvector(variables->>'name') @@ websearch_to_tsquery(:search)
   or to_tsvector(variables->>'label') @@ websearch_to_tsquery(:search)
   or to_tsvector(variables->>'description') @@ websearch_to_tsquery(:search)
   or to_tsvector(variables->>'representedVariableLabel') @@ websearch_to_tsquery(:search)
   or to_tsvector(variables->>'conceptualVariableLabel') @@ websearch_to_tsquery(:search)
   or to_tsvector(concepts->>'label') @@ websearch_to_tsquery(:search);


select distinct studies-> 'id' as studyId,
                studies->'seriesTitle' as seriesTitle,
                studies->'universeLabel' as universeLabel,
                studies->'title' as title,
                studies->'purpose' as purpose,
                studies->'referenceArea' as referenceArea,
                studies->'timeCoverage' as timeCoverage
from domain_json,
     jsonb_array_elements(data->'domains') as domains,
     jsonb_array_elements(domains->'subdomains') as subdomains,
     jsonb_array_elements(subdomains->'series') as series,
     jsonb_array_elements(series->'studies') as studies,
     jsonb_array_elements(studies->'dataFiles') as dataFiles,
     jsonb_array_elements(dataFiles->'subjects') as subjects,
     jsonb_array_elements(dataFiles->'keywords') as keywords,
     jsonb_array_elements(dataFiles->'logicalRecords') as logicalRecords,
     jsonb_array_elements(logicalRecords->'variables') as variables,
     jsonb_array_elements(variables->'concepts') as concepts
where
    to_tsvector(coalesce(studies->>'seriesTitle', '') || '' ||
   coalesce(studies->>'universeLabel', '') || '' ||
   coalesce(studies->>'title', '') || '' ||
   coalesce(studies->>'summary', '') || '' ||
   coalesce(studies->>'purpose', '') || '' ||
   coalesce(studies->>'studyCode', '') || '' ||
   coalesce(studies->>'sectorCoverage', '') || '' ||
   coalesce(studies->>'otherDissemination', '') || '' ||
   coalesce(studies->>'documentationOnMethodology', '') || '' ||
   coalesce(studies->>'geographicalComparability', '') || '' ||
   coalesce(studies->>'comparabilityOverTime', '') || '' ||
   coalesce(studies->>'sourceData', '') || '' ||
   coalesce(studies->>'frequencyOfDataCollection', '') || '' ||
   coalesce(studies->>'dataCollection', '') || '' ||
   coalesce(studies->>'dataValidation', '') || '' ||
   coalesce(studies->>'dataCompilation', '') || '' ||
   coalesce(dataFiles->>'title', '') || '' ||
   coalesce(keywords->>'name', '') || '' ||
   coalesce(subjects->>'name', '') || '' ||
   coalesce(logicalRecords->>'name', '') || '' ||
   coalesce(logicalRecords->>'label', '') || '' ||
   coalesce(logicalRecords->>'description', '') || '' ||
   coalesce(variables->>'name', '') || '' ||
   coalesce(variables->>'label', '') || '' ||
   coalesce(variables->>'description', '') || '' ||
   coalesce(variables->>'representedVariableLabel', '') || '' ||
   coalesce(variables->>'conceptualVariableLabel', '') || '' ||
   coalesce(concepts->>'label', '')) @@ websearch_to_tsquery(:search1);

CREATE INDEX metadata_json_gin_studies_series_title_idx ON domain_json USING  GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'seriesTitle') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_universe_label_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'universeLabel') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_title_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'title') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_summary_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'summary') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_purpose_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'purpose') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_study_code_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'studyCode') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_sector_coverage_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'sectorCoverage') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_other_dissemination_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'otherDissemination') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_documentation_on_methodology_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'documentationOnMethodology') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_geographical_comparability_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'geographicalComparability') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_comparability_over_time_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'comparabilityOverTime') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_source_data_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'sourceData') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_frequency_of_data_collection_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'frequencyOfDataCollection') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_data_collection_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'dataCollection') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_data_validation_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'dataValidation') gin_trgm_ops);
CREATE INDEX metadata_json_gin_studies_data_compilation_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies'->> 'dataCompilation') gin_trgm_ops);
CREATE INDEX metadata_json_gin_data_files_title_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' ->> 'title') gin_trgm_ops);
CREATE INDEX metadata_json_gin_keyword_name_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'keywords'->> 'name') gin_trgm_ops);
CREATE INDEX metadata_json_gin_subject_name_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'subjects'->> 'name') gin_trgm_ops);
CREATE INDEX metadata_json_gin_logical_record_name_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->> 'name') gin_trgm_ops);
CREATE INDEX metadata_json_gin_logical_record_label_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->> 'label') gin_trgm_ops);
CREATE INDEX metadata_json_gin_logical_record_description_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->> 'description') gin_trgm_ops);
CREATE INDEX metadata_json_gin_variable_name_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'name') gin_trgm_ops);
CREATE INDEX metadata_json_gin_variable_label_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'label') gin_trgm_ops);
CREATE INDEX metadata_json_gin_variable_description_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'description') gin_trgm_ops);
CREATE INDEX metadata_json_gin_variable_represented_variable_label_idx ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'representedVariableLabel') gin_trgm_ops);
CREATE INDEX metadata_json_gin_variable_conceptual_variable_label ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' ->> 'conceptualVariableLabel') gin_trgm_ops);
CREATE INDEX metadata_json_gin_concept_label ON domain_json USING GIN ((data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' -> 'concepts' ->> 'label') gin_trgm_ops);

analyze domain_json;

SELECT DISTINCT studies->>'id' AS studyId,
                studies->>'seriesTitle' AS seriesTitle,
                studies->>'universeLabel' AS universeLabel,
                studies->>'title' AS title,
                studies->>'purpose' AS purpose,
                studies->>'referenceArea' AS referenceArea,
                studies->>'timeCoverage' AS timeCoverage
        FROM domain_json,
             jsonb_array_elements(data->'domains') AS domains,
             jsonb_array_elements(domains->'subdomains') AS subdomains,
             jsonb_array_elements(subdomains->'series') AS series,
             jsonb_array_elements(series->'studies') AS studies,
             jsonb_array_elements(studies->'dataFiles') AS dataFiles,
             jsonb_array_elements(dataFiles->'subjects') AS subjects,
             jsonb_array_elements(dataFiles->'keywords') AS keywords,
             jsonb_array_elements(dataFiles->'logicalRecords') AS logicalRecords,
             jsonb_array_elements(logicalRecords->'variables') AS variables,
             jsonb_array_elements(variables->'concepts') AS concepts
        WHERE
              studies->>'seriesTitle' ILIKE :search
           OR studies->>'universeLabel' ILIKE :search
           OR studies->>'title' ILIKE :search
           OR studies->>'summary' ILIKE :search
           OR studies->>'purpose' ILIKE :search
           OR studies->>'studyCode' ILIKE :search
           OR studies->>'sectorCoverage' ILIKE :search
           OR studies->>'otherDissemination' ILIKE :search
           OR studies->>'documentationOnMethodology' ILIKE :search
           OR studies->>'geographicalComparability' ILIKE :search
           OR studies->>'comparabilityOverTime' ILIKE :search
           OR studies->>'sourceData' ILIKE :search
           OR studies->>'frequencyOfDataCollection' ILIKE :search
           OR studies->>'dataCollection' ILIKE :search
           OR studies->>'dataValidation' ILIKE :search
           OR studies->>'dataCompilation' ILIKE :search
           OR dataFiles->>'title' ILIKE :search
           OR keywords->>'name' ILIKE :search
           OR subjects->>'name' ILIKE :search
           OR logicalRecords->>'name' ILIKE :search
           OR logicalRecords->>'label' ILIKE :search
           OR logicalRecords->>'description' ILIKE :search
           OR variables->>'name' ILIKE :search
           OR variables->>'label' ILIKE :search
           OR variables->>'description' ILIKE :search
           OR variables->>'representedVariableLabel' ILIKE :search
           OR variables->>'conceptualVariableLabel' ILIKE :search
           OR concepts->>'label' ILIKE :search
OFFSET 0
FETCH NEXT 10 ROWS ONLY;


select distinct studies->'id' as studyId,
                        studies->'seriesTitle' as seriesTitle,
                        studies->'universeLabel' as universeLabel,
                        studies->'title' as title,
                        studies->'purpose' as purpose,
                        studies->'referenceArea' as referenceArea,
                        studies->'timeCoverage' as timeCoverage
        from study_search_json_index
        where
                    studies->>'seriesTitle' ILIKE :search
           or studies->>'universeLabel' ILIKE :search
           or studies->>'title' ILIKE :search
           or studies->>'summary' ILIKE :search
           or studies->>'purpose' ILIKE :search
           or studies->>'studyCode' ILIKE :search
           or studies->>'sectorCoverage' ILIKE :search
           or studies->>'otherDissemination' ILIKE :search
           or studies->>'documentationOnMethodology' ILIKE :search
           or studies->>'geographicalComparability' ILIKE :search
           or studies->>'comparabilityOverTime' ILIKE :search
           or studies->>'sourceData' ILIKE :search
           or studies->>'frequencyOfDataCollection' ILIKE :search
           or studies->>'dataCollection' ILIKE :search
           or studies->>'dataValidation' ILIKE :search
           or studies->>'dataCompilation' ILIKE :search
           or dataFiles->>'title' ILIKE :search
           or keywords->>'name' ILIKE :search
           or subjects->>'name' ILIKE :search
           or logicalRecords->>'name' ILIKE :search
           or logicalRecords->>'label' ILIKE :search
           or logicalRecords->>'description' ILIKE :search
           or variables->>'name' ILIKE :search
           or variables->>'label' ILIKE :search
           or variables->>'description' ILIKE :search
           or variables->>'representedVariableLabel' ILIKE :search
           or variables->>'conceptualVariableLabel' ILIKE :search
           or concepts->>'label' ILIKE :search;

select dj.data->'domains'->'subdomains'->'series'->'studies'->>'id' as studyId,
       dj.data->'domains'->'subdomains'->'series'->'studies'->>'seriesTitle' as seriesTitle,
       dj.data->'domains'->'subdomains'->'series'->'studies'->>'universeLabel' as universeLabel,
       dj.data->'domains'->'subdomains'->'series'->'studies'->>'title' as title,
       dj.data->'domains'->'subdomains'->'series'->'studies'->>'purpose' as purpose,
       dj.data->'domains'->'subdomains'->'series'->'studies'->>'referenceArea' as referenceArea,
       dj.data->'domains'->'subdomains'->'series'->'studies'->>'timeCoverage' as timeCoverage
from domain_json dj
where
                            dj.data->'domains'->'subdomains'->'series'->'studies'->>'seriesTitle' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'universeLabel' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'title' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'summary' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'purpose' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'studyCode' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'sectorCoverage' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'otherDissemination' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'documentationOnMethodology' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'geographicalComparability' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'comparabilityOverTime' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'sourceData' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'frequencyOfDataCollection' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'dataCollection' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'dataValidation' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies'->>'dataCompilation' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles'->>'title' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'keywords'->>'name' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'subjects'->>'name' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->>'name' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->>'label' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords'->>'description' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables'->>'name' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables'->>'label' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables'->>'description' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables'->>'representedVariableLabel' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables'->>'conceptualVariableLabel' ILIKE :search
   or dj.data->'domains'->'subdomains'->'series'->'studies' -> 'dataFiles' -> 'logicalRecords' -> 'variables' -> 'concepts'->>'label' ILIKE :search;

analyze study_json;

WITH variables_match AS (
    SELECT variable_id AS variable_id,
           study_id AS study_id,
           logical_record_id AS logical_record_id,
           ts_headline(label, websearch_to_tsquery(:search)) AS variable_label,
           ts_headline(name, websearch_to_tsquery(:search)) AS variable_name,
           representation_type AS representation_type,
           ts_rank(variable_search_document, websearch_to_tsquery(:search)) AS variable_rank
    FROM variable_search_document_store
    WHERE variable_search_document @@ websearch_to_tsquery(:search)
),
     variables_selected AS (
         SELECT variable_id,
                study_id,
                logical_record_id,
                variable_label,
                variable_name,
                representation_type,
                variable_rank
         FROM variables_match
         ORDER BY variable_rank DESC
     )
SELECT                        data,
                              ts_rank(study_search_document, websearch_to_tsquery(:search)) AS study_rank,
                              json_agg(jsonb_build_object(
                                      'variable', variable_id,
                                      'logicalRecordId', logical_record_id,
                                      'variableLabel', variable_label,
                                      'variableName', variable_name,
                                      'representationType', representation_type,
                                      'variableRank', variable_rank) ORDER BY variable_rank ) FILTER ( WHERE variable_rank > 0 ) AS variables
FROM study_json LEFT JOIN variables_selected USING (study_id)
WHERE study_search_document @@ websearch_to_tsquery(:search)
GROUP BY data, ts_rank(study_search_document, websearch_to_tsquery(:search))
ORDER BY study_rank DESC;

select count(study_id) from study;
select count(other_material_id) from other_material;
select count(data_file_id) from data_file;
select count(subject_id) from subject;
select count(keyword_id) from keyword;
select count(logical_record_id) from logical_record;
select count(variable_id) from variable;
select count(concept_id) from concept;
select count(quality_indicator_id) from quality_indicator;
select count(code_list_id) from code_list;
select count(category_id) from category;

-- ##### JSONB INDEXING TESTING #####

[
  {
    "guid": "11086bbd-1ed8-4a28-b05d-07bc84cdac61",
    "title": "consectetur sunt",
    "friends": [
      {
        "id": "5c7dc4d0-44f7-466e-b640-58af0ca568e0",
        "name": "officia tempor pariatur"
      },
      {
        "id": "257492d0-6085-4904-8848-26d83012ab4a",
        "name": "incididunt consequat laboris"
      },
      {
        "id": "a86e922a-bff5-423d-b040-40feeada3f45",
        "name": "velit nostrud anim"
      },
      {
        "id": "9b64104a-ebb7-4a81-a584-97fc3b099d61",
        "name": "sunt magna aliqua"
      }
    ]
  },
  {
    "guid": "c4fc91da-2a7d-4ff8-b31e-4f88ae34690d",
    "title": "consequat non",
    "friends": [
      {
        "id": "92bf0c43-9da3-4c01-8f4a-b3e213339e96",
        "name": "cillum minim elit"
      },
      {
        "id": "35805a3c-44c0-4ac2-8a5a-81443a5ab2aa",
        "name": "esse adipisicing nisi"
      },
      {
        "id": "3f696267-edbb-4609-8a72-93a4e790f5fd",
        "name": "officia fugiat enim"
      },
      {
        "id": "ffc6816f-80c1-42e5-8c8d-6b90602f6c2f",
        "name": "tempor minim cillum"
      }
    ]
  },
  {
    "guid": "4111b5ac-49e8-41da-a492-5786592be3b4",
    "title": "id ipsum",
    "friends": [
      {
        "id": "e8178bd9-87ee-46a3-9bbb-99c61063ce79",
        "name": "consequat magna officia"
      },
      {
        "id": "c132b5ab-e039-4b9f-9fb0-5ac3f77cc9e1",
        "name": "occaecat reprehenderit ad"
      },
      {
        "id": "7bd0cdc8-e88a-4285-90ae-e778503c713f",
        "name": "sint in irure"
      }
    ]
  },
  {
    "guid": "3bb4a375-0d3b-4fc5-b482-34a22a7c987d",
    "title": "ad anim",
    "friends": [
      {
        "id": "219708bb-b2e8-46b8-805e-77ebb8404fcf",
        "name": "fugiat nostrud ullamco"
      },
      {
        "id": "f95bd685-9b61-4cb3-9d6d-6fa620e859ff",
        "name": "anim quis magna"
      },
      {
        "id": "b5aa177d-2a9d-428c-8c5a-97d3c8ece92b",
        "name": "deserunt aliqua ad"
      }
    ]
  },
  {
    "guid": "17abdc16-7e89-4340-bb4d-562af4c93f18",
    "title": "qui aliquip",
    "friends": [
      {
        "id": "cb1088b5-37ae-4fa0-8515-6c0a47e0a0d0",
        "name": "minim consequat non"
      },
      {
        "id": "83eb1364-b01e-4e53-bfba-2c3ddcc70a77",
        "name": "fugiat et quis"
      },
      {
        "id": "f48caf21-b48b-4167-b5cf-1826c7958444",
        "name": "ullamco ipsum in"
      }
    ]
  },
  {
    "guid": "a06b06ce-40b2-40c6-bc3b-9d5724db287f",
    "title": "labore nostrud",
    "friends": [
      {
        "id": "dd1c787a-fa96-4d90-b8ec-e5f0f423c55e",
        "name": "et occaecat duis"
      },
      {
        "id": "4ddeae3f-3681-42ac-b35d-e215a6eb0c73",
        "name": "irure amet nostrud"
      },
      {
        "id": "7bb172c6-982e-43b9-8c7d-1bb506e7741b",
        "name": "ad excepteur reprehenderit"
      },
      {
        "id": "359f7812-3cde-4c6d-89a0-796111b581d3",
        "name": "anim labore occaecat"
      }
    ]
  },
  {
    "guid": "4de0ceae-f505-4026-a782-b957a7c7adb1",
    "title": "non nisi",
    "friends": [
      {
        "id": "78c6b17f-5a23-4833-9650-2d598f3afbc0",
        "name": "id in consectetur"
      },
      {
        "id": "79ab703f-9855-49a0-8cac-9aa7df3bcb62",
        "name": "nisi nisi ea"
      },
      {
        "id": "85794307-8e62-40fe-a874-3e706fd52c28",
        "name": "qui sint nulla"
      },
      {
        "id": "8b066634-3e6f-4e5a-b65c-5f7c95f0aff0",
        "name": "pariatur occaecat excepteur"
      }
    ]
  }
]

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX trigram_index_title ON domain_json USING GIN ((data->>'title') gin_trgm_ops);

analyze domain_json

explain analyze SELECT * FROM domain_json
WHERE data->>'title' ILIKE '%non%';

SELECT * FROM domain_json
WHERE data->'friends' @> '[{"name": "consectetur"}]';

SELECT * FROM domain_json
WHERE data->'friends' @> '[{"name": "id in consectetu"}]'::jsonb;

SELECT * FROM domain_json
WHERE data->'friends' @> '[{"name": "incididunt%"}]'::jsonb;

SELECT *
FROM domain_json
WHERE EXISTS (
    SELECT 1
    FROM jsonb_array_elements(data->'friends') AS friend
    WHERE friend->>'name' ILIKE '%incididunt%'
);

explain analyze SELECT DISTINCT dj.*
FROM domain_json dj
         CROSS JOIN jsonb_array_elements(dj.data->'friends') AS friend
WHERE friend->>'name' ILIKE '%ad%';

-- ##### JSONB INDEXING TESTING #####

EXPLAIN ANALYZE SELECT DISTINCT
    s.study_data-> 'id' as studyId,
    s.study_data->'seriesTitle' as seriesTitle,
    s.study_data->'universeLabel' as universeLabel,
    s.study_data->'title' as title,
    s.study_data->'purpose' as purpose,
    s.study_data->'referenceArea' as referenceArea,
    s.study_data->'timeCoverage' as timeCoverage
FROM concept c
         JOIN variable v USING (variable_id)
         JOIN logical_record lr USING (logical_record_id)
         JOIN data_file df USING (data_file_id)
         JOIN keyword kw USING (data_file_id)
         JOIN subject sb USING (data_file_id)
         JOIN study s USING (study_id)
WHERE
      s.study_data->>'seriesTitle' ILIKE :search
   or s.study_data->>'universeLabel' ILIKE :search
   or s.study_data->>'title' ILIKE :search
   or s.study_data->>'summary' ILIKE :search
   or s.study_data->>'purpose' ILIKE :search
   or s.study_data->>'studyCode' ILIKE :search
   or s.study_data->>'sectorCoverage' ILIKE :search
   or s.study_data->>'otherDissemination' ILIKE :search
   or s.study_data->>'documentationOnMethodology' ILIKE :search
   or s.study_data->>'geographicalComparability' ILIKE :search
   or s.study_data->>'comparabilityOverTime' ILIKE :search
   or s.study_data->>'sourceData' ILIKE :search
   or s.study_data->>'frequencyOfDataCollection' ILIKE :search
   or s.study_data->>'dataCollection' ILIKE :search
   or s.study_data->>'dataValidation' ILIKE :search
   or s.study_data->>'dataCompilation' ILIKE :search
   or df.data_file_data->>'title' ILIKE :search
   or kw.keyword_data->>'name' ILIKE :search
   or sb.subject_data->>'name' ILIKE :search
   or lr.logical_record_data->>'name' ILIKE :search
   or lr.logical_record_data->>'label' ILIKE :search
   or lr.logical_record_data->>'description' ILIKE :search
   or v.variable_data->>'name' ILIKE :search
   or v.variable_data->>'label' ILIKE :search
   or v.variable_data->>'description' ILIKE :search
   or v.variable_data->>'representedVariableLabel' ILIKE :search
   or v.variable_data->>'conceptualVariableLabel' ILIKE :search
   or c.concept_data->>'label' ILIKE :search;

EXPLAIN ANALYZE SELECT * FROM concept WHERE concept_data->>'label' ILIKE :search;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX trigram_index_title ON domain_json USING GIN ((data->>'title') gin_trgm_ops);

CREATE INDEX studies_series_title_idx ON study USING GIN ((study_data->>'seriesTitle') gin_trgm_ops);
CREATE INDEX studies_universe_label_idx ON study USING GIN ((study_data->>'universeLabel') gin_trgm_ops);
CREATE INDEX studies_title_idx ON study USING GIN ((study_data->>'title') gin_trgm_ops);
CREATE INDEX studies_summary_idx ON study USING GIN ((study_data->>'summary') gin_trgm_ops);
CREATE INDEX studies_purpose_idx ON study USING GIN ((study_data->>'purpose') gin_trgm_ops);
CREATE INDEX studies_study_code_idx ON study USING GIN ((study_data->>'studyCode') gin_trgm_ops);
CREATE INDEX studies_sector_coverage_idx ON study USING GIN ((study_data->>'sectorCoverage') gin_trgm_ops);
CREATE INDEX studies_other_dissemination_idx ON study USING GIN ((study_data->>'otherDissemination') gin_trgm_ops);
CREATE INDEX studies_documentation_on_methodology_idx ON study USING GIN ((study_data->>'documentationOnMethodology') gin_trgm_ops);
CREATE INDEX studies_geographical_comparability_idx ON study USING GIN ((study_data->>'geographicalComparability') gin_trgm_ops);
CREATE INDEX studies_comparability_over_time_idx ON study USING GIN ((study_data->>'comparabilityOverTime') gin_trgm_ops);
CREATE INDEX studies_source_data_idx ON study USING GIN ((study_data->>'sourceData') gin_trgm_ops);
CREATE INDEX studies_frequency_of_data_collection_idx ON study USING GIN ((study_data->>'frequencyOfDataCollection') gin_trgm_ops);
CREATE INDEX studies_data_collection_idx ON study USING GIN ((study_data->>'dataCollection') gin_trgm_ops);
CREATE INDEX studies_data_validation_idx ON study USING GIN ((study_data->>'dataValidation') gin_trgm_ops);
CREATE INDEX studies_data_compilation_idx ON study USING GIN ((study_data->>'dataCompilation') gin_trgm_ops);
CREATE INDEX data_files_title_idx ON data_file USING GIN ((data_file_data->>'title') gin_trgm_ops);
CREATE INDEX keyword_name_idx ON keyword USING GIN ((keyword_data->>'name') gin_trgm_ops);
CREATE INDEX subject_name_idx ON subject USING GIN ((subject_data->>'name') gin_trgm_ops);
CREATE INDEX logical_record_name_idx ON logical_record USING GIN ((logical_record_data->>'name') gin_trgm_ops);
CREATE INDEX logical_record_label_idx ON logical_record USING GIN ((logical_record_data->>'label') gin_trgm_ops);
CREATE INDEX logical_record_description_idx ON logical_record USING GIN ((logical_record_data->>'description') gin_trgm_ops);
CREATE INDEX variable_name_idx ON variable USING GIN ((variable_data->>'name') gin_trgm_ops);
CREATE INDEX variable_label_idx ON variable USING GIN ((variable_data->>'label') gin_trgm_ops);
CREATE INDEX variable_description_idx ON variable USING GIN ((variable_data->>'description') gin_trgm_ops);
CREATE INDEX variable_represented_variable_label_idx ON variable USING GIN ((variable_data->>'representedVariableLabel') gin_trgm_ops);
CREATE INDEX variable_conceptual_variable_label_idx ON variable USING GIN ((variable_data->>'conceptualVariableLabel') gin_trgm_ops);
CREATE INDEX concept_label_idx ON concept USING GIN ((concept_data->>'label') gin_trgm_ops);

CREATE INDEX studies_series_title_idx ON study USING GIN (study_data);
CREATE INDEX data_files_title_idx ON data_file USING GIN (data_file_data);
CREATE INDEX keyword_name_idx ON keyword USING GIN (keyword_data);
CREATE INDEX subject_name_idx ON subject USING GIN (subject_data);
CREATE INDEX logical_record_name_idx ON logical_record USING GIN (logical_record_data);
CREATE INDEX variable_name_idx ON variable USING GIN (variable_data);
CREATE INDEX concept_label_idx ON concept USING GIN (concept_data);


DROP INDEX studies_series_title_idx;
DROP INDEX studies_universe_label_idx;
DROP INDEX studies_title_idx;
DROP INDEX studies_summary_idx;
DROP INDEX studies_purpose_idx;
DROP INDEX studies_study_code_idx;
DROP INDEX studies_sector_coverage_idx;
DROP INDEX studies_other_dissemination_idx;
DROP INDEX studies_documentation_on_methodology_idx;
DROP INDEX studies_geographical_comparability_idx;
DROP INDEX studies_comparability_over_time_idx;
DROP INDEX studies_source_data_idx;
DROP INDEX studies_frequency_of_data_collection_idx;
DROP INDEX studies_data_collection_idx;
DROP INDEX studies_data_validation_idx;
DROP INDEX studies_data_compilation_idx;
DROP INDEX data_files_title_idx;
DROP INDEX keyword_name_idx;
DROP INDEX subject_name_idx;
DROP INDEX logical_record_name_idx;
DROP INDEX logical_record_label_idx;
DROP INDEX logical_record_description_idx;
DROP INDEX variable_name_idx;
DROP INDEX variable_label_idx;
DROP INDEX variable_description_idx;
DROP INDEX variable_represented_variable_label_idx;
DROP INDEX variable_conceptual_variable_label_idx;
DROP INDEX concept_label_idx;
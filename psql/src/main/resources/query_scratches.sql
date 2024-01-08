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

EXPLAIN ANALYZE
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
ORDER BY study_rank DESC
OFFSET 0
FETCH NEXT 10 ROWS ONLY;

explain analyze
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

select count(metadata_domain_id) from metadata_domain;
select count(subdomain_id) from subdomain;
select count(series_id) from series;
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

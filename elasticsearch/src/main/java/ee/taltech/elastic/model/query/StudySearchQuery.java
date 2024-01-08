package ee.taltech.elastic.model.query;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Getter
public class StudySearchQuery extends CommonSearchQuery {

    private static final String[] INCLUDE_SOURCE_FILTERS = new String[]{
            "title",
            "referenceArea",
            "timeCoverage",
            "universeLabel",
            "seriesTitle",
            "purpose",
            "dataFiles.logicalRecords.id",
            "dataFiles.logicalRecords.variables.id"
    };

    private static final String SEARCH_QUERY_WITH_SEARCH_VALUE_STRING = """
            {
              "bool": {
                "should": [
                  {
                    "multi_match": {
                      "query": ":searchValue",
                      "fields": [
                        "seriesTitle",
                        "universeLabel",
                        "title",
                        "summary",
                        "purpose",
                        "studyCode",
                        "sectorCoverage",
                        "otherDissemination",
                        "documentationOnMethodology",
                        "geographicalComparability",
                        "comparabilityOverTime",
                        "sourceData",
                        "frequencyOfDataCollection",
                        "dataCollection",
                        "dataValidation",
                        "dataCompilation"
                      ]
                    }
                  },
                  {
                    "nested": {
                      "path": "dataFiles",
                      "query": {
                        "match": {
                          "dataFiles.title": {
                            "query": ":searchValue"
                          }
                        }
                      }
                    }
                  },
                  {
                    "nested": {
                      "path": "dataFiles.keywords",
                      "query": {
                        "match": {
                          "dataFiles.keywords.name": {
                            "query": ":searchValue"
                          }
                        }
                      }
                    }
                  },
                  {
                    "nested": {
                      "path": "dataFiles.subjects",
                      "query": {
                        "match": {
                          "dataFiles.subjects.name": {
                            "query": ":searchValue"
                          }
                        }
                      }
                    }
                  },
                  {
                    "nested": {
                      "path": "dataFiles.logicalRecords",
                      "query": {
                        "multi_match": {
                          "query": ":searchValue",
                          "fields": [
                            "dataFiles.logicalRecords.name",
                            "dataFiles.logicalRecords.label",
                            "dataFiles.logicalRecords.description"
                          ]
                        }
                      }
                    }
                  },
                  {
                    "nested": {
                      "path": "dataFiles.logicalRecords.variables",
                      "query": {
                        "multi_match": {
                          "query": ":searchValue",
                          "fields": [
                            "dataFiles.logicalRecords.variables.name",
                            "dataFiles.logicalRecords.variables.label",
                            "dataFiles.logicalRecords.variables.description",
                            "dataFiles.logicalRecords.variables.representedVariableLabel",
                            "dataFiles.logicalRecords.variables.conceptualVariableLabel"
                          ]
                        }
                      },
                      "inner_hits": {
                        "size": 1000,
                        "highlight": {
                          "number_of_fragments": 0,
                          "fields": {
                            "dataFiles.logicalRecords.variables.label": {}
                          }
                        }
                      }
                    }
                  },
                  {
                    "nested": {
                      "path": "dataFiles.logicalRecords.variables.concepts",
                      "query": {
                        "match": {
                          "dataFiles.logicalRecords.variables.concepts.label": {
                            "query": ":searchValue"
                          }
                        }
                      }
                    }
                  }
                ]
              }
            }
            """;

    private String builtQueryString;
    private Query query;

    public StudySearchQuery(SearchQueryParams params, Pageable pageable) {
        this.buildBaseQueryString(params);
        this.buildQuery(pageable);
    }

    private void buildBaseQueryString(SearchQueryParams params) {
        String searchQueryString = this.buildSearchQueryString(params.getSearchValue());
        this.builtQueryString = BASE_QUERY_STRING.replace(":searchQuery", searchQueryString);
    }

    private String buildSearchQueryString(String searchValueParam) {
        if (isNotBlank(searchValueParam)) {
            return SEARCH_QUERY_WITH_SEARCH_VALUE_STRING.replace(":searchValue", searchValueParam);
        } else {
            return SEARCH_QUERY_WITHOUT_SEARCH_VALUE_STRING;
        }
    }

    private void buildQuery(Pageable pageable) {
        log.info(this.builtQueryString);
        Query stringQuery = new StringQuery(this.builtQueryString).setPageable(pageable);
        stringQuery.addSourceFilter(new FetchSourceFilter(INCLUDE_SOURCE_FILTERS, null));
        this.query = stringQuery;
    }
}

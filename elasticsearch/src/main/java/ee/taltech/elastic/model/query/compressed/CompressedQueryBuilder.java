package ee.taltech.elastic.model.query.compressed;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CompressedQueryBuilder {

    private static final String QUERY_STRING = """
            {
              "bool": {
                "must": [
                  {
                    "bool": {
                      "should": [
                        {
                          "multi_match": {
                            "query": ":searchValue",
                            "fields": [
                              "studySearchDocument"
                            ]
                          }
                        },
                        {
                          "nested": {
                            "path": "variables",
                            "query": {
                              "multi_match": {
                                "query": ":searchValue",
                                "fields": [
                                  "variables.name",
                                  "variables.label",
                                  "variables.description",
                                  "variables.representedVariableLabel",
                                  "variables.conceptualVariableLabel"
                                ]
                              }
                            },
                            "inner_hits": {
                              "size": 1000,
                              "highlight": {
                                "number_of_fragments": 0,
                                "fields": {
                                  "variables.label": {}
                                }
                              }
                            }
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;

//    private static final String[] INCLUDE_SOURCE_FILTERS = new String[]{
//            "title",
//            "referenceArea",
//            "timeCoverage",
//            "universeLabel",
//            "seriesTitle",
//            "purpose",
//            "dataFiles.logicalRecords.id",
//            "dataFiles.logicalRecords.variables.id"
//    };

    public static Query getQueryForCompressed(Pageable pageable, String searchValue) {
        return new StringQuery(QUERY_STRING.replace(":searchValue", searchValue)).setPageable(pageable);
    }

}

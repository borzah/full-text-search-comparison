package ee.taltech.elastic.model.query;

public class CommonSearchQuery {

    protected static final String SEARCH_QUERY_WITHOUT_SEARCH_VALUE_STRING = """
            {
              "bool": {
                "should": [
                  {
                    "match_all": {}
                  }
                ]
              }
            }
            """;

    protected static final String BASE_QUERY_STRING = """
            {
              "bool": {
                "must": [
                  :searchQuery
                ]
              }
            }
            """;
}

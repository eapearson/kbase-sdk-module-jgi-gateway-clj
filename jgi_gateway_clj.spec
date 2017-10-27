/*
A KBase module: jgi_gateway_clj
*/

module jgi_gateway_clj {

    /*
        a bool defined as int
    */
    typedef int bool;

    /*
        Call performance measurement
    */
    typedef structure {        
        int request_elapsed_time;
    } CallStats;

    typedef structure {
        int id;
        string message;
        UnspecifiedObject info;
    } Error;


    /* SEARCH */

    /*
        SearchFilter
        The jgi back end takes a map of either string, integer, or array of integer.
        I don't think the type compiler supports union types, so unspecified it is.
    */
    typedef mapping<string, UnspecifiedObject> SearchFilter;
    typedef mapping<string, string> SearchQuery;

    /*
        search searches the JGI service for matches against the
        query, which may be a string or an object mapping string->string

        query - 

        Other parameters
        @optional filter 
        @optional limit
        @optional page
        @optional include_private
    */
    typedef structure {
        SearchQuery query;
        SearchFilter filter;
        int limit;
        int page;
        bool include_private;
    } SearchInput;


    /*
      SearchQueryResult holds the actual json returned by the 
      backend search api.

      source - the indexed document, a json structure of various shapes
                dependent upon the data type and source; entire metadata jamo record
      index - Name of Elasticsearch index searched on
      score - Elasticsearch search result score
      id    - Unique record id, which is used to fetch files
    */

    /*
        SearchDocument
        The source document for the search; it is both the data obtained by the
        search as well as the source of the index. 
        It is the entire metadata JAMO record.
    */
    typedef UnspecifiedObject SearchDocument;

    /*
        SearchResult
        Represents a single search result item
    */
    typedef structure {
        SearchDocument source;
        string index;
        string score;
        string id;
    } SearchResultItem;

    /*
        SearchQueryResult
        The top level search object returned from the query.
        Note that this structure closely parallels that returned by the jgi search service.
        The only functional difference is that some field names which were prefixed by 
        underscore are known by their unprefixed selfs.
        hits  - a list of the actual search result documents and statsitics returned;;
                note that this represents the window of search results defined by
                the limit input property.
        total - the total number of items matched by the search; not the same as the 
               items actually returned;
    */
    typedef structure {
        list<SearchResultItem> hits; 
        int total;   
    } SearchQueryResult;


    typedef structure {
       SearchQueryResult search_result;
    } SearchResult;


    /*
        The search function takes a search structure and returns a list of
        documents.
    */
    funcdef search(SearchInput input) 
            returns (SearchResult result, CallStats stats) 
            authentication required;

    /* STAGE */

    typedef structure {
       list<string> ids;
    } StageInput;

    /*
        StagingResult returns a map entry for each id submitted in the stage request.
        The map key is the _id property returned in a SearchResult item (not described here but probably 
        should be), the value is a string describing the result of the staging request.
        At time of writing, the value is always "staging" since the request to the jgi gateway jgi service
        and the call to stage in the jgi gateway kbase service are in different processes.
    */
    
    typedef structure {
        string job_id;
    } StagingResult;

    funcdef stage(StageInput input) 
            returns (StagingResult result, CallStats stats) 
            authentication required;

   typedef structure {
       string job_id;
   } StagingStatusInput;

    typedef structure {
        string message;
    } StagingStatusResult;

    /* Fetch the current status of the given staging fetch request as 
       identified by its job id */
    funcdef stage_status(StagingStatusInput input) 
            returns (StagingStatusResult result, CallStats stats) 
            authentication required;

};

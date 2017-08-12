/*
A KBase module: jgi_gateway_eap
*/

module jgi_gateway_clj {

    /*
        a bool defined as int
    */
    typedef int bool;

    /*
        search_jgi searches the JGI service for matches against the
        search_string

        Other parameters
        @optional limit
        @optional page
    */
    typedef structure {
        string search_string;
        int limit;
        int page;
    } SearchInput;


    /*
      SearchQueryResult holds the actual json returned by the 
      backend search api.
      TODO: the data returned is not really a string,string map, although
      python doesn't care.
    */
    /* typedef mapping<string, string> docdata; */
    typedef list<UnspecifiedObject> SearchQueryResult;

    typedef structure {
       SearchQueryResult search_result;
       int search_elapsed_time;
    } SearchResults;


    /*
        The search_jgi function takes a search string and returns a list of
        documents.
    */
    funcdef search_jgi(SearchInput input) returns (SearchResults output) authentication required;

    typedef structure {
       list<string> ids;
    } StageInput;

    /*
        StagingResults returns a map entry for each id submitted in the stage_objects request.
        The map key is the _id property returned in a SearchResults item (not described here but probably 
        should be), the value is a string describing the result of the staging request.
        At time of writing, the value is always "staging" since the request to the jgi gateway jgi service
        and the call to stage_objects in the jgi gateway kbase service are in different processes.
    */
    typedef mapping<string, string> StagingResults;

    funcdef stage_objects(StageInput input) returns (StagingResults results) authentication required;

    typedef structure {
        string config; 
    } DebugResults;

    funcdef debug() returns (DebugResults results) authentication required;
};

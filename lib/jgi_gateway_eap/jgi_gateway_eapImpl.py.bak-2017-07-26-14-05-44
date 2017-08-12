# -*- coding: utf-8 -*-
#BEGIN_HEADER
"""
JGI Gateway Service
"""

import os
import requests
import json
import sys
#END_HEADER


class jgi_gateway_eap:
    '''
    Module Name:
    jgi_gateway_eap

    Module Description:
    A KBase module: jgi_gateway
    '''

    ######## WARNING FOR GEVENT USERS ####### noqa
    # Since asynchronous IO can lead to methods - even the same method -
    # interrupting each other, you must be *very* careful when using global
    # state. A method could easily clobber the state set by another while
    # the latter method is running.
    ######################################### noqa
    VERSION = "0.0.1"
    GIT_URL = "git@github.com:eapearson/jgi_gateway.git"
    GIT_COMMIT_HASH = "01bb1215d8afd8bc92862cafb38e64f4485321ec"

    #BEGIN_CLASS_HEADER
    #END_CLASS_HEADER

    # config contains contents of config file in a hash or None if it couldn't
    # be found
    def __init__(self, config):
        #BEGIN_CONSTRUCTOR
        """
        default constructor
        """

        self.user = None
        self.passwd = None
        if 'jgi-token' in config:
            (user, passwd) = config['jgi-token'].split(':')
            self.user = user
            self.passwd = passwd
        self.jgi_host = 'https://jgi-kbase.nersc.gov'
        if 'jgi-host' in config:
            self.jgi_host = config['jgi-host']
        print "Using %s for queries" % (self.jgi_host)
        #END_CONSTRUCTOR
        pass


    def search_jgi(self, ctx, input):
        """
        The search_jgi function takes a search string and returns a list of
        documents.
        :param input: instance of type "SearchInput" (search_jgi searches the
           JGI service for matches against the search_string Other parameters
           @optional limit @optional page) -> structure: parameter
           "search_string" of String, parameter "limit" of Long, parameter
           "page" of Long
        :returns: instance of type "SearchResults" -> structure: parameter
           "doc_data" of list of type "docdata" -> mapping from String to
           String
        """
        # ctx is the context object
        # return variables are: output
        #BEGIN search_jgi
        header = {'Content-Type': 'application/json'}
        if 'search_string' not in input:
            raise(ValueError("missing required parameter search_string"))
        query = {"query": input['search_string']}
        if 'limit' in input:
            query['size'] = input['limit']
        if 'page' in input:
            query['page'] = input['page']
        queryjson = json.dumps(query)
        ret = requests.post(self.jgi_host + '/query', data=queryjson,
                            auth=(self.user, self.passwd),
                            headers=header)
        if ret.status_code == 200:
            output = ret.json()
        else:
            raise ValueError('Bad Response from JGI search service (%d)' %
                             ret.status_code)
        #END search_jgi

        # At some point might do deeper type checking...
        if not isinstance(output, dict):
            raise ValueError('Method search_jgi return value ' +
                             'output is not type dict as required.')
        # return the results
        return [output]

    def stage_objects(self, ctx, input):
        """
        :param input: instance of type "StageInput" -> structure: parameter
           "ids" of list of String
        :returns: instance of type "StagingResults" -> mapping from String to
           String
        """
        # ctx is the context object
        # return variables are: results
        #BEGIN stage_objects
        results = dict()
        header = {'Content-Type': 'application/json'}
        request = {"ids": ','.join(input['ids']),
                   "path": "/data/%s" % (ctx['user_id'])}
        requestjson = json.dumps(request)
        print "Debug: " + requestjson
        pid = os.fork()
        if pid == 0:
            ret = requests.post(self.jgi_host + '/fetch', data=requestjson,
                                auth=(self.user, self.passwd),
                                headers=header)
            ret.raise_for_status()
            print ret.status_code
            sys.exit(0)
            # TODO Add some logging
        for id in input:
            results[id] = "STAGED"
        #END stage_objects

        # At some point might do deeper type checking...
        if not isinstance(results, dict):
            raise ValueError('Method stage_objects return value ' +
                             'results is not type dict as required.')
        # return the results
        return [results]
    def status(self, ctx):
        #BEGIN_STATUS
        returnVal = {'state': "OK",
                     'message': "",
                     'version': self.VERSION,
                     'git_url': self.GIT_URL,
                     'git_commit_hash': self.GIT_COMMIT_HASH}
        #END_STATUS
        return [returnVal]

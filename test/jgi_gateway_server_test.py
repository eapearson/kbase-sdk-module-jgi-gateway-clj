# -*- coding: utf-8 -*-
import unittest
import os  # noqa: F401
import json  # noqa: F401
import time
import requests

from os import environ
try:
    from ConfigParser import ConfigParser  # py2
except:
    from configparser import ConfigParser  # py3

from pprint import pprint  # noqa: F401

from biokbase.workspace.client import Workspace as workspaceService
from jgi_gateway_eap.jgi_gateway_eapImpl import jgi_gateway_eap
from jgi_gateway_eap.jgi_gateway_eapServer import MethodContext


class jgi_gatewayTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        token = environ.get('KB_AUTH_TOKEN', None)
        user_id = requests.post(
            'https://kbase.us/services/authorization/Sessions/Login',
            data='token={}&fields=user_id'.format(token)).json()['user_id']
        # WARNING: don't call any logging methods on the context object,
        # it'll result in a NoneType error
        cls.ctx = MethodContext(None)
        cls.ctx.update({'token': token,
                        'user_id': user_id,
                        'provenance': [
                            {'service': 'jgi_gateway_eap',
                             'method': 'please_never_use_it_in_production',
                             'method_params': []
                             }],
                        'authenticated': 1})
        config_file = environ.get('KB_DEPLOYMENT_CONFIG', None)
        cls.cfg = {}
        config = ConfigParser()
        config.read(config_file)
        for nameval in config.items('jgi_gateway_eap'):
            cls.cfg[nameval[0]] = nameval[1]
        cls.wsURL = cls.cfg['workspace-url']
        cls.wsClient = workspaceService(cls.wsURL)
        cls.serviceImpl = jgi_gateway_eap(cls.cfg)
        cls.scratch = cls.cfg['scratch']
        cls.callback_url = os.environ['SDK_CALLBACK_URL']

    @classmethod
    def tearDownClass(cls):
        if hasattr(cls, 'wsName'):
            cls.wsClient.delete_workspace({'workspace': cls.wsName})
            print('Test workspace was deleted')

    def getWsClient(self):
        return self.__class__.wsClient

    def getWsName(self):
        if hasattr(self.__class__, 'wsName'):
            return self.__class__.wsName
        suffix = int(time.time() * 1000)
        wsName = "test_jgi_gateway_eap_" + str(suffix)
        ret = self.getWsClient().create_workspace({'workspace': wsName})  # noqa
        self.__class__.wsName = wsName
        return wsName

    def getImpl(self):
        return self.__class__.serviceImpl

    def getContext(self):
        return self.__class__.ctx

    # NOTE: According to Python unittest naming rules test method names should start from 'test'. # noqa
    def test_search(self):
        query = {"search_string": "coli"}
        ret = self.getImpl().search_jgi(self.getContext(), query)[0]
        self.assertIsNotNone(ret)
        self.assertIn('hits', ret)
        self.assertEquals(len(ret['hits']), 10)
        query['limit'] = 20
        ret = self.getImpl().search_jgi(self.getContext(), query)[0]
        self.assertIsNotNone(ret)
        self.assertIn('hits', ret)
        self.assertEquals(len(ret['hits']), 20)
        query['page'] = 2
        ret = self.getImpl().search_jgi(self.getContext(), query)[0]
        self.assertIsNotNone(ret)
        self.assertIn('hits', ret)
        self.assertEquals(len(ret['hits']), 20)

    def test_staging(self):
        req = {'ids': ['51d4fa27067c014cd6ed1a90', '51d4fa27067c014cd6ed1a96']}
        ret = self.getImpl().stage_objects(self.getContext(), req)[0]
        self.assertIsNotNone(ret)

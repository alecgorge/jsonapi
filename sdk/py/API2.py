#!/usr/bin/env python2
# -*- encoding: utf-8 -*-

""" Bukkit JSONAPI v2 for Python

Python implementation utilizing the second version of the API.
Needs 'requests' library installed (pip install requests)

Usage:

>>> import API2
>>> conn = API2.Connection(<parameters>)
>>> api = API2.JSONAPI(conn)
Then you can use the calls like described in <http://mcjsonapi.com/apidocs/>
like this:

Let's say you want to ban player "abc" for a reason "griefing"
API function name is players.name.ban with 2 parameters.
In this API, you will then call:
>>> api.players.name.ban("abc", "griefing") # api is the JSONAPI object

Do not try to dir() on the JSONAPI object as it does not store any info
about the methods. The API's method name is derived dynamically from
what you've written after `api.`

Feel free to post any suggestions or improvements.

@author: Milan Falešník <milan@falesnik.net>
"""

import hashlib
import json
import urllib
import requests
import re


class JSONAPIException(Exception):
    pass


class PageNotFoundException(JSONAPIException):
    pass


class InvalidJSONException(JSONAPIException):
    pass


class ServerOfflineException(JSONAPIException):
    pass


class APIErrorException(JSONAPIException):
    pass


class InvocationTargetException(JSONAPIException):
    pass


class OtherCaughtException(JSONAPIException):
    pass


class MethodNotExistException(JSONAPIException):
    pass


class WrongAPIKeyException(JSONAPIException):
    pass


class NotAllowedAPIKeyException(JSONAPIException):
    pass


class MissingUsernameException(JSONAPIException):
    pass


exception_mapping = {
    1: PageNotFoundException,
    2: InvalidJSONException,
    3: ServerOfflineException,
    4: APIErrorException,
    5: InvocationTargetException,
    6: OtherCaughtException,
    7: MethodNotExistException,
    8: WrongAPIKeyException,
    9: NotAllowedAPIKeyException,
    10: MissingUsernameException
}


class Response(object):
    arg_regexp = re.compile(
        r"Incorrect number of args: gave ([0-9]+) \([^)]*\), expected ([0-9]+)"
    )

    def __init__(self, data):
        self.is_success = data["is_success"]
        self.source = data["source"]
        self.tag = data.get("tag", None)
        self.result = data[data["result"]]
        self.return_code = 0 if self.is_success else self.result["code"]
        self.raise_exception_if_failed()

    def raise_exception_if_failed(self):
        if self.return_code > 0:
            if self.return_code == 6:
                match = self.arg_regexp.search(self.result["message"])
                if match:
                    given, expected = [int(x) for x in match.groups()]
                    # Mimic the pythonic one.
                    raise TypeError("%s() takes exactly %d arguments (%d given)" %
                                    (self.source, expected, given))
                else:
                    raise exception_mapping[self.return_code](self.result["message"])
            else:
                raise exception_mapping[self.return_code](self.result["message"])

    def __nonzero__(self):
        return self.is_success is True


class Connection(object):
    url = "http://{host}:{port}/api/2/call?json={json}"

    def _get(self, payload):
        """ Push the JSON on the server and get the responding object

        @param payload: Dict containing the payload data for API
        """
        return requests.get(self.url.format(
            host=self.host,
            port=self.port,
            json=urllib.quote(json.dumps(payload))
        )).json()

    def _get_key(self, method_name):
        """ Calculates the security key for JSONAPI call

        @param method_name: Method to calculate the key for
        """
        sha = hashlib.sha256()
        sha.update(self.username)
        sha.update(method_name)
        sha.update(self.password)
        return sha.hexdigest()

    def _call_method(self, method_name, *args):
        """ Create the payload for desired method and params
            and query the API.

        @param method_name: Method to call
        @param args: Arguments for the called methods
        """
        return self._get([
            dict(
                name=method_name,
                username=self.username,
                key=self._get_key(method_name),
                arguments=args
            )
        ])[0]

    def call(self, method_name, *args, **kwargs):
        """ Call the method.

        @param method_name: Method to call
        @param args: Arguments for the called methods
        @keyword keep_response: If True, the whole Response object is returned
            rather than just the resulting data. Default False.
        """
        response = Response(self._call_method(method_name, *args))
        if kwargs.get("keep_response", False):
            return response
        else:
            return response.result

    def __init__(self,
                 host="localhost",
                 port=20059,
                 username="admin",
                 password="changeme"):
        self.host = host
        self.port = int(port)
        self.username = username
        self.password = password


class CallChain(object):
    def __init__(self, node_name, parent_object):
        self.node = node_name
        self.parent = parent_object

    def __getattribute__(self, name):
        try:
            return super(CallChain, self).__getattribute__(name)
        except AttributeError as e:
            try:
                return self.__class__(name, self)
            except AttributeError:
                raise e

    def __call__(self, *args, **kwargs):
        parent = self.parent
        stack = [self.node]
        while not isinstance(parent, Connection):
            stack.append(parent.node)
            parent = parent.parent
        stack.reverse()
        return parent.call(".".join(stack), *args, **kwargs)


class JSONAPI(object):
    def __init__(self, connection):
        self.connection = connection

    @property
    def is_online(self):
        """ Check whether the server is online.

        """
        try:
            self.server()
            return True
        except (requests.exceptions.ConnectionError, JSONAPIException):
            return False

    def __getattribute__(self, name):
        try:
            return super(JSONAPI, self).__getattribute__(name)
        except AttributeError as e:
            try:
                return CallChain(name, self.connection)
            except AttributeError:
                raise e

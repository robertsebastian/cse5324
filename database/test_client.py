#!/usr/bin/python

import unittest
import httplib
import json
import os

def request(text):
   conn = httplib.HTTPConnection('localhost:8000')
   conn.request('POST', '/tutor_find_db', json.dumps(text), {
      'Content-Type': 'application/json',
      'Accept':       'application/json',
   })
   response = conn.getresponse().read()
   conn.close()

   try:
      return json.loads(response)
   except ValueError:
      print response
      raise

class DatabaseTest(unittest.TestCase):
   def setUp(self):
      if os.access('tutor.db', os.R_OK):
         os.remove('tutor.db')

class LoginTests(DatabaseTest):
   def setUp(self):
      super(DatabaseTest, self).setUp()
      request({'action': 'register', 'email': 'alice@example.org', 'password': 'password'})

   def test_login_invalid_user(self):
      r = request({'action': 'login', 'email': 'danny@example.org', 'password': 'password'})
      self.assertEqual(r['success'], False)
      self.assertEqual(r['error'], 'invalid_login')

   def test_login_invalid_password(self):
      r = request({'action': 'login', 'email': 'alice@example.org', 'password': 'password1'})
      self.assertEqual(r['success'], False)
      self.assertEqual(r['error'], 'invalid_login')
      
   def test_login_valid(self):
      r = request({'action': 'login', 'email': 'alice@example.org', 'password': 'password'})
      self.assertEqual(r['success'], True)
      self.assertIn('session_id', r)

class RegistrationTests(DatabaseTest):
   pass

class UpdateTests(DatabaseTest):
   def test_update(self):
      r = request({'action': 'register', 'email': 'alice@example.org', 'password': 'password'})
      r = request({'action': 'get_user', 'session_id': r['session_id'], 'user_id': r['user_id']})
      self.assertEqual(r['success'], True)
      self.assertEqual(r['account_email'], 'alice@example.org')

suite = unittest.TestLoader().loadTestsFromTestCase(LoginTests)

#print 'Registering Alice'
#print request({'action': 'register', 'email': 'alice@example.org', 'password': 'password'})
#print 'Registering Alice again'
#print request({'action': 'register', 'email': 'alice@example.org', 'password': 'password'})
#print 'Registering Bob'
#print request({'action': 'register', 'email': 'bob@example.org',   'password': 'password'})
#print 'Registering Cindy'
#print request({'action': 'register', 'email': 'cindy@example.org', 'password': 'password'})
#
#print 'Updating user information for Alice'
#print request({
#      'action':       'update_user',
#      'name':         'Alice Example',
#      'user_id':      '1000',
#      'phone':        '1234567890',
#      'tutor_flag':   1,
#      'subject_tags': 'math,calculus,math2310',
#      'loc_lat':      50.0001,
#      'loc_lon':      50.0001,
#      'session_id':   login_info['session_id']})
#print request({'action': 'search', 'email': 'cindy@example.org', 'password': 'password'})

#!/usr/bin/python

import unittest
import httplib
import json
import os

################################################################################

def request(text):
   """Make a request to the database's HTTP server"""

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

################################################################################
class DatabaseTest(unittest.TestCase):
   """Base test class to clean up existing database"""

   def setUp(self):
      self.cleanDatabase()

   def tearDown(self):
      self.cleanDatabase()

   def cleanDatabase(self):
      if os.access('tutor.db', os.R_OK):
         os.remove('tutor.db')

class DatabaseTestWithUsers(DatabaseTest):
   """Base test class including some predefined users"""

   def setUp(self):
      super(DatabaseTest, self).setUp()
      self.alice = request({'action': 'register', 'email': 'alice@example.org', 'password': 'password'})
      self.bob   = request({'action': 'register', 'email': 'bob@example.org', 'password': 'password'})
      self.cindy = request({'action': 'register', 'email': 'cindy@example.org', 'password': 'password'})

################################################################################
class LoginTests(DatabaseTestWithUsers):
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
      self.assertIn('user_id', r)

class RegistrationTests(DatabaseTest):
   def test_register_user(self):
      r = request({'action': 'register', 'email': 'alice@example.org', 'password': 'password'})
      self.assertEqual(r['success'], True)
      self.assertIn('session_id', r)
      self.assertIn('user_id', r)

   def test_user_already_exists(self):
      request({'action': 'register', 'email': 'bob@example.org', 'password': 'password'})
      r = request({'action': 'register', 'email': 'bob@example.org', 'password': 'password'})
      self.assertEqual(r['success'], False)
      self.assertNotIn('session_id', r)
      self.assertIn('error', r)
      self.assertEqual(r['error'], 'duplicate_user')

   def test_invalid_password(self):
      r = request({'action': 'register', 'email': 'cindy@example.org', 'password': ''})
      self.assertEqual(r['success'], False)
      self.assertNotIn('session_id', r)
      self.assertIn('error', r)
      self.assertEqual(r['error'], 'invalid_password')

class ReviewTests(DatabaseTestWithUsers):
   def review(self, user, subject, score, text):
      return {
         'action':     'submit_review',
         'session_id': user['session_id'],
         'subject_id': subject['user_id'],
         'score':      score,
         'text':       text}

   def test_submit_review(self):
      req = self.review(self.alice, self.bob, 5.0, 'good')
      r = request(req)
      self.assertEqual(r['success'], True)
      self.assertIn('review_id', r)
      self.assertAlmostEqual(r['score'], 5.0, places=1)
      self.assertEqual(r['text'], 'good')

   def test_submit_review_invalid_subject(self):
      r = request(self.review(self.alice, {'user_id': 1000}, 5.0, 'good'))
      self.assertEqual(r['success'], False)
      self.assertEqual(r['error'], 'invalid_subject')

   def test_submit_review_invalid_score(self):
      r = request(self.review(self.alice, self.bob, 1000.0, 'good'))
      self.assertEqual(r['success'], False)
      self.assertEqual(r['error'], 'invalid_score')

   def test_update_review_get_review(self):
      # Request twice to verify updated rather than duplicated
      request(self.review(self.bob, self.alice, 5.0, 'good'))
      request(self.review(self.bob, self.alice, 4.0, 'decent'))

      r = request({'action': 'get_reviews', 'session_id': self.bob['session_id'], 'subject_id': self.alice['user_id']})
      self.assertEqual(r['success'], True)
      self.assertEqual(len(r['reviews']), 1)
      self.assertEqual(r['reviews'][0]['text'], 'decent')
      self.assertAlmostEqual(r['reviews'][0]['score'], 4.0, places=1)

   def test_tutor_score(self):
      request(self.review(self.alice, self.cindy, 5.0, 'great'))
      request(self.review(self.bob, self.cindy, 0.0, 'awful'))

      r = request({'action': 'get_user', 'session_id': self.bob['session_id'], 'user_id': self.cindy['user_id']})
      self.assertEqual(r['success'], True)
      self.assertAlmostEqual(r['score'], 5.0 / 2.0, places=1)

class UpdateTests(DatabaseTestWithUsers):
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
   def test_update(self):
      r = request({'action': 'get_user', 'session_id': self.alice['session_id'], 'user_id': self.alice['user_id']})
      self.assertEqual(r['success'], True)
      self.assertEqual(r['account_email'], 'alice@example.org')

if __name__ == '__main__':
   unittest.main()

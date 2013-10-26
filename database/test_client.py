#!/usr/bin/python

import unittest
import httplib
import json
import os
import imp
import sys
import tutor_find_db

direct_requests = False

################################################################################
def request(text):
   """Make a request to the database's HTTP server"""

   response = None
   if direct_requests:
      tutor_find_db.open_db()
      response = tutor_find_db.handle_request(json.dumps(text))
      tutor_find_db.close_db()

   else:
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

   def get_user(self, user, subject):
      return request({'action': 'get_user', 'session_id': user['session_id'], 'user_id': subject['user_id']})

   def setUp(self):
      super(DatabaseTestWithUsers, self).setUp()
      self.alice = request({'action': 'register', 'email': 'alice@example.org', 'password': 'password'})
      self.bob   = request({'action': 'register', 'email': 'bob@example.org', 'password': 'password'})
      self.cindy = request({'action': 'register', 'email': 'cindy@example.org', 'password': 'password'})

################################################################################
class LoginTests(DatabaseTestWithUsers):
   def test_login_invalid_user(self):
      r = request({'action': 'login', 'email': 'danny@example.org', 'password': 'password'})
      self.assertEqual(r['success'], False)
      self.assertEqual(r['error'], 'login_failed')

   def test_login_invalid_password(self):
      r = request({'action': 'login', 'email': 'alice@example.org', 'password': 'password1'})
      self.assertEqual(r['success'], False)
      self.assertEqual(r['error'], 'login_failed')
      
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
   def test_update(self):
      update_vals = {
         'action':       'update_user',
         'name':         'Alice Example',
         'user_id':      '1000',
         'phone':        '1234567890',
         'tutor_flag':   1,
         'subject_tags': 'math,calculus,math2310',
         'loc_lat':      50.0001,
         'loc_lon':      50.0001,
         'session_id':   self.alice['session_id']}
      r = request(update_vals)

      self.assertEqual(r['success'], True)

      # Editable fields should be set
      for val in 'name', 'phone', 'tutor_flag', 'subject_tags', 'loc_lat', 'loc_lon':
         self.assertEqual(update_vals[val], r[val])

      # Non-editable fields should not
      self.assertNotEqual(update_vals['user_id'], r['user_id'])

class FavoriteTests(DatabaseTestWithUsers):
   def favorite(self, user, subject, status):
      return request({
         'action':     'set_favorite',
         'session_id': user['session_id'],
         'subject_id': subject['user_id'],
         'favorited':  status})

   def test_favorite(self):
      self.favorite(self.alice, self.bob, False)
      r = self.favorite(self.alice, self.bob, True)
      self.assertEqual(r['success'], True)

      u = self.get_user(self.alice, self.bob)
      self.assertEqual(u['favorited'], True)

   def test_get_favorites(self):
      self.favorite(self.alice, self.bob, True)
      self.favorite(self.alice, self.cindy, True)
      
      r = request({'action': 'get_favorites', 'session_id': self.alice['session_id']})
      self.assertEqual(r['success'], True)
      self.assertEqual(len(r['favorites']), 2)

class SearchTests(DatabaseTestWithUsers):
   def update_user(self, user, name, tags):
      return request({
         'action':       'update_user',
         'name':         name,
         'subject_tags': tags,
         'loc_lat':      50.0,
         'loc_lon':      50.0,
         'session_id':   user['session_id']})

   def search(self, query):
      return request({
         'action':     'search',
         'query':      query,
         'lat':        50.1,
         'lon':        50.1,
         'session_id': self.alice['session_id']})

   def setUp(self):
      super(SearchTests, self).setUp()
      self.update_user(self.alice, 'Alice Example', 'math,calculus,math2013')
      self.update_user(self.bob,   'Bob Example', 'math,physics,phys1302')
      self.update_user(self.cindy, 'Cindy Someone', 'physics,english,eng3110,phys1302')

   def test_name(self):
      r = self.search('example')
      self.assertEqual(len(r['results']), 2)

   def test_name_2(self):
      r = self.search('Bob')
      self.assertEqual(len(r['results']), 1)

   def test_subject(self):
      r = self.search('physics')
      self.assertEqual(len(r['results']), 2)

   def test_subject2(self):
      r = self.search('MATH-2013')
      self.assertEqual(len(r['results']), 1)

if __name__ == '__main__':
   #tests = unittest.TestLoader().loadTestsFromTestCase(SearchTests)
   #unittest.TextTestRunner(verbosity=2).run(tests)
   unittest.main()

#!/usr/bin/python

import unittest
import httplib
import json
import os
import imp
import sys
import random
import tutor_find_db

def request(text):
   """Make a request to the database's HTTP server"""

   tutor_find_db.open_db()
   response = tutor_find_db.handle_request(json.dumps(text))
   tutor_find_db.close_db()

   try:
      return json.loads(response)
   except ValueError:
      print response
      raise

def review(user, subject, score, text):
   return request({
      'action':     'submit_review',
      'session_id': user['session_id'],
      'subject_id': subject['user_id'],
      'score':      score,
      'text':       text})

def favorite(user, subject, status):
   return request({
      'action':     'set_favorite',
      'session_id': user['session_id'],
      'subject_id': subject['user_id'],
      'favorited':  status})

def update_user(user, name, tags):
   return request({
      'action':         'update_user',
      'name':           name,
      'subject_tags':   tags,
      'price_per_hour': random.randrange(1, 999) / 10.0,
      'loc_lat':        32.715278 + (random.randrange(0, 1000) - 500) / 10000.0,
      'loc_lon':        -97.016944 + (random.randrange(0, 1000) - 500) / 10000.0,
      'session_id':     user['session_id']})

def register(email, name):
   u = request({'action': 'register', 'email': email, 'password': 'password'})
   update_user(u, name, 'subject1,subject2')
   return u

if os.access('tutor.db', os.R_OK):
   os.remove('tutor.db')

alice = register('alice@example.org', 'Alice Example')
bob   = register('bob@example.org', 'Bob Example')
cindy = register('cindy@example.org', 'Cindy Someone')

names = ('Dan', 'Eve', 'Fran', 'Gary', 'Hanna', 'Isis', 'Jon', 'Kyle', 'Lana', 'Matt', 'Nancy', 'Ophelia', 'Peter', 'Quara', 'Robert', 'Steve')
users = []
for name in names:
   users.append(register('%s@example.org' % name.lower(), '%s Example' % name))

for user in users:
   review(alice, user, random.randrange(0, 50) / 10.0, 'I have no real opinion about this tutor')

review(alice, bob, 5.0, 'good')
review(bob, alice, 4.0, 'decent')
review(alice, cindy, 5.0, 'great')
review(bob, cindy, 0.0, 'awful')

favorite(alice, bob, True)
favorite(alice, cindy, True)

update_user(alice, 'Alice Example', 'math,calculus,math2013')
update_user(bob,   'Bob Example', 'math,physics,phys1302')
update_user(cindy, 'Cindy Someone', 'physics,english,eng3110,phys1302')

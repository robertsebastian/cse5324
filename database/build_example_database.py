#!/usr/bin/python

import unittest
import httplib
import json
import os
import imp
import sys
import random
import tutor_find_db
import base64

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
      'phone':          '555-555-5555',
      'price_per_hour': random.randrange(1, 999) / 10.0,
      'loc_address':    '701 S Nedderman Dr\nArlington, TX 76019',
      'loc_lat':        32.715278 + (random.randrange(0, 1000) - 500) / 10000.0,
      'loc_lon':        -97.016944 + (random.randrange(0, 1000) - 500) / 10000.0,
      'about_me':       (name + " ") * 20,
      'session_id':     user['session_id']})

def set_picture(user, image):
   return request({
      'action':     'set_picture',
      'session_id': user['session_id'],
      'picture':    base64.b64encode(open(image).read()),
   })

def register(email, name):
   u = request({'action': 'register', 'email': email, 'password': 'password'})
   update_user(u, name, 'Subject1, Subject-2, History, Math, English, MATH-2310, Calculus, Phsyics, Physics 1, Physics 2')
   return u

if os.access('tutor.db', os.R_OK):
   os.remove('tutor.db')

alice = register('alice@example.org', 'Alice Example')
bob   = register('bob@example.org', 'Bob Example')
cindy = register('cindy@example.org', 'Cindy Someone')

print set_picture(alice, 'sample_images/1.jpg')

names = ('Dan', 'Eve', 'Fran', 'Gary', 'Hanna', 'Isis', 'Jon', 'Kyle', 'Lana', 'Matt', 'Nancy', 'Ophelia', 'Peter', 'Quara', 'Robert', 'Steve')
users = []
for name in names:
   users.append(register('%s@example.org' % name.lower(), '%s Example' % name))

for user in users:
   review(alice, user, random.randrange(0, 50) / 10.0, 'I have no real opinion about this tutor')

   set_picture(user, 'sample_images/1.jpg')
   for i in range(1, 5):
      subject = users[random.randrange(0, len(users))]
      favorite(user, subject, True)
      review(user, subject, 3.0, 'A very average tutor. More review text. More review Text.'
         'More review Text. More review Text. More review Text. More review Text.'
         'More review Text. More review Text. More review Text. More review Text.')


review(alice, bob, 5.0, 'good')
review(bob, alice, 4.0, 'decent')
review(alice, cindy, 5.0, 'great')
review(bob, cindy, 0.0, 'awful')

favorite(alice, bob, True)
favorite(alice, cindy, True)

update_user(alice, 'Alice Example', 'Math, Calculus, MATH-2013')
update_user(bob,   'Bob Example', 'MATH, PHYSICS, PHYS1302')
update_user(cindy, 'Cindy Someone', 'physics, english, eng-3110,phys-1302')

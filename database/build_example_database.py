#!/usr/bin/python

import unittest
import httplib
import json
import os
import imp
import sys
import random
from random import randrange
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

def update_user(user, name):
   (avail_str, avail) = pick_times(randrange(2, 8))
   return request({
      'action':              'update_user',
      'name':                name,
      'subject_tags':        ', '.join([subjects[randrange(0, len(subjects))] for i in range(0, 8)]),
      'phone':               new_number(),
      'price_per_hour':      randrange(50, 300) / 10.0,
      'loc_address':         '701 S Nedderman Dr\nArlington, TX 76019',
      'loc_lat':             32.7289869 + (randrange(0, 1000) - 500) / 30000.0,
      'loc_lon':             -97.115008 + (randrange(0, 1000) - 500) / 20000.0,
      'availability_string': avail_str,
      'availability':        avail,
      'about_me':            about_me[randrange(0, len(about_me))],
      'tutor_flag':          True,
      'session_id':          user['session_id']})

def set_picture(user, image):
   return request({
      'action':     'set_picture',
      'session_id': user['session_id'],
      'picture':    base64.b64encode(open(image).read()),
   })

def set_preferred(user, preferred):
   tutor_find_db.open_db()
   tutor_find_db.set_preferred(user['user_id'], preferred)
   tutor_find_db.close_db()

def register(email, name, picture=None):
   u = request({'action': 'register', 'email': email, 'password': 'password'})
   update_user(u, name)
   if picture: 
      path = "sample_images/" + picture
      if os.access(path, os.R_OK): set_picture(u, path)
   return u

def new_number():
   return "555-%03d-%04d" % (randrange(0, 999), randrange(0, 9999))

def pick_times(num):
   str  = []
   mask = 0
   for i in range(0, num):
      day  = randrange(1, len(days))
      time = randrange(1, len(times))
      str.append('%d,%d' % (day, time))
      mask |= (days[day] & times[time])
   return (':'.join(str), mask)

if os.access('tutor.db', os.R_OK):
   os.remove('tutor.db')

names = ('Dan', 'Eve', 'Francis', 'Gary', 'Hanna', 'Irene', 'Jonathan', 'Kyle', 'Lori') #, 'Matt', 'Nancy', 'Ophelia', 'Peter', 'Quara', 'Robert', 'Steve')
reviews = (
   (1.0, "This guy was awful. Didn't know anything about the subject area. Waste of money. It's a wonder I still passed the class."),
   (1.0, "It's hard to believe that someone charging this much can be so rude to a client. Sorry I'm not already an expert. That's what I hired you for, asshole."),
   (1.0, "Didn't help at all. Failed the class. :("),
   (1.0, ""),
   (2.0, "Charges too much for what you get."),
   (2.0, "I was really just looking for somebody to do the homework for me. Don't bother if you're looking for help."),
   (2.0, "Knew the subject okay, but showed up to every meeting late and didn't even apologize. Don't bother."),
   (2.0, ""),
   (3.0, "Okay, but not great. Seemed to know what he was talking about, but did a pretty lousy job of explaining it, so things took way longer than they should have. Bottom line, I'd try somebody else next time."),
   (3.0, "Meh."),
   (3.0, "Pretty helpful. Could answer questions and stuff. Still got a C in the class though."),
   (3.0, ""),
   (4.0, "Kickass tutor. Really knows his stuff. Raised my grade from a D- at midterms to an A- at the end. 4 stars because nobody's perfect."),
   (4.0, "This tutor helped a lot and could answer any question I had."),
   (4.0, "A little on the pricier side, but you get what you pay for. Had a pretty in-depth knowledge of the subject and responded quickly to e-mail. Showed up on-time to every tutoring session."),
   (4.0, ""),
   (5.0, "Amazing in every way. Shits rainbows and sunshine. Raised my grade from an F- to an A+ overnight. This may or may not have involved unauthroized access to a computer system."),
   (5.0, "No complaints and a great price!"),
   (5.0, "Really went above and beyond helping me with my classes. Even managed to teach a dumbass like myself to be an expert in this class. Encyclopedic knowlege of the subject too."),
   (5.0, ""),
)

about_me = (
   "I come from a long line of highly skilled tutors spanning hundreds of subject areas and spread all over the world. If you hire me, you're getting world-class service at an unbeatable price.",
   "I may not be the smartest or most knowledgeable tutor, but if you pick me, all of your wildest dreams will come true.",
   "I'm a senior and have gone through all of the basic classes. I can help you pick up on the key concepts and pass those finals. Your grades will go up by one letter grade or your money back.",
   "If you need some help with your homework or access to some old exams for your class, give me a call."
   "",
)

subjects = (
   'Math', 'Science', 'Physics', 'Algebra', 'English', 'History', 'Calculus', 'Calculus 1', 'Calculus 2', 'MATH-2419', 'MATH2301',
   'Phsyics 1', 'Physics 2', 'Advanced Engineering Math', 'EE-3410', 'Linear Algebra', 'EE-2101', 'Electrical Network Analysis',
   'ENA', 'EE-2310', 'Differential equations', 'Diff. Eq.', 'College algebra'
)

days  = (0x07777777, 0x07777700, 0x00000077, 0x07000000, 0x00700000, 0x00070000, 0x00007000, 0x00000700, 0x00000070, 0x00000007)
times = (0x07777777, 0x01111111, 0x02222222, 0x04444444)

users = []
for name in names:
   users.append(register('%s@example.org' % name.lower(), '%s Example' % name))
users.append(register("alice@example.org",             "Alice Example", "Andy.jpg"))
users.append(register("bob@example.org",               "Bob Robertson"))
users.append(register("cindy@example.org",             "Cindy Someone"))
users.append(register("android@robots.org",            "Andy Andoid",           "Andy.jpg"))
users.append(register("april.martimer@gmail.com",      "April Martimer",        "April_Martimer.jpg"))
users.append(register("billy.mcshady@student.uta.edu", "Billy McShady",         "Billy_McShady.jpg"))
users.append(register("elizabeth.kieran@gmail.com",    "Elizabeth Kieran",      "Elizabeth_Kieran.jpg"))
users.append(register("lynette@vc.net",                "First Citizen Lynette", "First_Citizen_Lynette.jpg"))
users.append(register("gaius2934@hotmail.com",         "Gaius Magnus",          "Gaius_Magnus.jpg"))
users.append(register("1337d00dXxX2@yahoo.com",        "Henry Young",           "Henry_Young.jpg"))
users.append(register("jwatson@twc.org",               "Jeremy Watson",         "Jeremy_Watson.jpg"))
users.append(register("marcus@gmail.com",              "Marcus",                "Marcus.jpg"))
users.append(register("jim@ncr.mil",                   "Jim Smith",             "Military_Police.jpg"))
users.append(register("admin@nv.gov",                  "Mr. House",             "Mr._House.jpg"))
users.append(register("myron@reno.gov",                "Myron",                 "Myron.jpg"))
users.append(register("santiago3294@gmail.com",        "Santiago",              "Santiago.jpg"))
users.append(register("soren442@yahoo.com",            "Soren",                 "Soren.jpg"))

for user in users:
   set_preferred(user, randrange(0, 5) == 0)

   for i in range(1, 5):
      subject = users[randrange(0, len(users))]
      favorite(user, subject, True)
      (score, text) = reviews[randrange(0, len(reviews))]
      review(user, subject, score, text)

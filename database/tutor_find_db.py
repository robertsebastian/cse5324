#!/usr/bin/python

import sqlite3
import os
import json
import hashlib
import collections
import math
import cgi
import cgitb
import sys
import time
import re
import base64

cgitb.enable()

db_name = 'tutor.db'
db      = None

################################################################################
class DbTable:
   """ Wrapper class to simplify common operations on a database table"""

   DbVar = collections.namedtuple('DbVar', 'name editable type')

   def __init__(self, name, cols):
      self.name     = name
      self.cols     = [DbTable.DbVar._make(c) for c in cols]
      self.col_type = collections.namedtuple('%s_type' % name, [c.name for c in self.cols])
      self.id_col   = self.cols[0]

      self.col_set          = set((c.name for c in self.cols))
      self.col_editable_set = set((c.name for c in self.cols if c.editable))

   def create(self):
      """Create this table"""

      column_text = ','.join([' '.join((c.name, c.type)) for c in self.cols])
      db.execute('create table %s (%s)' % (self.name, column_text))

   def select_many(self, conditions, condition_vals):
      """Return a list of rows from the database matching the given conditions

      conditions     -- SQL formatted condition string (e.g. 'val1=2 and val2=?')
      condition_vals -- List of parameters to sub into condition string
      """

      fields = ','.join(self.col_type._fields)
      dbc = db.cursor()
      dbc.execute('select %s from %s where %s' % (fields, self.name, conditions), condition_vals)
      return [u for u in map(self.col_type._make, dbc.fetchall())]

   def select_one(self, conditions, condition_vals):
      """Return a single row matching the given conditions or None

      conditions     -- SQL formatted condition string (e.g. 'val1=2 and val2=?')
      condition_vals -- List of parameters to sub into condition string
      """

      results = self.select_many(conditions, condition_vals);
      if len(results) == 0: return None
      return results[0]

   def count(self, conditions, condition_vals):
      dbc = db.cursor()
      dbc.execute('select %s from %s where %s' % (self.id_col.name, self.name, conditions), condition_vals)
      return len(dbc.fetchall())

   def get(self, id_val):
      """Return the row with the matching ID
         
      id_val -- row ID number
      """

      return self.select_one('%s=?' % self.id_col.name, [id_val])

   def insert(self, values):
      """Add a new row to the table

      values -- dict of column names mapped to their new values (e.g. {'name': 'Alice'})
      """

      #insert_vals = {k: values[k] for k in (set(values.keys()) & self.col_set)}
      insert_vals = {}
      for k in set(values.keys()) & self.col_set:
         insert_vals[k] = values[k]

      key_str = ','.join(insert_vals.keys())
      val_str = ','.join('?' * len(insert_vals))

      query = 'insert into %s(%s) values (%s)' % (self.name, key_str, val_str)
      dbc = db.cursor()
      dbc.execute(query, insert_vals.values())

      return dbc.lastrowid # Return row ID that we just added

   def update(self, id_val, changes, editable_only=False):
      """Update an existing row in the table

      id_val        -- row ID number
      changes       -- dict of column names mapped to their new values (e.g. {'name': 'Alice'})
      editable_only -- if True, only columns marked as editable will be modified
      """

      selected_set = self.col_editable_set if editable_only else self.col_set

      #update_vals = {k: changes[k] for k in (set(changes.keys()) & selected_set)}
      update_vals = {}
      for k in set(changes.keys()) & selected_set:
         update_vals[k] = changes[k]

      update_str = ','.join((k + '=?' for k in update_vals.keys()))

      query = 'update %s set %s where %s=?' % (self.name, update_str, self.id_col.name)
      db.execute(query, update_vals.values() + [id_val])

   def delete(self, conditions, condition_vals):
      """Delete a row from the table

      conditions     -- SQL formatted condition string (e.g. 'val1=2 and val2=?')
      condition_vals -- List of parameters to sub into condition string
      """
      db.execute('delete from %s where %s' % (self.name, conditions), condition_vals)

################################################################################
# Functions intended to be accessed by the database for searching
def geo_dist(lat1, lon1, lat2, lon2):
   """Calculate distance between two geographic points"""
   if not (lat1 and lon1 and lat2 and lon2): return float('inf')
   dlon = lon2 - lon1 
   dlat = lat2 - lat1 
   a = math.sin(dlat/2.0)**2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon/2.0)**2 
   c = 2 * math.atan2(math.sqrt(a), math.sqrt(1.0-a)) 
   return 3963.1676 * c # Multiply by radius of earth to get distance

def comp_tags(tags1, tags2):
   """Check for overlap between two sets of tags"""
   if not (tags1 and tags2): return False
   return len(set(tags1.split(',')) & set(tags2.split(','))) > 0

################################################################################
class RequestError(Exception):
   """Name for errors errors produced while processing a database request"""
   pass

################################################################################
# Table definitions for the database
users_table = DbTable('users', (
      ('user_id',                 False, 'integer primary key autoincrement'),
      ('password_hash',           False, 'text'),
      ('account_email',           False, 'text'),
      ('name',                    True,  'text'),
      ('name_normalized',         True,  'text'),
      ('phone',                   True,  'text'),
      ('about_me',                True,  'text'),
      ('public_email_address',    True,  'text'),
      ('loc_address',             True,  'text'),
      ('loc_lat',                 True,  'real'),
      ('loc_lon',                 True,  'real'),
      ('loc_max_dist',            True,  'real'),
      ('tutor_flag',              True,  'integer'),
      ('preferred_flag',          False, 'integer'),
      ('subject_tags',            True,  'text'),
      ('subject_tags_normalized', True,  'text'),
      ('availability_string',     True,  'text'),
      ('availability',            True,  'integer'),
      ('price_per_hour',          True,  'real'),
      ('score',                   False, 'real'),
      ('num_reviews',             False, 'integer')))
reviews_table = DbTable('reviews', (
      ('review_id',               False, 'integer primary key autoincrement'),
      ('submitter_id',            False, 'integer'),
      ('subject_id',              False, 'integer'),
      ('score',                   True,  'real'),
      ('text',                    True,  'text')))
sessions_table = DbTable('sessions', (
      ('session_id',              False, 'integer primary key autoincrement'),
      ('user_id',                 False, 'integer')))
favorites_table = DbTable('favorites', (
      ('row_id',                  False, 'integer primary key autoincrement'),
      ('user_id',                 True,  'integer'),
      ('subject_id',              True,  'integer')))
pictures_table = DbTable('pictures', (
      ('row_id',                  False, 'integer primary key autoincrement'),
      ('user_id',                 False, 'integer'),
      ('picture',                 True,  'blob')))

################################################################################
# Handler functions for user requests
def open_db():
   global db

   if db: return

   # Create database if it doesn't already exist
   do_create = not os.path.exists(db_name)
   db = sqlite3.connect(db_name)
   if do_create:
      users_table.create()
      reviews_table.create()
      sessions_table.create()
      favorites_table.create()
      pictures_table.create()
      db.commit()

   # Store search functions -- has to be done fresh every time
   db.create_function('geo_dist', 4, geo_dist)
   db.create_function('comp_tags', 2, comp_tags)

def close_db():
   global db

   if db: db.close()
   db = None

def login(req):
   """Check password and create session for existing user"""

   # Look for matching email/password
   hashed_pw = hashlib.sha1(req['password']).hexdigest()
   user = users_table.select_one('account_email=? and password_hash=?', [req['email'], hashed_pw])
   if not user:
      raise RequestError('login_failed')

   # Return session id if we find user
   sessions_table.delete('user_id=?', [user.user_id])
   session_id = sessions_table.insert({'user_id': user.user_id})
   db.commit()
   return {'session_id': session_id, 'user_id': user.user_id}

def register(req):
   """Add a new user and create new session"""

   # Make sure email meets requirements
   if len(req['email']) < 3: # TODO: Actual e-mail address validation
      raise RequestError('invalid_email')

   # Make sure password meets requirements
   if len(req['password']) < 6:
      raise RequestError('invalid_password')

   # Make sure user id doesn't already exist
   if users_table.select_one('account_email=?', [req['email']]):
      raise RequestError('duplicate_user')

   # Add user and return session ID
   user_id    = users_table.insert({
      'account_email':        req['email'],
      'public_email_address': req['email'],
      'name':                 req['email'],
      'password_hash':        hashlib.sha1(req['password']).hexdigest(),
   })
   session_id = sessions_table.insert({'user_id': user_id})
   db.commit()

   return {'session_id': session_id, 'user_id': user_id}

def validate_session(req):
   """Get valid session or throw exception"""

   session = sessions_table.get(req['session_id'])
   if not session:
      raise RequestError('invalid_session')
   return session

def update_user(req):
   """Update a user's profile data"""

   session = validate_session(req)

   # Validate length fields
   if 'about_me' in req and len(req['about_me']) > 3000:
      raise RequestError('length_error')
   # TODO: Validate other length fields

   # Add normalized name for searching
   if 'name_normalized' in req: del req['name_normalized']
   if 'name' in req:
      req['name_normalized'] = normalize_search_string(req['name'])

   # Add normalized subject tags for searching
   if 'subject_tags_normalized' in req: del req['subject_tags_normalized']
   if 'subject_tags' in req:
      req['subject_tags_normalized'] = normalize_search_string(req['subject_tags'])

   if 'tutor_flag' in req:
      req['tutor_flag'] = (0, 1)[req['tutor_flag']]

   # Update editable fields with request data
   users_table.update(session.user_id, req, True)
   db.commit()

   # Optionally add a picture with this request
   if 'picture' in req:
      set_picture(req)

   # Return updated data
   return sanitize_user(session, users_table.get(session.user_id))

def submit_review(req):
   """Add a new review for a tutor or update an existing one"""

   session = validate_session(req)

   # Valid score between 0 and 5
   if not 0.0 <= req['score'] <= 5.0:
      raise RequestError('invalid_score')

   # Check valid subject and can't submit rewviews for self
   subject = users_table.get(req['subject_id'])
   if not subject or session.user_id == subject.user_id:
      raise RequestError('invalid_subject')

   # Insert new review or update existing
   existing = reviews_table.select_one('submitter_id=? and subject_id=?', [session.user_id, subject.user_id])
   review_id = None
   if existing:
      reviews_table.update(existing.review_id, req, True)
      review_id = existing.review_id
   else:
      review_id = reviews_table.insert(dict({'submitter_id': session.user_id}, **req))

   # Update subject score
   scores = [r.score for r in reviews_table.select_many('subject_id=?', [subject.user_id])]
   new_score = sum(scores) / len(scores)

   users_table.update(subject.user_id, {'score': new_score, 'num_reviews': len(scores)})
   db.commit()

   return dict(reviews_table.get(review_id)._asdict())

def get_reviews(req):
   """Get full list of reviews for a given tutor"""

   validate_session(req)

   reviews = [dict(r._asdict()) for r in reviews_table.select_many('subject_id=?', [req['subject_id']])]
   for r in reviews:
      # Really ugly -- this could be a join
      user = users_table.get(r['submitter_id'])
      r['submitter_name'] = user.name

   return {'reviews': reviews}

def get_user(req):
   """Return a single user's profile data"""

   session = validate_session(req)

   user = users_table.get(req['user_id'])
   if not user:
      raise RequestError('invalid_user')
   user = sanitize_user(session, user)

   return user

def set_favorite(req):
   """Set the favorite status for a user"""

   session = validate_session(req)
   if req['favorited']:
      # Set favorited status if not already in the table
      if favorites_table.select_one('user_id=? and subject_id=?', [session.user_id, req['subject_id']]) == None:
         favorites_table.insert({'user_id': session.user_id, 'subject_id': req['subject_id']})
   else:
      # Clear favorite entry
      favorites_table.delete('user_id=? and subject_id=?', [session.user_id, req['subject_id']])
   db.commit()

   return {'subject_id': req['subject_id'], 'favorited': req['favorited']}

def get_favorites(req):
   """Get list of favorited users"""

   session = validate_session(req)

   favorites = []
   for fav in favorites_table.select_many('user_id=?', [session.user_id]):
      subject = users_table.get(fav.subject_id)
      if not subject: continue

      subject = sanitize_user(session, subject)
      subject['favorited'] = True
      
      favorites.append(subject)

   return {'favorites': favorites}

def sanitize_user(session, user):
   """Remove private fields from a user object and convert to dictionary"""

   user = dict(user._asdict())
   uid = user['user_id']

   # Append calculated values
   user['favorited'] = favorites_table.select_one('user_id=? and subject_id=?', [session.user_id, uid]) != None
   user['has_picture'] = pictures_table.count('user_id=?', [uid]) > 0
   user['preferred_flag'] = user['preferred_flag'] == 1 # Convert to boolean
   user['tutor_flag'] = user['tutor_flag'] == 1

   my_review = reviews_table.select_one('submitter_id=? and subject_id=?', [session.user_id, uid]);
   if my_review:
      user['my_score'] = my_review.score
      user['my_comment'] = my_review.text

   # Clean up fields we don't want to send
   del user['password_hash']
   return user

def normalize_search_string(string):
   return re.sub('[^0-9a-zA-Z,]+', '', string).lower()

def search(req):
   """Search database for a tutor"""
   session = validate_session(req)

   # First treat the query as a subject tag and search for that
   query = normalize_search_string(req['query'])
   query_glob = '*%s*' % query

   results = users_table.select_many( """
      tutor_flag == 1 and (
         comp_tags(subject_tags_normalized, ?) or
         name_normalized glob ?
      )""", [query, query_glob])
   results = [sanitize_user(session, u) for u in results]

   # Calculate distance from querying user
   for u in results:
      u['distance'] = geo_dist(u['loc_lat'], u['loc_lon'], req['lat'], req['lon'])

   return {'results': results}

# Store a user profile picture in the database
def set_picture(req):
   session = validate_session(req)

   # Make sure user id doesn't already exist
   already_present = pictures_table.count('user_id=?', [session.user_id]) > 0
   if already_present:
      pictures_table.update(picture.row_id, {'picture': req['picture']})
   else:
      pictures_table.insert({'user_id': session.user_id, 'picture': req['picture']})
   db.commit()

   return {}

# Retrieve a profile picture
def get_picture(req):
   session = validate_session(req)

   picture = pictures_table.select_one('user_id=?', [req['user_id']])
   if not picture:
      raise RequestError("no_picture")
   return {'picture': picture.picture}

################################################################################
# Admin functions
def set_preferred(user_id, flag):
   flagVal = (0, 1)[flag]
   users_table.update(user_id, {'preferred_flag': flagVal})
   db.commit();

################################################################################
# Table mapping request names to handler functions and required arguments
RequestHandler = collections.namedtuple('RequestHandler', 'func required')
request_table = {
   'login':         RequestHandler(login,         set(['email', 'password'])),
   'register':      RequestHandler(register,      set(['email', 'password'])),
   'update_user':   RequestHandler(update_user,   set(['session_id'])),
   'submit_review': RequestHandler(submit_review, set(['session_id', 'subject_id', 'score', 'text'])),
   'get_user':      RequestHandler(get_user,      set(['session_id', 'user_id'])),
   'get_reviews':   RequestHandler(get_reviews,   set(['session_id', 'subject_id'])),
   'set_favorite':  RequestHandler(set_favorite,  set(['session_id', 'subject_id', 'favorited'])),
   'get_favorites': RequestHandler(get_favorites, set(['session_id'])),
   'set_picture':   RequestHandler(set_picture,   set(['session_id', 'picture'])),
   'get_picture':   RequestHandler(get_picture,   set(['session_id', 'user_id'])),
   'search':        RequestHandler(search,        set(['session_id', 'query', 'lat', 'lon'])),
}

################################################################################
# Request handling
def handle_request(req_str):
   """Execute user request and return results"""

   req = json.loads(req_str)

   # Make sure action is defined
   if 'action' not in req or req['action'] not in request_table:
      return json.dumps({'action': 'unknown', 'success': False, 'error': 'invalid_action'})

   # Make sure argument list is complete
   args_required = request_table[req['action']].required
   if len(set(req.keys()) & args_required) != len(args_required):
      return json.dumps({'action': req['action'], 'success': False, 'error': 'incomplete_argument_list'})

   # Make request
   try:
      result = request_table[req['action']].func(req)
      result['success'] = True
      result['action'] = req['action']
      return json.dumps(result)
   except RequestError as e:
      return json.dumps({'success': False, 'error': e.args[0]})

if __name__ == "__main__":
   open_db()

   # Handle request and return result
   request = sys.stdin.read(int(os.environ['CONTENT_LENGTH']))
   result  = handle_request(request)

   close_db()

   print 'Connection: close'
   print 'Content-Type: application/json'
   print 'Content-Length: %d' % len(result)
   print
   print result

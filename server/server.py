"""

	Linkedlist

	:file: server.py
	:author: Nick Church
	:date: March, 2016
	:description:
		The implementation of our server which runs	as a python script,
		using the schema.sql file in this directory to initialize the db

"""

from contextlib import closing
import sqlite3
from flask import Flask, Response, request, session, g, url_for, abort
#from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash
from functools import wraps
import json
import hashlib
import time

# config
DATABASE = '/tmp/linkedlist.db'
DEBUG = True
SECRET_KEY = 'secret'
USERNAME = 'admin'
PASSWORD = 'password'

app = Flask(__name__)
#app.config['SQLALCHEMY_DATABASE_URL'] = 'sqlite:////tmp/linkedlist.db'
#db = SQLAlchemy(app)
app.config.from_object(__name__)
# app.config.from_envvar('LINKEDLIST_SETTINGS', silent=True) # our config file

"""~~~~SQLAlchemy class definitions~~~~"""
"""
class User(db.Model):
	id = db.Column(db.Integer, primary_key=True)
	username = db.Column(db.String(25))
	email = db.Column(db.String(120), unique=True)
	auth_token = db.Column(db.Text)

	def __init__(self, username, email, auth_token)
		self.username = username
		self.email = email
		auth_token = auth_token

class Session(db.Model):
	id = db.Column(db.Integer, primary_key=True)
	user_id = db.Column(db.Integer)
	session_api_key = db.Column(db.String())
"""

@app.before_request
def before_request():
	g.db = connect_db()

@app.teardown_request
def teardown_request(exception):
	db = getattr(g, 'db', None)
	if db is not None:
		db.close()

# initialize out database, reading from the file 'schema.sql' in this directory
def init_db():
	with closing(connect_db()) as db:
		with app.open_resource('schema.sql', mode='r') as f:
			db.cursor().executescript(f.read())
		db.commit()

def connect_db():
	return sqlite3.connect(app.config['DATABASE'])

def check_auth(api_key):
	cur = g.db.execute('select * from session where session_api_key=?', [api_key])
	return (len(cur.fetchall()) == 1)


def requires_auth(f):
	@wraps(f)
	def decorated(*args, **kwargs):
		session_api_key = request.get_json()['session_api_key']
		if not check_auth(session_api_key):
			abort(400, 'Invalid API key')
		else:
			return f(*args, **kwargs)
	return decorated

def get_auth_user(session_api_key):
	if session_api_key is None:
		return None
	rows = g.db.execute('select user_id from session where session_api_key=?', [session_api_key]).fetchall()
	if len(rows) == 1:
		return rows[0]
	else:
		return None

"""~~~~~ Routes ~~~~~"""

def json_ok_response(data=None):
	if data is not None:
		js = json.dumps(data)
		response = Response(js, status=200, mimetype='application/json')
		return response
	else:
		response = Response(status=200, mimetype='application/json')
		return response

def gen_api_key(auth_token):
	sha = hashlib.sha256()
	sha.update(str(auth_token)+str(time.time()))
	return sha.hexdigest()

@app.route('/user/login', methods=['POST'])
def login():
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	email = content.get('email_address')
	password = content.get('password')
	if email is None or password is None:
		abort(400, 'Invalid fields in request json')
	cur = g.db.execute('select id, auth_token from user where email=?', [email])
	rows = cur.fetchall()
	if (len(rows) == 0):
		abort(400, 'Invalid login credentials')
	elif (len(rows) == 1):
		user = rows[0]
		user_id = user[0]
		db_auth_token = user[1]
		session_api_key = gen_api_key(db_auth_token)
		if check_password_hash(db_auth_token, password):
			# the password is good, return a session token
			g.db.execute('delete from session where user_id=?', [user_id])
			g.db.execute('insert into session (user_id, session_api_key) values (?, ?)', [user_id, session_api_key])
			g.db.commit()
			return json_ok_response(dict(session_api_key=session_api_key))
		else:
			# the password is invalid, return a failure
			abort(400, 'Invalid login credentials')
	else:
		# this shouldn't be possible because emails are unique but we should still handle the case, and print an error to the server log.
		abort(500, 'Something bad happened, please contact a server administrator')


def email_taken(email):
	cur = g.db.execute('select * from user where email = ?', [email])
	return (len(cur.fetchall()) != 0)

@app.route('/user/createaccount', methods=['POST'])
def create_user():
	content = request.get_json() # force=True will try and get json data even if the header doesn't say the content type is application/json
	if not content:
		abort(400) # invalid request
	username = content.get('username')
	# verify that the email is unique to the database
	email = content.get('email_address')
	if email_taken(email):
		abort(400, 'Email is in use')
	password = content.get('password')
	password_conf = content.get('password_conf')

	if username is None or email is None or password is None or password_conf is None:
		abort(400, 'Invalid fields in request json')
	if password != password_conf: # passwords don't match
		abort(400, 'Passwords don\'t match')
	# hash the password into an auth token
	auth_token = generate_password_hash(password)
	# insert the data
	g.db.execute('insert into user (username, email, auth_token) values (?, ?, ?)',
		[username, email, auth_token])
	g.db.commit()
	cur = g.db.execute('select id, auth_token from user where email = ?', [email])
	row = cur.fetchone()
	user_id = row[0]
	auth_token = row[1]
	session_api_key = gen_api_key(auth_token)
	g.db.execute('insert into session (user_id, session_api_key) values (?, ?)', [user_id, session_api_key])
	g.db.commit()
	return json_ok_response(dict(session_api_key=session_api_key))

@app.route('/user', methods=['POST'])
@requires_auth
def get_user_info():
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	email = content.get('email_address')
	if email is None:
		abort(400)
	rows = g.db.execute('select username, email from user where email = ?', [email])
	row = rows.fetchone()
	if (len(row) == 0):
		abort(500, 'Invalid username')
	else:
		username = row[0]
		email = row[1]
		return json_ok_response(dict(username=username, email=email))

@app.route('/list/create', methods=['POST'])
@requires_auth
def create_list():
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	session_api_key = request.get_json()['session_api_key']
	name = content.get('name')
	if name is None:
		abort(400)
	user_id = get_auth_user(session_api_key)
	if user_id is None:
		abort(400)
	g.db.execute('insert into list (owner_id, name) values (?, ?)', [user_id, name])
	g.db.commit()
	return json_ok_response()

@app.route('/lists', methods=['POST'])
@requires_auth
def get_lists():
	session_api_key = request.get_json()['session_api_key']
	user_id = get_auth_user(session_api_key)
	rows = g.db.execute('select id, name from lists where owner_id = ?', [user_id]).fetchall()
	lists = []
	for row in rows:
		list_id = row[0]
		list_name = row[1]
		lists.append(dict(list_name=list_name, list_id=list_id))
	return json_ok_response(dict(lists=lists))


if __name__ == '__main__':
	app.run()

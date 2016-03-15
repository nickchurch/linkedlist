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
DATABASE = 'linkedlist.db'
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
# for when I have time to migrate our app to this schema...
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
		message_json = request.get_json()
		session_api_key = message_json.get('session_api_key')
		if session_api_key is None:
			abort(400, 'No API key')
		if not check_auth(session_api_key):
			abort(400, 'Invalid API key')
		else:
			return f(*args, **kwargs)
	return decorated

# return the user id associated with the passed in api key
def get_auth_user(session_api_key):
	if session_api_key is None:
		return None
	rows = g.db.execute('select user_id from session where session_api_key=?', [session_api_key]).fetchall()
	if len(rows) == 1:
		return rows[0][0]
	else:
		return None

"""~~~~ Routes ~~~~"""

def json_ok_response(data=None):
	if data is not None:
		js = json.dumps(data)
		response = Response(js, status=200, mimetype='application/json')
		return response
	else:
		response = Response(status=200, mimetype='application/json')
		return response

"""~~~~ User Routes ~~~~"""

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
	cur = g.db.execute('select id, auth_token, username from user where email==?', [email])
	rows = cur.fetchall()
	if (len(rows) == 0):
		abort(400, 'Invalid login credentials')
	elif (len(rows) == 1):
		user = rows[0]
		user_id = user[0]
		db_auth_token = user[1]
		username = user[2]
		session_api_key = gen_api_key(db_auth_token)
		if check_password_hash(db_auth_token, password):
			# the password is good, return a session token
			g.db.execute('delete from session where user_id=?', [user_id])
			g.db.execute('insert into session (user_id, session_api_key) values (?, ?)', [user_id, session_api_key])
			g.db.commit()
			return json_ok_response(dict(session_api_key=session_api_key, username=username))
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


"""~~~~ List Routes ~~~~"""
def list_exists(owner_id, list_name):
	rows = g.db.execute('select * from list where (owner_id==?) and (name==?)', [owner_id, list_name]).fetchall()
	return (len(rows) != 0)

def list_exists_and_user_is_owner(owner_id, list_id):
	rows = g.db.execute('select * from list where owner_id==? and id==?', [owner_id, list_id]).fetchall()
	return len(rows) == 1

def get_user_id_from_email(email):
	user = g.db.execute('select id from user where email==?', [email]).fetchone()
	if (len(user) == 1):
		user_id = user[0]
		return user_id
	else:
		return None

def list_exists_and_user_is_member(list_id, user_id):
	if list_id is None or user_id is None:
		return False
	list_exists = len(g.db.execute('select id from list where id==?', [list_id]).fetchall())
	if not list_exists:
		return False
	user_is_member = len(g.db.execute('select id from list_member where list_id==? and user_id==?', [list_id, user_id]).fetchall())
	if not user_is_member:
		return False
	return True

@app.route('/list/create', methods=['POST'])
@requires_auth
def create_list():
	content = request.get_json()
	if not content:
		app.logger.debug('create_list: no message content')
		abort(400) # invalid request
	session_api_key = request.get_json()['session_api_key']
	name = 'my new list'
	user_id = get_auth_user(session_api_key=session_api_key)
	if user_id is None:
		app.logger.debug('User tried to create a list, authorized but could not get user_id with session key %s' % session_api_key)
		abort(400)
	# a user can't create more than one list with the same name
	if list_exists(owner_id=user_id, list_name=name):
		app.logger.debug('User %s tried to create the list %s but it already exists' % (user_id, name))
		abort(400)
	g.db.execute('insert into list (owner_id, name) values (?, ?)', [user_id, name])
	g.db.commit()
	row = g.db.execute('select id from list where (owner_id==?) and (name==?)', [user_id, name]).fetchone()
	list_id = row[0]
	# add the user to the list_member table, even though this user is already listed as the list owner
	g.db.execute('insert into list_member (list_id, user_id) values (?, ?)', [list_id, user_id])
	g.db.commit()
	return json_ok_response(dict(list_id=list_id))

@app.route('/list/delete', methods=['POST'])
@requires_auth
def delete_list():
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	session_api_key = content.get('session_api_key')
	list_id = content.get('list_id')
	user_id = get_auth_user(session_api_key=session_api_key)
	if user_id is None:
		app.logger.debug('User tried to delete a list, authorized but could not get user_id with session key %s' % session_api_key)
		abort(400)
	if list_exists_and_current_user_is_owner(owner_id=user_id, list_id=list_id):
		g.db.execute('delete from list where id==?',[list_id])
		g.db.commit()
	else:
		abort(400)

@app.route('/list/update', methods=['POST'])
@requires_auth
def update_list():
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	session_api_key = content.get('session_api_key')
	list_id = content.get('list_id')
	name = content.get('name')
	if name is None:
		abort(400)
	user_id = get_auth_user(session_api_key=session_api_key)
	if user_id is None:
		app.logger.debug('User tried to delete a list, authorized but could not get user_id with session key %s' % session_api_key)
		abort(400)
	if list_exists_and_current_user_is_owner(owner_id=user_id, list_id=list_id):
		g.db.execute('update list set name=? where id=?',[name, list_id])
		g.db.commit()
	else:
		abort(400)

@app.route('/lists', methods=['POST'])
@requires_auth
def get_lists():
	session_api_key = request.get_json()['session_api_key']
	user_id = get_auth_user(session_api_key=session_api_key)
	rows = g.db.execute('select list_id from list_member where (user_id==?)', [user_id]).fetchall()
	lists = []
	for row in rows:
		this_list = g.db.execute('select id, name from list where (id==?)', [row[0]]).fetchone()
		list_id = this_list[0]
		list_name = this_list[1]
		lists.append(dict(list_name=list_name, list_id=list_id))
	return json_ok_response(dict(lists=lists))


@app.route('/list', methods=['POST'])
@requires_auth
def get_list():
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	list_id = content.get('list_id')
	if list_id is None:
		abort(400)
	# check that the user has authorization to access this list
	session_api_key = content['session_api_key']
	user_id = get_auth_user(session_api_key=session_api_key)
	# make sure the user is a member of this list
	if not list_exists_and_user_is_member(list_id=list_id, user_id=user_id):
		abort(400, 'User does not belong to requested list, or list does not exist')
	# get the name of the list
	list_info = g.db.execute('select name, owner_id from list where id==?', [list_id]).fetchone()
	list_name = list_info[0]
	list_owner_id = list_info[1]
	# compile the members of the list
	list_members = []
	list_member_rows = g.db.execute('select user_id from list_member where list_id==?', [list_id]).fetchall()
	for member_row in list_member_rows:
		member_id = member_row[0]
		list_member = g.db.execute('select username, email from user where id==?', [member_id]).fetchone()
		member_username = list_member[0]
		member_email = list_member[1]
		member = dict(id=member_id, username=member_username, email=member_email)
		list_members.append(member)
	# compile the items in the list
	list_items = []
	list_item_rows = g.db.execute('select id, value, checked from list_item where list_id==?', [list_id])
	for item_row in list_item_rows:
		item_id = item_row[0]
		item_value = item_row[1]
		item_checked = item_row[2]
		item = dict(id=item_id, value=item_value, checked=item_checked)
		list_items.append(item)
	data = dict(list_id=list_id, list_name=list_name, owner_id=list_owner_id, list_members=list_members, list_items=list_items)
	return json_ok_response(data)


@app.route('/list/adduser', methods=['POST'])
@requires_auth
def list_add_user():
	session_api_key = request.get_json()['session_api_key']
	current_user_id = get_auth_user(session_api_key=session_api_key)
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	list_id = content.get('list_id')
	if list_id is None:
		abort(400)
	user_to_add_email = content.get('user_email')
	if user_to_add_email is None:
		abort(400)
	list_exists_and_current_user_is_owner = len(g.db.execute('select * from list where owner_id==? and id==?', [current_user_id, list_id]).fetchall())
	if not list_exists_and_current_user_is_owner:
		abort(400)
	user_to_add_id = get_user_id_from_email(email=user_to_add_email)
	if not user_to_add_id:
		abort(400)
	# check to see if the user is already a member of this list
	already_member = len(g.db.execute('select * from list_member where list_id==? and user_id==?', [list_id,user_to_add_id]).fetchall())
	if already_member:
		abort(400)
	g.db.execute('insert into list_member (list_id, user_id) values (?, ?)', [list_id, user_to_add_id])
	g.db.commit()
	return json_ok_response()

@app.route('/list/removeuser', methods=['POST'])
@requires_auth
def list_remove_user():
	session_api_key = request.get_json()['session_api_key']
	current_user_id = get_auth_user(session_api_key=session_api_key)
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	# get the list id and check if it's valid
	list_id = content.get('list_id')
	if list_id is None:
		abort(400)
	# get the user to remove and check if it's valid and they actually belong to this list
	user_to_remove_id = content.get('user_id')
	if user_to_remove_id == current_user_id:
		# if you remove yourself from a list you own, the whole list is deleted, until we support ownership change
		g.db.execute('delete from list where id==?', [list_id])
		d.db.commit()
		return json_ok_response()
	if user_to_remove_id is None:
		abort(400)
	list_exists_and_current_user_is_owner = len(g.db.execute('select * from list where owner_id==? and id==?', [current_user_id, list_id]).fetchall())
	if not list_exists_and_current_user_is_owner:
		abort(400)
	user_belongs_to_list = len(g.db.execute('select * from list_member where list_id==? and user_id==?', [list_id, user_to_remove_id]).fetchall())
	if not user_belongs_to_list:
		abort(400) # that user doesn't belong to this list, you can't remove them
	# we have made all of the necessary checks, delete the row from the table where this user is a member of this list!
	g.db.execute('delete from list_member where list_id==? and user_id==?', [list_id, user_to_remove_id])
	g.db.commit()
	return json_ok_response()


"""~~~~ List Item Routes ~~~~"""

def item_with_value_exists_in_list(list_id, value):
	for list_item in g.db.execute('select value from list_item where list_id==?', [list_id]).fetchall():
		if list_item[0] == value:
			return True
	return False

def item_exists_in_list(list_id, item_id):
	return len(g.db.execute('select * from list_item where id==? and list_id==?', [item_id, list_id]).fetchall())

@app.route('/list/additemtolist', methods=['POST'])
@requires_auth
def add_list_item():
	session_api_key = request.get_json()['session_api_key']
	current_user_id = get_auth_user(session_api_key=session_api_key)
	content = request.get_json()
	if not content:
		abort(400, 'No message content') # invalid request
	# get the list id and check if it's valid
	list_id = content.get('list_id')
	if list_id is None:
		abort(400, 'No list_id')
	if not list_exists_and_user_is_member(list_id=list_id, user_id=current_user_id):
		abort(400, 'Not member of list')
	list_item = content.get('item')
	if list_item is None:
		abort(400, 'No list item')
	list_item_value = list_item.get('value')
	list_item_checked = bool(list_item.get('checked'))
	if list_item_value is None or list_item_checked is None:
		abort(400, 'List item has no data in the \'value\' or \'checked\' fields')
	# check if there is already an entry in this list with this value, if there is, abort
	if item_with_value_exists_in_list(list_id=list_id, value=list_item_value):
		abort(400, 'An item with this value already exists in this list')
	g.db.execute('insert into list_item (list_id, value, checked) values (?,?,?)', [list_id, list_item_value, list_item_checked])
	g.db.commit()
	return json_ok_response()

@app.route('/list/updatelistitem', methods=['POST'])
@requires_auth
def update_list_item():
	session_api_key = request.get_json()['session_api_key']
	current_user_id = get_auth_user(session_api_key=session_api_key)
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	# get the list id and check if it's valid
	list_id = content.get('list_id')
	if list_id is None:
		abort(400)
	if not list_exists_and_user_is_member(list_id=list_id, user_id=current_user_id):
		abort(400)
	list_item = content.get('item')
	if list_item is None:
		abort(400)
	list_item_to_update_id = list_item.get('id')
	if list_item_to_update_id is None:
		abort(400)
	list_item_value = list_item.get('value')
	list_item_checked = bool(list_item.get('checked'))
	if list_item_value is None or list_item_checked is None:
		abort(400)
	# check if there is already an entry in this list with this value that does not have this id, if there is, abort
	if (len(g.db.execute('select id from list_item where value==? and id!=?', [list_item_value, list_item_to_update_id]).fetchall()) != 0):
		abort(400)
	g.db.execute('update list_item set value=?, checked=? where id=?', [list_item_value, list_item_checked, list_item_to_update_id])
	g.db.commit()
	return json_ok_response()

@app.route('/list/removeitemfromlist', methods=['POST'])
@requires_auth
def remove_list_item():
	session_api_key = request.get_json()['session_api_key']
	current_user_id = get_auth_user(session_api_key=session_api_key)
	content = request.get_json()
	if not content:
		abort(400) # invalid request
	# get the list id and check if it's valid
	list_id = content.get('list_id')
	if list_id is None:
		abort(400)
	if not list_exists_and_user_is_member(list_id=list_id, user_id=current_user_id):
		abort(400)
	list_item_to_remove_id = content.get('item_id')
	if list_item_to_remove_id is None:
		abort(400)
	if not item_exists_in_list(list_id=list_id, item_id=list_item_to_remove_id):
		abort(400)
	# the user making the request is a member of the list and the item to remove belongs to the list, so remove it
	g.db.execute('delete from list_item where id==?', [list_item_to_remove_id])
	g.db.commit()
	return json_ok_response()

if __name__ == '__main__':
	app.run()

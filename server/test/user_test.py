"""
create user, log in, and get information back
"""

import requests
import json

headers = {'content-type': 'application/json'}
url = 'http://127.0.0.1:5000'
create_user_route='/user/createaccount'
login_user_route='/user/login'
get_user_data_route='/user'

create_user_data = {'username': 'Alice Test',
        'email_address': 'alice@test.com',
        'password': 'test123',
        'password_conf': 'test123'}

login_user_data = {'email_address': 'alice@test.com', 'password': 'test123'}

try:
    create_response = requests.post(url+create_user_route, data=json.dumps(create_user_data), headers=headers)
    print 'status code:',create_response.status_code,'\nrequest body:',create_response.request.body,'\nresponse text:',create_response.text,'\n'

    login_response = requests.post(url+login_user_route, data=json.dumps(login_user_data), headers=headers)
    print 'status code:',login_response.status_code,'\nrequest body:',login_response.request.body,'\nresponse text:',login_response.text,'\n'

    session_api_key = json.loads(login_response.text)['session_api_key']
    get_user_data = {'session_api_key': session_api_key, 'email_address': 'alice@test.com'}

    get_response = requests.post(url+get_user_data_route, data=json.dumps(get_user_data), headers=headers)
    print 'status code:',get_response.status_code,'\nrequest body:',get_response.request.body,'\nresponse text:',get_response.text,'\n'

except Exception as e:
    print e

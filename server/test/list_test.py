"""
create list, get all lists, get specific list
"""

import requests
import json
import datetime

headers = {'content-type': 'application/json'}
url = 'http://127.0.0.1:5000'

login_user_route='/user/login'
create_list_route='/list/create'
get_lists_route='/lists'
add_user_to_list_route='/list/adduser'
remove_user_from_list_route='/list/removeuser'

login_user_data = {'email_address': 'alice@test.com', 'password': 'test123'}

try:
    login_response = requests.post(url+login_user_route, data=json.dumps(login_user_data), headers=headers)
    print 'status code: ',login_response.status_code,'\nrequest body:',login_response.request.body,'\nresponse text:',login_response.text,'\n'

    session_api_key = json.loads(login_response.text)['session_api_key']
    """
    create_list_data = {'session_api_key': session_api_key, 'name': 'test list created at: '+str(datetime.datetime.now())}
    create_list_response = requests.post(url+create_list_route, data=json.dumps(create_list_data), headers=headers)
    print 'status code: ',create_list_response.status_code,'\nrequest body: ',create_list_response.request.body,'\nresponse text: ',create_list_response.text,'\n'

    get_lists_data = {'session_api_key': session_api_key}
    get_lists_response = requests.post(url+get_lists_route, data=json.dumps(get_lists_data), headers=headers)
    print 'status code: ',get_lists_response.status_code,'\nrequest body: ',get_lists_response.request.body,'\nresponse text: ',get_lists_response.text,'\n'
    """

    add_user_to_list_data = {'session_api_key': session_api_key, 'list_id': 11, 'user_email': 'bob@test.com'}
    add_user_to_list_response = requests.post(url+add_user_to_list_route, data=json.dumps(add_user_to_list_data), headers=headers)
    print 'status code: ',add_user_to_list_response.status_code,'\nrequest body: ',add_user_to_list_response.request.body,'\nresponse text: ',add_user_to_list_response.text,'\n'

    remove_user_from_list_data = {'session_api_key': session_api_key, 'list_id': 11, 'user_id': 1}
    remove_user_from_list_response = requests.post(url+remove_user_from_list_route, data=json.dumps(remove_user_from_list_data), headers=headers)
    print 'status code: ',remove_user_from_list_response.status_code,'\nrequest body: ',remove_user_from_list_response.request.body,'\nresponse text: ',remove_user_from_list_response.text,'\n'

except Exception as e:
    print e

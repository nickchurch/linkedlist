"""
tests creation, updating, and deletion of list_item entries
"""

import requests
import json

headers = {'content-type': 'application/json'}
url = 'http://127.0.0.1:5000'

login_user_route='/user/login'
create_list_item_route='/list/additemtolist'
update_list_item_route='/list/updatelistitem'
remove_list_item_route='/list/removeitemfromlist'

login_user_data = {'email_address': 'alice@test.com', 'password': 'test123'}

try:
    login_response = requests.post(url+login_user_route, data=json.dumps(login_user_data), headers=headers)
    print 'status code: ',login_response.status_code,'\nrequest body: ',login_response.request.body,'\nresponse text: ',login_response.text,'\n'

    session_api_key = json.loads(login_response.text)['session_api_key']

    create_path = url+create_list_item_route
    create_list_item_data = {'session_api_key': session_api_key, 'list_id': 2, 'item': {'value': 'get captain crunch', 'list_id': 1, 'checked': 0}}
    create_list_item_response = requests.post(create_path, data=json.dumps(create_list_item_data), headers=headers)
    print 'status code: ',create_list_item_response.status_code,'\nrequest body: ',create_list_item_response.request.body,'\nresponse text: ',create_list_item_response.text,'\n'

    update_list_item_data = {'session_api_key': session_api_key, 'list_id': 2, 'item': {'id': 1, 'value': 'get captain crunch', 'list_id': 1, 'checked': 1}}
    update_path = url+update_list_item_route
    update_list_item_response = requests.post(update_path, data=json.dumps(update_list_item_data), headers=headers)
    print 'status code: ',update_list_item_response.status_code,'\nrequest body: ',update_list_item_response.request.body,'\nresponse text: ',update_list_item_response.text,'\n'

    remove_list_item_data = {'session_api_key': session_api_key, 'list_id': 2, 'item_id': 1}
    remove_path = url+remove_list_item_route
    remove_list_item_response = requests.post(remove_path, data=json.dumps(remove_list_item_data), headers=headers)
    print 'status code: ',remove_list_item_response.status_code,'\nrequest body: ',remove_list_item_response.request.body,'\nresponse text: ',remove_list_item_response.text,'\n'

except Exception as e:
    print e

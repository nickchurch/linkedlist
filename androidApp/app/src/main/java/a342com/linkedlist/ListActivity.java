package a342com.linkedlist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ListActivity extends AppCompatActivity {

    public static String LOG_TAG = "My log tag";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public static String username = "";
    public static String email = "";
    public static String session_api_key = "";
    public static String password = "";

    public ArrayList<RoomElement> roomList;
    public MyAdapter aa;
    public ListsService lists_service;

    private class RoomElement {
        public String list_id;
        public String list_name;

        RoomElement(String _id, String _name) {
            list_id = _id;
            list_name = _name;
        }
    }

    private class MyAdapter extends ArrayAdapter<RoomElement> {
        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<RoomElement> _rooms) {
            super(_context, _resource, _rooms);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;
            RoomElement w = getItem(position);

            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource, newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            TextView txt_room_name = (TextView) newView.findViewById(R.id.room_item_name);
            txt_room_name.setText(w.list_name);

            ImageButton b = (ImageButton) newView.findViewById(R.id.room_item_leave);

            b.setTag(w);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(
                            getApplicationContext(),
                            "leaving list " + ((RoomElement) v.getTag()).list_id,
                            Toast.LENGTH_LONG
                    ).show();
                    removeList(v);
                }
            });

            newView.setTag(w);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(
                            getApplicationContext(),
                            "clicked room " + ((RoomElement) v.getTag()).list_id,
                            Toast.LENGTH_LONG
                    ).show();
                    gotoList(v);
                }
            });

            return newView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        roomList = new ArrayList<RoomElement>();
        aa = new MyAdapter(this, R.layout.sub_list_element, roomList);
        ListView myListView = (ListView) findViewById(R.id.lst_msglist);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList(new View(getApplicationContext()));
                swipeLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, 0);
        username = prefs.getString("username", "");
        email = prefs.getString("email", "");
        session_api_key = prefs.getString("session_api_key", "");
        password = prefs.getString("password", "");

        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(username + "'s Lists");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.http_base_url))
                .addConverterFactory(GsonConverterFactory.create())    //parse Gson string
                .client(httpClient)    //add logging
                .build();
        lists_service = retrofit.create(ListsService.class);

        refreshList(new View(getApplicationContext()));
    }

    public void refreshList(View v) {
        refreshList();
    }

    public void refreshList() {
        aa.clear();

        Call<getListsResponse> queryLists = lists_service.get_lists(new getListsRequest(session_api_key, ""));

        queryLists.enqueue(new Callback<getListsResponse>() {
            @Override
            public void onResponse(Response<getListsResponse> response) {
                if (response.isSuccess()) {
                    List<Room> roomsResponse = response.body().lists;
                    while (!roomsResponse.isEmpty()) {
                        Room elem = roomsResponse.remove(roomsResponse.size() - 1);
                        RoomElement le = new RoomElement(
                                elem.list_id,
                                elem.list_name
                        );
                        roomList.add(le);
                    }
                    aa.notifyDataSetChanged();
                } else {
                    //TODO: error
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(
                        getApplicationContext(),
                        "Could not connect to server!\n\n"
                                + "ListActivity().getListsResponse.onFailure():\n"
                                + t.toString(),
                        Toast.LENGTH_LONG)
                        .show();
            }
        });

    }

    public void createList(View v) {


        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Add a list");
        alert.setMessage("Specify a name");

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String t = input.getText().toString();

                Call<createListResponse> createList = lists_service.create_list(new getListsRequest(session_api_key, t));

                createList.enqueue(new Callback<createListResponse>() {
                    @Override
                    public void onResponse(Response<createListResponse> response) {
                        if (response.isSuccess()) {
                            createListResponse resp = response.body();
                            Toast.makeText(
                                    getApplicationContext(),
                                    "created list=" + resp.list_id,
                                    Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            //TODO: error
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Could not connect to server!\n\n"
                                        + "ListActivity().getListsResponse.onFailure():\n"
                                        + t.toString(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

                //refresh
                refreshList(new View(getApplicationContext()));
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    public void removeList(View v) {
        String list_id = ((RoomElement) v.getTag()).list_id;

        Call<blankResponse> removeUser = lists_service.remove_user(new removeUserRequest(session_api_key, list_id, email));
        removeUser.enqueue(new Callback<blankResponse>() {
            @Override
            public void onResponse(Response<blankResponse> response) {
                if (response.isSuccess()) {
                    //TODO: success.  Toast I guess?
                } else {
                    //TODO: error.
                }
                refreshList(new View(getApplicationContext()));
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(
                        getApplicationContext(),
                        "Could not connect to server!\n\n"
                                + "ListActivity().getListsResponse.onFailure():\n"
                                + t.toString(),
                        Toast.LENGTH_LONG)
                        .show();
            }
        });

    }

    public void gotoList(View v) {
        RoomElement roomEntering = (RoomElement) v.getTag();
        Toast.makeText(
                getApplicationContext(),
                "going to list=" + roomEntering.list_name,
                Toast.LENGTH_LONG)
                .show();

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("list_id", roomEntering.list_id);
        editor.putString("list_name", roomEntering.list_name);
        editor.commit();


        Intent intent = new Intent(this, ListItemsActivity.class);
        startActivity(intent);
    }

    public interface ListsService {
        @POST("lists")
        Call<getListsResponse> get_lists(@Body getListsRequest body);

        @POST("list/create")
        Call<createListResponse> create_list(@Body getListsRequest body);

        @POST("list/removeuser")
        Call<blankResponse> remove_user(@Body removeUserRequest body);
    }

}

class createListResponse {
    public String list_name;
    public String list_id;

    createListResponse(String _id, String _name) {
        this.list_id = _id;
        this.list_name = _name;
    }
}

class blankResponse {
    public String result;
}

class removeUserRequest {
    public String session_api_key;
    public String list_id;
    public String user_id;

    removeUserRequest(String _session_api_key, String _list_id, String _user_id) {
        this.session_api_key = _session_api_key;
        this.list_id = _list_id;
        this.user_id = _user_id;
    }
}

class getListsRequest {
    public String session_api_key;
    public String name;

    getListsRequest(String _session_api_key, String _name) {
        this.session_api_key = _session_api_key;
        this.name = _name;
    }
}

class getListsResponse {
    public List<Room> lists = new ArrayList<Room>();
}

class Room {
    public String list_id;
    public String list_name;

    Room(String _id, String _name) {
        this.list_id = _id;
        this.list_name = _name;
    }
}
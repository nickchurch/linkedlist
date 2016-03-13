package a342com.linkedlist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import android.location.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    public static String LOG_TAG = "My log tag";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public static String nickname = "";
    public static String email = "";
    public static String auth_token = "";
    public static String password = "";

    public ArrayList<RoomElement> roomList;
    public MyAdapter aa;

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

        public MyAdapter (Context _context, int _resource, List<RoomElement> _rooms) {
            super(_context, _resource, _rooms);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
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

            return newView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, 0);
        nickname = prefs.getString("username", "");
        email = prefs.getString("email", "");
        auth_token = prefs.getString("auth_token", "");
        password = prefs.getString("password", "");

        ((CollapsingToolbarLayout)findViewById(R.id.toolbar_layout)).setTitle(nickname + "'s Lists");

        refreshList();
    }

    public void refreshList(View v) {refreshList();}
    public void refreshList() {
        aa.clear();
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
        ListsService service = retrofit.create(ListsService.class);
        Call<ListsResponse> queryLists = service.getLists(auth_token);

        queryLists.enqueue(new Callback<ListsResponse>() {
            @Override
            public void onResponse(Response<ListsResponse> response) {
                if (response.body() != null && response.body().result.equals("ok")) {
                    List<Room> roomsResponse = response.body().resultList;
                    while (!roomsResponse.isEmpty()) {
                        Room elem = roomsResponse.remove(roomsList.size()-1);
                        //TODO
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
                                + "ListActivity().ListsResponse.onFailure():\n"
                                + t.toString(),
                        Toast.LENGTH_LONG)
                        .show();
            }
        });
    }


    public void createList(View v) {createList();}
    public void createList() {
        //TODO
    }

    public void removeList(View v) {removeList();}
    public void removeList() {
        //TODO
    }

    public void gotoList(View v) {
        //TODO
    }

    public interface ListsService {
        @GET("lists")
        Call<ListsResponse> getLists(@Query("session_api_key") String auth_token);


        @GET("list/removeuser")
        Call<ListsResponse> leaveList(@Query("session_api_key") String auth_token,
                                      @Query("list_id") String list_id,
                                      @Query("user_id") String user_id);


        @GET("") //TODO: not implemented
        Call<LoginResponse> createList(@Query("email_address") String email,
                                       @Query("password") String password,
                                       @Query("password_conf") String password_conf,
                                       @Query("username") String nickname);
    }

}

class ListsResponse {
    @SerializedName("result")
    @Expose
    public String result;
    @SerializedName("linkedlists")
    @Expose
    public List<Room> resultList = new ArrayList<Room>();
}

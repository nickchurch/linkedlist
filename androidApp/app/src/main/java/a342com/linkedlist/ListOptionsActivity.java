package a342com.linkedlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

/**
 * Created by Josh Shih on 3/13/2016.
 */
public class ListOptionsActivity extends AppCompatActivity{

    public static String LOG_TAG = "My log tag";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public static String username = "";
    public static String email = "";
    public static String session_api_key = "";
    public static String password = "";
    public static String list_id = "";

    public ArrayList<memberList> memberList2;
    public MyAdapter aa;
    public ListService members_service;



    private class MyAdapter extends ArrayAdapter<memberList> {
        int resource;
        Context context;

        public MyAdapter (Context _context, int _resource, List<memberList> _memberList) {
            super(_context, _resource, _memberList);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            LinearLayout newView;
            memberList w = getItem(position);

            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource, newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            ((TextView) newView.findViewById(R.id.member_name)).setText(w.username);

            ImageButton b = (ImageButton)newView.findViewById(R.id.kick_user);
            b.setTag(w);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(
                            getApplicationContext(),
                            "removing member " + ((memberList) v.getTag()).user_id,
                            Toast.LENGTH_LONG
                    ).show();
                    remove_member(v);
                }
            });

            return newView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_options);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        memberList2 = new ArrayList<memberList>();
        aa = new MyAdapter(this, R.layout.sub_list_element, memberList2);
        ListView myListView = (ListView) findViewById(R.id.lst_memberlist);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();

    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, 0);
        username = prefs.getString("username", "");
        email = prefs.getString("email", "");
        session_api_key = prefs.getString("session_api_key", "");
        password = prefs.getString("password", "");
        list_id = prefs.getString("list_id", "");

        //TODO: set title to name of room
        //((CollapsingToolbarLayout)findViewById(R.id.toolbar_layout)).setTitle(username + "'s Lists");

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
        members_service = retrofit.create(ListService.class);

        refreshListOfMembers(new View(getApplicationContext()));

    }

    public void refreshListOfMembers(View v) {
        aa.clear();

        Call<listItemResponse> queryMembers = members_service.get_list(new listItemRequest(session_api_key, list_id));
        queryMembers.enqueue(new Callback<listItemResponse>() {

            @Override
            public void onResponse(Response<listItemResponse> response) {
                if (response.isSuccess()) {
                    List<memberList> mems = response.body().list_members;
                    while (!mems.isEmpty()) {
                        memberList le = mems.remove(mems.size() - 1);
                        memberList2.add(le);
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

    public void remove_member(View v) {
        memberList elem = (memberList) v.getTag();
        //TODO

        refreshListOfMembers(v);
    }

    public void addMember(View v) {
        //TODO
        //send add_item to server with "empty" item
        //refresh
    }
    public interface ListService {
        @POST("list")
        Call<listItemResponse> get_list(@Body listItemRequest body);
        @POST("list/adduser")
        Call<blankResponse> add_member(@Body listMemberRequest body);
        @POST("list/removeuser")
        Call<blankResponse> remove_member(@Body removeMemberRequest body);
    }




}

class removeMemberRequest {
    public String session_api_key;
    public String list_id;
    public String user_id;

    removeMemberRequest (String _session_api_key, String _list_id, String _user_id) {
        this.session_api_key = _session_api_key;
        this.list_id = _list_id;
        this.user_id = _user_id;
    }
}

class listMemberRequest {
    public String session_api_key;
    public String list_id;
    public memberList member;

    listMemberRequest (String _session_api_key, String _list_id, memberList _member) {
        this.session_api_key = _session_api_key;
        this.list_id = _list_id;
        this.member = _member;
    }
}
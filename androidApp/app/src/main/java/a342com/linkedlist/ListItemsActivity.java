package a342com.linkedlist;

import android.app.LauncherActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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
import retrofit2.http.Body;
import retrofit2.http.POST;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ListItemsActivity extends AppCompatActivity {

    public static String LOG_TAG = "My log tag";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public static String username = "";
    public static String email = "";
    public static String session_api_key = "";
    public static String password = "";
    public static String list_id = "";

    public ArrayList<ListItem> listItems;
    public MyAdapter aa;
    public ItemsService items_service;

    public class ListItem {
        public String value;
        public String item_id;
        public boolean checked;

        ListItem (String _value, String _item_id, boolean _checked) {
            this.value = _value;
            this.item_id = _item_id;
            this.checked = _checked;
        }
    }

    private class MyAdapter extends ArrayAdapter<ListItem> {
        int resource;
        Context context;

        public MyAdapter (Context _context, int _resource, List<ListItem> _listitems) {
            super(_context, _resource, _listitems);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            LinearLayout newView;
            ListItem w = getItem(position);

            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource, newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            ((EditText) findViewById(R.id.listitem_value)).setText(w.value);
            ((CheckBox) findViewById(R.id.listitem_chk)).setChecked(w.checked);

            ImageButton b = (ImageButton)findViewById(R.id.listitem_remove);
            b.setTag(w);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(
                            getApplicationContext(),
                            "removing item " + ((ListItem) v.getTag()).item_id,
                            Toast.LENGTH_LONG
                    ).show();
                    removeItem(v);
                }
            });

            CheckBox c = (CheckBox)findViewById(R.id.listitem_chk);
            c.setTag(w);
            c.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(
                            getApplicationContext(),
                            "item " + ((ListItem) v.getTag()).item_id + " is now " + ((ListItem)v.getTag()).item_id,
                            Toast.LENGTH_LONG
                    ).show();
                    updateItem(v);
                }
            });

            EditText txt = (EditText)findViewById(R.id.listitem_value);
            txt.setTag(w);
            txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        Toast.makeText(
                                getApplicationContext(),
                                "item " + ((ListItem) v.getTag()).item_id + "changed to:" + ((ListItem)v.getTag()).value,
                                Toast.LENGTH_LONG
                        ).show();
                        updateItem(v);
                    }
                }
            });

            return newView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listItems = new ArrayList<ListItem>();
        aa = new MyAdapter(this, R.layout.sub_list_element, listItems);
        ListView myListView = (ListView) findViewById(R.id.lst_itemlist);
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
        items_service = retrofit.create(ItemsService.class);

        refreshList(new View(getApplicationContext()));
    }

    public void refreshList(View v) {
        //TODO: preferably add new elements at the top so it looks right after an addItem() call
        //basically the same as the ListActivity version
    }

    public void removeItem(View v) {
        ListItem elem = (ListItem) v.getTag();
        //TODO

        refreshList(v);
    }

    public void updateItem(View v) {
        ListItem elem = (ListItem) v.getTag();
        //TODO
        refreshList(v);  //Do we need this? the value should be there
    }

    public void addItem(View v) {
        //TODO
        //send add_item to server with "empty" item
        //refresh
    }

    public void goto_ListOptions(View v) {
        //TODO: we also need a button for this
    }

    public interface ItemsService {
        @POST("list/additemtolist/")
        Call<blankResponse> add_item_to_list(@Body listItemRequest body);

        @POST("list/removeitemfromlist")
        Call<blankResponse> remove_item_from_list(@Body removeItemRequest body);

        @POST("list/updatelistitem")
        Call<blankResponse> update_list_item(@Body listItemRequest body);
    }
}

class removeItemRequest {
    public String session_api_key;
    public String list_id;
    public String item_id;

    removeItemRequest (String _session_api_key, String _list_id, String _item_id) {
        this.session_api_key = _session_api_key;
        this.list_id = _list_id;
        this.item_id = _item_id;
    }
}

class listItemRequest {
    public String session_api_key;
    public String list_id;
    public ListItemsActivity.ListItem item;

    listItemRequest (String _session_api_key, String _list_id, ListItemsActivity.ListItem _item) {
        this.session_api_key = _session_api_key;
        this.list_id = _list_id;
        this.item = _item;
    }
}
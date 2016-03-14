package a342com.linkedlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.security.SecureRandom;
import java.math.BigInteger;

import android.widget.EditText;

public class ListItemsActivity extends AppCompatActivity {

    public static String LOG_TAG = "My log tag";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public static String username = "";
    public static String email = "";
    public static String session_api_key = "";
    public static String password = "";
    public static String list_id = "";
    public static String list_name = "";

    //FIXME
    public static int item_id_counter = 100;

    public ArrayList<listItemElem> listItemElems;
    public MyAdapter2 aa;
    public ItemsService items_service;

    public class listItemElem {
        public String value;
        public String item_id;
        public int checked;

        listItemElem(String _value, String _item_id, int _checked) {
            this.value = _value;
            this.item_id = _item_id;
            this.checked = _checked;
        }
    }

    private class MyAdapter2 extends ArrayAdapter<listItemElem> {
        int resource;
        Context context;
        List<listItemElem> items;

        public MyAdapter2 (Context _context, int _resource, List<listItemElem> _listitems) {
            super(_context, _resource, _listitems);
            resource = _resource;
            context = _context;
            this.context = _context;
            this.items = _listitems;
        }

        @Override
        public View getView (final int position, View convertView, ViewGroup parent) {
            LinearLayout newView;
            listItemElem w = getItem(position);

            if (convertView == null) {
                newView = new LinearLayout(getApplicationContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource, newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            EditText edt = ((EditText) newView.findViewById(R.id.listitem_value));
            edt.setText(w.value);
            boolean whyJava = (w.checked != 0);
            ((CheckBox) newView.findViewById(R.id.listitem_chk)).setChecked(whyJava);


            ImageButton b = (ImageButton) newView.findViewById(R.id.listitem_remove);
            b.setTag(w);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(
                            getApplicationContext(),
                            "clicked imagebutton " + ((listItemElem) v.getTag()).item_id,
                            Toast.LENGTH_LONG
                    ).show();
                    //removeItem(v);
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

        listItemElems = new ArrayList<listItemElem>();
        aa = new MyAdapter2(this, R.layout.content_list_items, listItemElems);
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
        list_name = prefs.getString("list_name", "");

        ((CollapsingToolbarLayout)findViewById(R.id.item_list_toolbar_layout)).setTitle(list_name);

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
        aa.clear();

        Call<listItemResponse> getItems = items_service.get_items_list(new listItemRequest(session_api_key, list_id));
        getItems.enqueue(new Callback<listItemResponse>() {
            @Override
            public void onResponse(Response<listItemResponse> response) {
                if (response.isSuccess()) {

                    List<listItemElem> items = response.body().list_items;
                    while (!items.isEmpty()) {
                        listItemElem elem = items.remove(items.size() - 1);
                        listItemElems.add(elem);
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
                                + "ListItemsActivity().refreshList.onFailure():\n"
                                + t.toString(),
                        Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    public void removeItem(View v) {
        listItemElem elem = (listItemElem) v.getTag();
        //TODO

        refreshList(v);
    }

    public void updateItem(View v) {
        listItemElem elem = (listItemElem) v.getTag();
        //TODO
        refreshList(v);  //Do we need this? the value should be there
    }

    public void addItem(View v) {
        //TODO

        Call<blankResponse> addItem = items_service.add_item_to_list(
                new listItemRequest(session_api_key, list_id,
                        new listItemElem(
                                (new BigInteger(130, new SecureRandom()).toString(32)),
                                Integer.toString(item_id_counter),
                                0)));
        item_id_counter++;
        addItem.enqueue(new Callback<blankResponse>() {
            @Override
            public void onResponse(Response<blankResponse> response) {
                refreshList(new View(getApplicationContext()));
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(
                        getApplicationContext(),
                        "Could not connect to server!\n\n"
                                + "ListItemsActivity().addItem.onFailure():\n"
                                + t.toString(),
                        Toast.LENGTH_LONG)
                        .show();
            }
        });

        refreshList(v);
    }

    public void goto_ListOptions(View v) {
        //TODO: we also need a button for this
        Toast.makeText(
                getApplicationContext(),
                "going to List Options",
                Toast.LENGTH_LONG)
                .show();

        Intent intent = new Intent(this, ListOptionsActivity.class);
        startActivity(intent);
    }

    public interface ItemsService {
        @POST("list")
        Call<listItemResponse> get_items_list(@Body listItemRequest body);

        @POST("list/additemtolist")
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

class listItemResponse {
    public String list_id;
    public String list_name;
    public String owner_id;
    public List<memberList> list_members = new ArrayList<memberList>();
    public List<ListItemsActivity.listItemElem> list_items = new ArrayList<ListItemsActivity.listItemElem>();
}

class memberList {
    public String user_id;
    public String username;
    public String email;
}

class listItemRequest {
    public String session_api_key;
    public String list_id;
    public ListItemsActivity.listItemElem item;

    listItemRequest (String _session_api_key, String _list_id, ListItemsActivity.listItemElem _item) {
        this.session_api_key = _session_api_key;
        this.list_id = _list_id;
        this.item = _item;
    }

    listItemRequest (String _session_api_key, String _list_id) {
        this.session_api_key = _session_api_key;
        this.list_id = _list_id;
        this.item = null;
    }
}
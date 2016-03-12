package a342com.linkedlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Josh Shih on 3/12/2016.
 */
public class ListActivity extends AppCompatActivity{

    public static String email = "";
    public static String username = "";
    public static String auth_token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        username = intent.getStringExtra(MainActivity.username);
        email = intent.getStringExtra(MainActivity.email);

        Bundle ids = getIntent().getExtras();
        if(ids != null){
            auth_token = ids.getString(auth_token);
        }else {
            Intent intent1 = new Intent(this, MainActivity.class);
            Toast.makeText(getApplicationContext(), "Not Logged In!", Toast.LENGTH_LONG).show();
            startActivity(intent1);
        }

        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();

    }


    @Override
    protected void onResume(){
        super.onResume();
    }
}

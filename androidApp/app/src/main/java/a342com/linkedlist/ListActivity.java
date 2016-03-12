package a342com.linkedlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Josh Shih on 3/12/2016.
 */
public class ListActivity extends AppCompatActivity{

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    public static String email = "";
    public static String username = "";
    public static String auth_token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, 0);
        username = prefs.getString("username", "None");
        email = prefs.getString("email", "None");
        auth_token = prefs.getString("auth_token", "None");

        if(auth_token == "None"){
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
    protected  void onPause(){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("auth_token", auth_token);
        editor.putString("email", email);
        editor.putString("username", username);
        editor.commit();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}

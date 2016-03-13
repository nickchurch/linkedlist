package a342com.linkedlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = "My log tag";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public static String username = "";
    public static String email = "";
    public static String auth_token = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //test
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, 0);
        username = prefs.getString("username", "None");
        email = prefs.getString("email", "None");
        auth_token = prefs.getString("auth_token", "None");
    }

    @Override
    public void onResume(){
        Log.i(LOG_TAG, "Inside resume of main activity");

        if(auth_token.equals("None")) {
        } else {
        }

        super.onResume();
    }

    @Override
    public void onPause(){

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("auth_token", auth_token);
        editor.putString("email", email);
        editor.putString("username", username);
        editor.commit();

        super.onPause();
    }

    public void linkedList(View v){
        Context context = getApplicationContext();
        CharSequence text = "";
        int duration = Toast.LENGTH_SHORT;

        Bundle id = new Bundle();
        id.putString("auth_token", auth_token);

        Intent intent = new Intent(this, ListActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);

        int len = editText.getText().length();

        if(len == 0){
            text = "Please Fill Out Each Field!";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else{
            startActivity(intent);//pass the cuisine to the search activity for searching
        }
    }
}

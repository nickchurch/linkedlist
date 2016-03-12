package a342com.linkedlist;

import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = "My log tag";

    public static String username = "";
    public static String email = "";
    public static String auth_token = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //test
    }

    @Override
    public void onResume(){
        Log.i(LOG_TAG, "Inside resume of main activity");

        if(auth_token.equals("None")) {
            //toast bad login
            //clear text boxes
        } else {
            //go to activity_list
        }


        super.onResume();
    }

    @Override
    public void onPause(){
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
            intent.putExtra(email, editText.getText().toString());
            intent.putExtra(username, editText.getText().toString());
            intent.putExtras(id);
            startActivity(intent);//pass the cuisine to the search activity for searching
        }
    }

    public void click_login_login(View v) {
        String email = ((EditText) findViewById(R.id.login_edt_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.login_edt_password)).getText().toString();

        if ((email.length() == 0) || (password.length() == 0)) {
            //toast "enter stuff"
        } else {
            //send http:login
            //if auth_token == none
                //toast "invalid login"
            //else
                //store email, password, auth_token
                //go to ListActivity
        }
    }

    public void click_login_create(View v) {
        findViewById(R.id.layout_login).setVisibility(View.GONE);

    }

}

package a342com.linkedlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = "My log tag";
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    public static LoginService login_service;

    public static String username = "";
    public static String email = "";
    public static String session_api_key = "";
    public static String password = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, 0);
        username = prefs.getString("username", "");
        email = prefs.getString("email", "");
        session_api_key = prefs.getString("session_api_key", "");
        password = prefs.getString("password", "");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "Checking for previous login:" + session_api_key);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, 0);
        username = prefs.getString("username", "");
        email = prefs.getString("email", "");
        session_api_key = prefs.getString("session_api_key", "");
        password = prefs.getString("password", "");

        /* TODO: need machanism to allow them to log out
        if(!session_api_key.equals("")) {
            goto_ListActivity();
        */
        create_login(new View(getApplicationContext()));

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
        login_service = retrofit.create(LoginService.class);
    }

    @Override
    public void onPause() {

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("session_api_key", session_api_key);
        editor.putString("email", email);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();

        super.onPause();
    }

    public void login_create(View v) {
        findViewById(R.id.layout_login).setVisibility(View.GONE);
        findViewById(R.id.layout_create).setVisibility(View.VISIBLE);

        ((EditText) findViewById(R.id.login_edt_password)).setText("");
        ((EditText) findViewById(R.id.login_edt_email)).setText("");
        ((EditText) findViewById(R.id.create_edt_nickname)).setText("");
        ((EditText) findViewById(R.id.create_edt_email)).setText("");
        ((EditText) findViewById(R.id.create_edt_password)).setText("");
    }

    public void create_login(View v) {
        findViewById(R.id.layout_login).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_create).setVisibility(View.GONE);

        ((EditText) findViewById(R.id.login_edt_password)).setText("");
        ((EditText) findViewById(R.id.login_edt_email)).setText("");
        ((EditText) findViewById(R.id.create_edt_nickname)).setText("");
        ((EditText) findViewById(R.id.create_edt_email)).setText("");
        ((EditText) findViewById(R.id.create_edt_password)).setText("");
    }

    public void login_login(View v) {

        email = ((EditText) findViewById(R.id.login_edt_email)).getText().toString();
        password = ((EditText) findViewById(R.id.login_edt_password)).getText().toString();

        if ((email.length() == 0) || (password.length() == 0)) {
            Toast.makeText(
                    getApplicationContext(),
                    "Please fill out all fields",
                    Toast.LENGTH_LONG)
                    .show();
        } else {

            Call<LoginResponse> call = login_service.login_user(new LoginRequest(email, password));

            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Response<LoginResponse> response) {
                    if (response.isSuccess()) {
                        LoginResponse resp = response.body();

                        username = resp.username;
                        session_api_key = resp.session_api_key;

                        Toast.makeText(
                                getApplicationContext(),
                                "Logged in as " + username + "\n" + session_api_key,
                                Toast.LENGTH_LONG)
                                .show();

                        goto_ListActivity();

                    } else {
                        //TODO: error
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Could not connect to server!\n\n"
                                    + "MainActivity().LoginResponse.onFailure():\n"
                                    + t.toString(),
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    public void create_create(View v) {
        email = ((EditText) findViewById(R.id.create_edt_email)).getText().toString();
        password = ((EditText) findViewById(R.id.create_edt_password)).getText().toString();
        username = ((EditText) findViewById(R.id.create_edt_nickname)).getText().toString();

        if ((email.length() == 0) || (password.length() == 0) || (username.length() == 0)) {
            Toast.makeText(
                    getApplicationContext(),
                    "Please fill out all fields",
                    Toast.LENGTH_LONG)
                    .show();
        } else {

            Call<CreateResponse> call = login_service.create_user(new CreateRequest(username, email, password));

            call.enqueue(new Callback<CreateResponse>() {
                @Override
                public void onResponse(Response<CreateResponse> response) {
                    if (response.isSuccess()) {
                        CreateResponse resp = response.body();

                        session_api_key = resp.session_api_key;

                        Toast.makeText(
                                getApplicationContext(),
                                "Created account " + username + " and logged in\n" + session_api_key,
                                Toast.LENGTH_LONG)
                                .show();

                        goto_ListActivity();

                    } else {
                        //TODO: error
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Could not connect to server!\n\n"
                                    + "MainActivity().LoginResponse.onFailure():\n"
                                    + t.toString(),
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    public void goto_ListActivity() {
        //TODO: check values?
        Toast.makeText(
                getApplicationContext(),
                "session_api_key=" + session_api_key
                        + "\nemail=" + email
                        + "\nusername=" + username
                        + "\npassword=" + password,
                Toast.LENGTH_LONG)
                .show();

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("session_api_key", session_api_key);
        editor.putString("email", email);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();

        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public interface LoginService {
        @POST("user/login")
        Call<LoginResponse> login_user(@Body LoginRequest body);

        @POST("user/createaccount")
        Call<CreateResponse> create_user(@Body CreateRequest body);
    }
}

class LoginResponse {
    public String session_api_key;
    public String username;

    LoginResponse(String _session_api_key, String _username) {
        this.session_api_key = _session_api_key;
        this.username = _username;
    }
}

class LoginRequest {
    public String email_address;
    public String password;

    LoginRequest(String _email_address, String _password) {
        this.email_address = _email_address;
        this.password = _password;
    }
}

class CreateResponse {
    public String session_api_key;

    CreateResponse(String _session_api_key) {
        this.session_api_key = _session_api_key;
    }
}

class CreateRequest {
    public String username;
    public String email_address;
    public String password;
    public String password_conf;

    CreateRequest(String _username, String _email, String _password) {
        this.username = _username;
        this.email_address = _email;
        this.password = _password;
        this.password_conf = _password;
    }
}
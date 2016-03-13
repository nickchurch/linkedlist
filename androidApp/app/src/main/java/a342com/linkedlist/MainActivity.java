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

public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = "My log tag";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public static String nickname = "";
    public static String email = "";
    public static String auth_token = "";
    public static String password = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, 0);
        nickname = prefs.getString("username", "");
        email = prefs.getString("email", "");
        auth_token = prefs.getString("auth_token", "");
        password = prefs.getString("password", "");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(LOG_TAG, "Checking for previous login:" + auth_token);
        if(!auth_token.equals("")) {
            goto_ListActivity();
        }
    }

    @Override
    public void onPause(){

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("auth_token", auth_token);
        editor.putString("email", email);
        editor.putString("username", nickname);
        editor.putString("password", password);
        editor.commit();

        super.onPause();
    }

    public void login_login(View v) {login_login();}
    public void login_login(){

        email = ((EditText) findViewById(R.id.login_edt_email)).getText().toString();
        password = ((EditText) findViewById(R.id.login_edt_password)).getText().toString();

        if ((email.length() == 0) || (password.length() == 0)) {
            Toast.makeText(
                    getApplicationContext(),
                    "Please fill out all fields",
                    Toast.LENGTH_LONG)
                    .show();
        } else {
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
            LoginService login_service = retrofit.create(LoginService.class);
            Call<LoginResponse> queryLogin = login_service.LoginResponse(email, password);

            queryLogin.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Response<LoginResponse> response) {
                    if (response.body() != null && response.body().result.equals("ok")) {
                        if (response.body().authTok.equals("None")) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Invalid email address or password\nPlease try again.",
                                    Toast.LENGTH_LONG)
                                    .show();
                            ((EditText) findViewById(R.id.login_edt_password)).setText("");
                        } else {
                            auth_token = response.body().authTok;
                            goto_ListActivity();
                        }
                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                "Bad server response!\n\n"
                                        + "MainActivity().LoginResponse.onFailure():\n"
                                        + response.raw().code(),
                                Toast.LENGTH_LONG)
                                .show();
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


            //if auth_token == none
            //toast "invalid login"
            //else
            //store email, password, auth_token

        }
    }

    public void login_create(View v) {
        findViewById(R.id.layout_login).setVisibility(View.GONE);
        findViewById(R.id.layout_create).setVisibility(View.VISIBLE);
    }

    public void create_create(View v) {
        email = ((EditText) findViewById(R.id.create_edt_email)).getText().toString();
        password = ((EditText) findViewById(R.id.create_edt_password)).getText().toString();
        nickname = ((EditText) findViewById(R.id.create_edt_nickname)).getText().toString();

        if ((email.length() == 0) || (password.length() == 0) || (nickname.length() == 0)) {
            Toast.makeText(
                    getApplicationContext(),
                    "Please fill out all fields",
                    Toast.LENGTH_LONG)
                    .show();
        } else {
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
            LoginService login_service = retrofit.create(LoginService.class);
            Call<LoginResponse> queryLogin = login_service.CreateResponse(email, password, password, nickname);

            queryLogin.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Response<LoginResponse> response) {
                    if (response.body() != null && response.body().result.equals("ok")) {
                        if (response.body().authTok.equals("None")) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Invalid email address or password\nPlease try again.",
                                    Toast.LENGTH_LONG)
                                    .show();
                            ((EditText) findViewById(R.id.login_edt_password)).setText("");
                        } else {
                            auth_token = response.body().authTok;
                            goto_ListActivity();
                        }
                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                "Bad server response!\n\n"
                                        + "MainActivity().LoginResponse.onFailure():\n"
                                        + response.raw().code(),
                                Toast.LENGTH_LONG)
                                .show();
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
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("auth_token", auth_token);
        editor.putString("email", email);
        editor.putString("username", nickname);
        editor.putString("password", password);
        editor.commit();

        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public interface LoginService {
        @GET("user/login")
        Call<LoginResponse> LoginResponse(@Query("email") String email,
                                           @Query("password") String password);
        @GET("user/createaccount")
        Call<LoginResponse> CreateResponse(@Query("email_address") String email,
                                          @Query("password") String password,
                                          @Query("password_conf") String password_conf,
                                          @Query("username") String nickname);
    }
}

class LoginResponse {
    @SerializedName("result")
    @Expose
    public String result;
    @SerializedName("session_api_key")
    @Expose
    public String authTok;
}
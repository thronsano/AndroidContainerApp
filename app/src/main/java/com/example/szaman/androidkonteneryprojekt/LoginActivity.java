package com.example.szaman.androidkonteneryprojekt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText passwordField;
    private CheckBox keepLoggedIn;

    private LoginManager loginManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize variables
        loginManager = new LoginManager(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        //Check if there's a user who has checked "Do not log out" option
        if (loginManager.loggedUser() != null)
            loggedIn(loginManager.loggedUser(), false);

        //Find references to the input fields
        usernameField = findViewById(R.id.login);
        passwordField = findViewById(R.id.password);
        keepLoggedIn = findViewById(R.id.keepLoggedIn);

        ImageButton button_en = findViewById(R.id.language_en);
        ImageButton button_pl = findViewById(R.id.language_pl);

        //Add change language listeners to the flag buttons
        button_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLangRecreate("en");
                Intent refresh = new Intent(LoginActivity.this, LoginActivity.class);
                startActivity(refresh);
                finish();
            }
        });

        button_pl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLangRecreate("pl");
                Intent refresh = new Intent(LoginActivity.this, LoginActivity.class);
                startActivity(refresh);
                finish();
            }
        });
    }

    /**
     * Method used to update current locale settings in order to change the applications language
     *
     * @param langval code for the language to which the app should switch
     */
    private void setLangRecreate(String langval) {
        Configuration configuration = getBaseContext().getResources().getConfiguration();

        Locale locale = new Locale(langval);
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        recreate();
    }

    /**
     * Clears the values input into activity fields and opens RegisterActivity in order to preform registration
     * @param view view passed from the xml call
     */
    public void register(View view) {
        usernameField.setText("");
        passwordField.setText("");
        Intent loginActivity = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(loginActivity);
    }

    /**
     * Verifies input username and password via Login Manager instance and then calls loggedIn method if everything was successful or returns
     * @param view view passed from the xml call
     */
    public void login(View view) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        Boolean correct = true;

        if (!loginManager.verifyUsername(username)) {
            usernameField.setError(getString(R.string.username_error));
            correct = false;
        }

        if (!loginManager.verifyPassword(password)) {
            passwordField.setError(getString(R.string.password_error));
            correct = false;
        }

        if (correct) {
            if (loginManager.verifyCredentials(username, password)) {
                if (keepLoggedIn.isChecked())
                    loginManager.rememberUser(username);

                loggedIn(username, true);
            } else
                Toast.makeText(this, getString(R.string.username_incorrect), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the login was successful, updates locale if necessary for logged user and opens Home Activity
     * @param user user who has logged in
     * @param changeLang boolean used to describe whether to force change app's language or leave it as it is
     */
    private void loggedIn(String user, boolean changeLang) {
        String userLang = sharedPreferences.getString(loginManager.loggedUser() + "_LANG", null);
        String currentLang = getBaseContext().getResources().getConfiguration().locale.getLanguage();

        if (changeLang)
            editor.putString(user + "_LANG", currentLang).commit();
        else {
            if (userLang != null && !userLang.equals(currentLang))
                setLangRecreate(userLang);
            else if (userLang == null)
                editor.putString(user + "_LANG", currentLang).commit();
        }

        Toast.makeText(this, getString(R.string.successful_login), Toast.LENGTH_SHORT).show();

        Intent homeActivity = new Intent(LoginActivity.this, HomeActivity.class);
        homeActivity.putExtra("USERNAME", user);
        startActivity(homeActivity);
        finish();
    }
}

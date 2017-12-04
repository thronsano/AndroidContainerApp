package com.example.szaman.androidkonteneryprojekt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    EditText usernameField;
    EditText passwordField;
    CheckBox keepLoggedIn;
    LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginManager = new LoginManager(this);

        if (loginManager.loggedUser() != null)
            loggedIn(loginManager.loggedUser());

        usernameField = (EditText) findViewById(R.id.login);
        passwordField = (EditText) findViewById(R.id.password);
        keepLoggedIn = (CheckBox) findViewById(R.id.keepLoggedIn);
    }

    public void register(View view) {
        usernameField.setText("");
        passwordField.setText("");
        Intent loginActivity = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(loginActivity);
    }

    public void login(View view) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        Boolean correct = true;

        if (!loginManager.verifyUsername(username)) {
            usernameField.setError("Username must be 5-15 alphanumerical note");
            correct = false;
        }

        if (!loginManager.verifyPassword(password)) {
            passwordField.setError("Password must be 5-30 chars, at least 1 upper and lowercase letter!");
            correct = false;
        }

        if (correct) {
            if (loginManager.verifyCredentials(username, password)) {
                if (keepLoggedIn.isChecked())
                    loginManager.rememberUser(username);

                loggedIn(username);
            } else
                Toast.makeText(this, "Username or password incorrect!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loggedIn(String user) {
        Toast.makeText(this, "Successful login!", Toast.LENGTH_SHORT).show();

        Intent homeActivity = new Intent(LoginActivity.this, HomeActivity.class);
        homeActivity.putExtra("USERNAME", user);
        startActivity(homeActivity);
        finish();
    }
}

package com.example.szaman.androidkonteneryprojekt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    EditText usernameField, passwordField, emailField;
    LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginManager = new LoginManager(this);
        usernameField = (EditText) findViewById(R.id.username);
        passwordField = (EditText) findViewById(R.id.password);
        emailField = (EditText) findViewById(R.id.email);
    }

    public void goBackToLogin(View view) {
        finish();
    }

    public void register(View view) {
        String username = usernameField.getText().toString();
        String email = emailField.getText().toString();
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

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("E-mail address incorrect!");
            correct = false;
        }

        if (correct) {
            if (loginManager.addUser(username, password, email))
                registered();
            else
                Toast.makeText(this, "Username already taken!", Toast.LENGTH_SHORT).show();

        }
    }

    private void registered() {
        Toast.makeText(this, "Successful registration!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

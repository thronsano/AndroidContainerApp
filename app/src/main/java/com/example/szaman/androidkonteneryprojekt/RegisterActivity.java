package com.example.szaman.androidkonteneryprojekt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    EditText usernameField, passwordField, emailField, confirmPasswordField;
    LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginManager = new LoginManager(this);
        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirm_password);
        emailField = findViewById(R.id.email);
    }

    public void goBackToLogin(View view) {
        finish();
    }

    public void register(View view) {
        String username = usernameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassowrd = confirmPasswordField.getText().toString();
        Boolean correct = true;

        if (!loginManager.verifyUsername(username)) {
            usernameField.setError(getString(R.string.username_error));
            correct = false;
        }

        if (!loginManager.verifyPassword(password)) {
            passwordField.setError(getString(R.string.password_error));
            correct = false;
        }

        if (!password.equals(confirmPassowrd)) {
            confirmPasswordField.setError(getString(R.string.password_match_error));
            correct = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError(getString(R.string.email_error));
            correct = false;
        }

        if (correct) {
            if (loginManager.addUser(username, password, email))
                registered();
            else
                Toast.makeText(this, getString(R.string.username_taken), Toast.LENGTH_SHORT).show();

        }
    }

    private void registered() {
        Toast.makeText(this, getString(R.string.successful_registration), Toast.LENGTH_SHORT).show();
        finish();
    }
}

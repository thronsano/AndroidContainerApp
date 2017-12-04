package com.example.szaman.androidkonteneryprojekt;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.regex.Pattern;

/**
 * Created by Szaman on 25.11.2017.
 */

public class LoginManager {
    private static final String USERNAME_PATTERN = "^[A-Za-z\\d]{5,15}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d$@$!%<>*#?&]{5,30}$"; //Min 5 characters, max 30, at least one letter and one number

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int usersAmount = 0;
    private Pattern usernamePattern, passwordPattern;

    public LoginManager(Context context) {
        sharedPreferences = context.getSharedPreferences("com.example.szaman.androidkonteneryprojekt", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        usernamePattern = Pattern.compile(USERNAME_PATTERN);
        passwordPattern = Pattern.compile(PASSWORD_PATTERN);
    }

    private void ClearDatabase() {
        editor.clear();
        editor.commit();
        usersAmount = 0;
    }

    public String loggedUser() {
        return sharedPreferences.getString("loggedUser", null);
    }

    public void rememberUser(String username) {
        editor.putString("loggedUser", username);
        editor.commit();
    }

    public boolean verifyUsername(String username) {
        return usernamePattern.matcher(username).matches();
    }

    public boolean verifyPassword(String password) {
        return passwordPattern.matcher(password).matches();
    }

    public boolean addUser(String username, String password, String email) {
        String usernameTemp;
        usersAmount = sharedPreferences.getInt("usersAmount", 0);

        for (int i = 0; i < usersAmount; i++) { //Check if username taken
            usernameTemp = sharedPreferences.getString("Username" + i, "");
            if (username.equals(usernameTemp))
                return false;
        }

        editor.putString("Username" + usersAmount, username);
        editor.putString("Password" + usersAmount, password);
        editor.putString("Email" + usersAmount, email);

        usersAmount++;
        editor.putInt("usersAmount", usersAmount);
        editor.commit();

        return true;
    }

    public boolean verifyCredentials(String username, String password) {
        String usernameTemp, passwordTemp;
        usersAmount = sharedPreferences.getInt("usersAmount", 0);

        for (int i = 0; i < usersAmount; i++) {
            usernameTemp = sharedPreferences.getString("Username" + i, "");
            passwordTemp = sharedPreferences.getString("Password" + i, "");
            if (username.equals(usernameTemp)) { //IF USERNAME FOUND
                if (password.equals(passwordTemp))
                    return true;
                return false;
            }
        }
        return false;
    }
}

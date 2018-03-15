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
    private Pattern usernamePattern, passwordPattern;

    LoginManager(Context context) {
        sharedPreferences = context.getSharedPreferences("com.example.szaman.androidkonteneryprojekt", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        usernamePattern = Pattern.compile(USERNAME_PATTERN);
        passwordPattern = Pattern.compile(PASSWORD_PATTERN);
    }

    /**
     * Method used to clear the Shared Preferences completely in order to test new features
     */
    private void clearDatabase() {
        editor.clear();
        editor.commit();
    }

    /**
     * Returns username that has been save after checking "Do not log out" option
     * @return username of the user who's checked "Do not log out"
     */
    String loggedUser() {
        return sharedPreferences.getString("LOGGED_USER", null);
    }

    /**
     * Saves username supplied in params in order to skip verification process
     * @param username user to remember
     */
    void rememberUser(String username) {
        editor.putString("LOGGED_USER", username).commit();
    }

    /**
     * Erase user that has been saved in order to skip verification process
     */
    void forgetUser() {
        editor.remove("LOGGED_USER").commit();
    }

    /**
     * Find the index of the provided user
     * @param username username of the searched user
     * @return index of the provided user
     */
    private int findUserIndex(String username) {
        int usersAmount = sharedPreferences.getInt("USERS_AMOUNT", 0);
        String usernameTemp;

        for (int i = 0; i < usersAmount; i++) {
            usernameTemp = sharedPreferences.getString("USERNAME_" + i, "");
            if (username.equals(usernameTemp))
                return i;
        }

        return -1;
    }

    /**
     * Change user's password
     * @param username of the user that is supposed to have the password changed
     * @param newPassword new password of the said user
     * @return true/false depending of whether the password change was successful
     */
    boolean changePassword(String username, String newPassword) {
        if (!verifyPassword(newPassword)) //Password doesn't match the pattern
            return false;

        int userID = findUserIndex(username);
        editor.putString("PASSWORD_" + userID, newPassword).commit();

        return true;
    }

    /**
     * Removes user and all this data from the database
     * @param username user to be deleted
     */
    void deleteUser(String username) {
        String usernameTemp, passwordTemp, emailTemp;
        int usersAmount = sharedPreferences.getInt("USERS_AMOUNT", 0);
        int userID = findUserIndex(username);

        if (userID >= 0) {
            editor.remove("USERNAME_" + userID);
            editor.remove("PASSWORD_" + userID);

            //Shift users in Shared Preferences
            for (int i = userID + 1; i < usersAmount; i++) {
                usernameTemp = sharedPreferences.getString("USERNAME_" + i, "");
                passwordTemp = sharedPreferences.getString("PASSWORD_" + i, "");
                emailTemp = sharedPreferences.getString("EMAIL_" + i, "");

                editor.putString("USERNAME_" + (i - 1), usernameTemp);
                editor.putString("PASSWORD_" + (i - 1), passwordTemp);
                editor.putString("EMAIL_" + (i - 1), emailTemp);
            }

            editor.putInt("USERS_AMOUNT", --usersAmount);
            editor.commit();
        }
    }

    /**
     * Checks if the username fits the regex criteria
     * @param username username to check
     * @return correct or not
     */
    boolean verifyUsername(String username) {
        return usernamePattern.matcher(username).matches();
    }

    /**
     * Checks if the password fits the regex criteria
     * @param password password to check
     * @return correct or not
     */
    boolean verifyPassword(String password) {
        return passwordPattern.matcher(password).matches();
    }

    /**
     * Adds user to the database
     * @param username user's username
     * @param password user's password
     * @param email user's email address
     * @return operation successful or not
     */
    boolean addUser(String username, String password, String email) {
        String usernameTemp;
        int usersAmount = sharedPreferences.getInt("USERS_AMOUNT", 0);

        if (findUserIndex(username) >= 0) //Username taken
            return false;

        editor.putString("USERNAME_" + usersAmount, username);
        editor.putString("PASSWORD_" + usersAmount, password);
        editor.putString("EMAIL_" + usersAmount, email);

        usersAmount++;
        editor.putInt("USERS_AMOUNT", usersAmount);
        editor.commit();

        return true;
    }

    /**
     * Check whether provided user's credentials are correct
     * @param username user's username
     * @param password user's password
     * @return username/password match
     */
    boolean verifyCredentials(String username, String password) {
        int userID = findUserIndex(username);

        if (userID < 0) //User with selected name doesn't exist
            return false;
        else if (password.equals(sharedPreferences.getString("PASSWORD_" + userID, ""))) //User password match
            return true;
        else //Password doesn't match
            return false;
    }
}

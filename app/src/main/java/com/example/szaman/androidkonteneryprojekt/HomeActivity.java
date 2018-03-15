package com.example.szaman.androidkonteneryprojekt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {
    private static final String CONTAINER_NAME_PATTERN = "^[\\S][A-Za-z\\s\\d]{1,18}[\\S]$";

    private String currentUser;
    private boolean ascending = false;
    private List<String> containerList;
    private GridView gridView;
    private CustomGridAdapter customGridAdapter;
    private Pattern containerNamePattern;

    private LoginManager loginManager;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.d1);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        setSupportActionBar(toolbar);

        //Initialize variables
        FloatingActionButton addContainerBtn = findViewById(R.id.addContainerBtn);
        loginManager = new LoginManager(this);
        containerNamePattern = Pattern.compile(CONTAINER_NAME_PATTERN);

        sharedPreferences = getSharedPreferences("com.example.szaman.androidkonteneryprojekt", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        currentUser = getIntent().getStringExtra("USERNAME");
        containerList = Util.asSortedList(sharedPreferences.getStringSet(currentUser, null)); //load saved containerList and display as sorted list

        gridView = findViewById(R.id.gridView);
        customGridAdapter = new CustomGridAdapter(this, containerList);
        gridView.setAdapter(customGridAdapter);

        //ADD "HELLO USER" TEXT IN SIDE BAR
        NavigationView navigationView = findViewById(R.id.nav_view);
        TextView welcomeUserTxt = navigationView.getHeaderView(0).findViewById(R.id.drawer_welcome_user);
        String welcomeTxt = getString(R.string.welcome) + " " + currentUser + "!";
        welcomeUserTxt.setText(welcomeTxt);

        //SORT CLICK
        ImageButton sortButton = findViewById(R.id.sort_button);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortContainersList();
            }
        });

        //GRIDVIEW CLICK
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openContainer(gridView.getAdapter().getItem(i).toString());
            }
        });

        //GRIDVIEW LONG PRESS
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayContainerEditPrompt(i);
                return true;
            }
        });

        //ADD CONTAINER CLICK
        addContainerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //Add container_list_element button
                displayContainerCreatePrompt();
            }
        });

        //ADD DRAWER MENU ARROW
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //DRAWER MENU CLICK
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.change_password)
                    displayChangePasswordPrompt();
                else if (id == R.id.sign_out)
                    logout();
                else if (id == R.id.delete_account)
                    displayDeleteAccountPrompt();//deleteAccount();
                else if (id == R.id.reminder)
                    displayChooseReminderPrompt();

                return true;
            }
        });

        //Pop up a prompt if the user selected to remind him of items to restock
        if ("REMIND".equals(chosenRemindOption()))
            displayRemindingPrompt();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Method returns all elements defined inside every container that have amount equal to zero.
     *
     * @return an Array consisting of strings formed by following scheme: "ITEM_NAME    IN CONTAINER    CONTAINER_NAME"
     */
    private ArrayList<String> itemsToRestock() {
        ArrayList<String> restockItems = new ArrayList<>();

        //Initalize elementActivity to fetch elements from containers
        ElementActivity elementActivity = new ElementActivity();
        elementActivity.sharedPreferences = getSharedPreferences("com.example.szaman.androidkonteneryprojekt", Context.MODE_PRIVATE);

        //Find zeroed elements in each container
        for (String container : containerList) {
            List<ElementActivity.Element> tempElementArray = elementActivity.decodeList(currentUser, container);

            for (ElementActivity.Element el : tempElementArray)
                if (el.amount == 0) {
                    String name = el.name + "\t" + container;
                    restockItems.add(name);
                }

        }

        return restockItems;
    }

    /**
     * Method which fetches currently selected remind option from Shared Preferences
     *
     * @return currently set remind option, null if missing
     */
    private String chosenRemindOption() {
        String action = sharedPreferences.getString(currentUser + "_ACTION", null);
        return action == null ? "" : action;
    }

    /**
     * Displays a prompt stating which elements(if there are any) need restocking (their amount is equal to zero)
     */
    private void displayRemindingPrompt() {
        ArrayList<String> itemsToRestock = itemsToRestock();

        if (itemsToRestock.size() == 0)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogueTheme);
        final View view = this.getLayoutInflater().inflate(R.layout.restock_reminder_prompt, null);
        builder.setView(view);

        final TextView elementField = view.findViewById(R.id.elementFieldPrompt);
        final TextView containerField = view.findViewById(R.id.containerFieldPrompt);

        String itemsRestockElements = "";
        String itemsRestockContainers = "";

        for (String element : itemsToRestock) {
            String[] splitData = element.split("\\t");
            itemsRestockElements += splitData[0] + "\n";
            itemsRestockContainers += splitData[1] + "\n";
        }

        elementField.setText(itemsRestockElements);
        containerField.setText(itemsRestockContainers);

        //Create the prompt window
        builder.setTitle(getString(R.string.reminder))
                .setMessage(getString(R.string.zeroed_elements_reminder))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog b = builder.create();
        b.show();
    }

    /**
     * Displays a prompt asking for action that is to be taken if the quantity of an element reaches zero
     */
    private void displayChooseReminderPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogueTheme);

        //Add new fields to default theme
        View view = this.getLayoutInflater().inflate(R.layout.reminder_add_prompt, null);
        builder.setView(view);

        String chosenRemind = chosenRemindOption();

        final RadioGroup rg = view.findViewById(R.id.all_options);
        final RadioButton r1 = view.findViewById(R.id.delete_element);
        final RadioButton r2 = view.findViewById(R.id.remind_restock);
        final RadioButton r3 = view.findViewById(R.id.leave_zero);

        //Check currently selected option
        if (chosenRemind.equals("DELETE"))
            r1.setChecked(true);
        else if (chosenRemind.equals("REMIND"))
            r2.setChecked(true);
        else if (chosenRemind.equals("LEAVE"))
            r3.setChecked(true);

        //Create the prompt window
        builder.setTitle(getString(R.string.reminder))
                .setMessage(getString(R.string.add_reminder_prompt_msg))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int choiceID = rg.getCheckedRadioButtonId();

                        if (choiceID == R.id.delete_element)
                            editor.putString(currentUser + "_ACTION", "DELETE").commit();
                        else if (choiceID == R.id.remind_restock)
                            editor.putString(currentUser + "_ACTION", "REMIND").commit();
                        else if (choiceID == R.id.leave_zero)
                            editor.putString(currentUser + "_ACTION", "LEAVE").commit();

                        Snackbar.make(findViewById(R.id.home_layout), getString(R.string.reminder_saved), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        AlertDialog b = builder.create();
        b.show();
    }

    /**
     * Displays a prompt asking for current password as verification and then for a new one in order to change the current user's credentials
     */
    private void displayChangePasswordPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogueTheme);

        final View view = this.getLayoutInflater().inflate(R.layout.change_password_prompt, null);
        builder.setView(view);

        final EditText passwordField = view.findViewById(R.id.password_field);
        final EditText newPasswordField = view.findViewById(R.id.new_password);
        final EditText confirmField = view.findViewById(R.id.confirm_password);

        builder.setTitle(R.string.change_password)
                .setMessage(R.string.change_password_msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String password = passwordField.getText().toString();
                        String passwordNew = newPasswordField.getText().toString();
                        String passwordConfirm = confirmField.getText().toString();

                        if (loginManager.verifyCredentials(currentUser, password))
                            if (loginManager.verifyPassword(passwordNew))
                                if (passwordNew.equals(passwordConfirm)) {
                                    loginManager.changePassword(currentUser, passwordNew);
                                    Snackbar.make(findViewById(R.id.home_layout), getString(R.string.password_changed), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                } else
                                    Snackbar.make(findViewById(R.id.home_layout), getString(R.string.password_match_error), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            else
                                Snackbar.make(findViewById(R.id.home_layout), getString(R.string.password_error), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        else
                            Snackbar.make(findViewById(R.id.home_layout), getString(R.string.password_incorrect), Snackbar.LENGTH_LONG).setAction("Action", null).show();


                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog b = builder.create();
        b.show();
    }

    /**
     * Displays a prompt asking for user's password and them proceeds to delete his account
     */
    private void displayDeleteAccountPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogueTheme);

        View view = this.getLayoutInflater().inflate(R.layout.container_add_prompt, null);
        builder.setView(view);

        final EditText passwordField = view.findViewById(R.id.nameField);
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setTitle(R.string.delete_account_title)
                .setMessage(R.string.delete_account_msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String password = passwordField.getText().toString();

                        if (loginManager.verifyCredentials(currentUser, password))
                            deleteAccount();
                        else
                            Snackbar.make(findViewById(R.id.home_layout), getString(R.string.password_incorrect), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog b = builder.create();
        b.show();
    }

    /**
     * Displays a prompt to get new name needed to edit an existing container defined by an index supplied in params
     *
     * @param index index of the element that is to be edited
     */
    private void displayContainerEditPrompt(final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogueTheme);

        View view = this.getLayoutInflater().inflate(R.layout.container_add_prompt, null);
        builder.setView(view);

        final EditText containerName = view.findViewById(R.id.nameField);
        containerName.setText(containerList.get(index));
        containerName.setSelection(containerName.getText().length());

        builder.setTitle(R.string.container_name_prompt_title)
                .setMessage(R.string.name_must_be_unique_to_edit)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = containerName.getText().toString();

                        if (containerNamePattern.matcher(name).matches() && (!containerList.contains(name) || containerList.get(index).equals(name)))
                            editContainer(index, name);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteContainer(index);
                        saveContainersList();
                        refreshGridViewList();
                        Snackbar.make(findViewById(R.id.home_layout), getString(R.string.container_deleted), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                });


        AlertDialog b = builder.create();
        b.show();
    }

    /**
     * Displays a prompt to get name needed to create a new container and then creates it
     */
    private void displayContainerCreatePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogueTheme);

        View view = this.getLayoutInflater().inflate(R.layout.container_add_prompt, null);
        builder.setView(view);

        final EditText nameField = view.findViewById(R.id.nameField);

        builder.setTitle(R.string.container_name_prompt_title)
                .setMessage(R.string.add_prompt_msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = nameField.getText().toString();

                        if (containerNamePattern.matcher(name).matches() && !containerList.contains(name))
                            createContainer(name);


                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog b = builder.create();
        b.show();
    }

    /**
     * Method that deletes container specified by index in param from Shared Preferences and runtime containers array
     *
     * @param index index of the element that is to be deleted
     */
    private void deleteContainer(int index) {
        editor.remove(currentUser + "_" + containerList.get(index)).commit();
        containerList.remove(index);
    }

    /**
     * Removes the old element from Shared Preferences and runtime containers array then proceeds to add a new one with edited values.
     * In case the element values haven't been altered it skips the operation.
     *
     * @param index index of the element that is to be edited
     * @param name  new name for the edited element
     */
    private void editContainer(int index, String name) {
        deleteContainer(index);
        containerList.add(name);

        saveContainersList();
        refreshGridViewList();
    }

    /**
     * Function used to create a container from supplied parameters that later stores in runtime containers array and Shared Preferences.
     *
     * @param name name of the container that is to be created
     */
    private void createContainer(String name) {
        containerList.add(containerList.size(), name);
        refreshGridViewList();
        saveContainersList();
    }

    /**
     * Sorts runtime list of elements based on it's alphabetical order or reverse of it
     */
    private void sortContainersList() {
        if (ascending)
            Collections.sort(containerList);
        else
            Collections.reverse(containerList);

        customGridAdapter.notifyDataSetChanged();
        ascending = !ascending;
    }

    /**
     * Function converts the runtime containers array into a string set and then saves it in Shared Preferences
     */
    private void saveContainersList() {
        Set<String> setToSave = new HashSet<>(containerList);
        editor.putStringSet(currentUser, setToSave);
        editor.commit();
    }

    /**
     * Notifies the adapter that data set has changed in order for it to refresh it's elements and scrolls to the end of the list
     */
    private void refreshGridViewList() {
        customGridAdapter.notifyDataSetChanged();
        gridView.smoothScrollToPosition(containerList.size());
    }

    /**
     * Switches activity to ElementActivity filled with elements corresponding to container supplied in params
     *
     * @param containerName container of which the elements are to be displayed
     */
    private void openContainer(String containerName) {
        Intent containerActivity = new Intent(HomeActivity.this, ElementActivity.class);
        containerActivity.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
        containerActivity.putExtra("CONTAINER", containerName);
        startActivity(containerActivity);
    }

    /**
     * Deletes the account of currently logged user and all the information connected to him
     */
    private void deleteAccount() {
        //Erase all containers and their contains
        while (containerList.size() > 0)
            deleteContainer(0);

        //Erase container set
        editor.remove(currentUser);

        //Remove user's stored language info
        editor.remove(currentUser + "_LANG");

        //Remove user's reminder settings
        editor.remove(currentUser + "_ACTION");

        //Remove user's credentials
        loginManager.deleteUser(currentUser);

        editor.commit();
        logout();
    }

    /**
     * Method used to logout the user
     */
    private void logout() {
        loginManager.forgetUser();

        Intent loginActivity = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }

    /**
     * Custom adapter that allows to represent Containers as a square with name and a picture
     */
    private class CustomGridAdapter extends BaseAdapter {

        private Context context;
        private List<String> items;
        LayoutInflater inflater;

        public CustomGridAdapter(Context context, List<String> items) {
            this.context = context;
            this.items = items;
            inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.container_list_element, null);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.grid_container_image);
            TextView textView = (TextView) convertView.findViewById(R.id.grid_container_text);
            textView.setText(items.get(position));

            return convertView;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
    }
}

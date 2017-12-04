package com.example.szaman.androidkonteneryprojekt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {
    private static final String CONTAINER_NAME_PATTERN = "^[\\S][A-Za-z\\s\\d$@$!%<>*#?()\\:\\+\\-&]{1,18}[\\S]$";
    String loggedUser;
    List<String> containers;
    GridView gridView;
    CustomGridAdapter adapter;
    Pattern containerNamePattern;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FloatingActionButton addContainerBtn = findViewById(R.id.addContainerBtn);
        FloatingActionButton logoutBtn = findViewById(R.id.logoutBtn);
        Toolbar toolbar = findViewById(R.id.toolbar);

        //initialize
        gridView = (GridView) findViewById(R.id.gridView);
        containerNamePattern = Pattern.compile(CONTAINER_NAME_PATTERN);
        sharedPreferences = getSharedPreferences("com.example.szaman.androidkonteneryprojekt", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        loggedUser = getIntent().getStringExtra("USERNAME");
        containers = Util.asSortedList(sharedPreferences.getStringSet(loggedUser, null)); //load saved containers and display as sorted list
        adapter = new CustomGridAdapter(this, containers);

        gridView.setAdapter(adapter);
        setSupportActionBar(toolbar);

        //GRIDVIEW CLICK
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Snackbar.make(view, "Clicked " + gridView.getAdapter().getItem(i), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                openContainer(i);
            }
        });

        //ADD CONTAINER CLICK
        addContainerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //Add container button
                displayNamePrompt();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //Add container button
                logout();
            }
        });

    }

    private void logout() {
        editor.remove("loggedUser").commit();
        
        Intent loginActivity = new Intent( HomeActivity.this, LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }

    private void displayNamePrompt() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        View view = this.getLayoutInflater().inflate(R.layout.nameprompt, null);
        builder.setView(view);
        final EditText editText = (EditText) view.findViewById(R.id.nameField);

        builder.setMessage("Select a name for your container")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = editText.getText().toString();
                        if (containerNamePattern.matcher(name).matches())
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

    private void openContainer(int position) {
        Intent containerActivity = new Intent(HomeActivity.this, ContainerActivity.class);
        containerActivity.putExtra("POS", position);
        startActivity(containerActivity);
    }

    private void createContainer(String name) {
        containers.add(containers.size(), name);
        adapter.notifyDataSetChanged();
        gridView.smoothScrollToPosition(containers.size());

        Set<String> setToSave = new HashSet<>(containers);
        editor.putStringSet(getIntent().getStringExtra("USERNAME"), setToSave);
        editor.commit();
    }

    public class CustomGridAdapter extends BaseAdapter {

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
                convertView = inflater.inflate(R.layout.container, null);

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

package com.example.szaman.androidkonteneryprojekt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ElementActivity extends AppCompatActivity {

    private static final String ELEMENT_NAME_PATTERN = "^[\\S][A-Za-z\\s\\d]{1,15}[\\S]$";
    private static final String ELEMENT_UNIT_PATTERN = "[\\SA-Za-z\\d]{1,10}";

    private ListView listView;
    private List<Element> elementsList;
    private boolean ascending = false;
    private boolean ascendingNumeric = false;

    private Pattern elementsNamePattern;
    private Pattern elementsUnitPattern;

    private CustomListAdapter customListAdapter;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element);

        //Get the references
        FloatingActionButton floatingActionButton = findViewById(R.id.addContainerElementBtn);
        listView = findViewById(R.id.container_list_view);

        //Gain access to Shared Preferences
        sharedPreferences = getSharedPreferences("com.example.szaman.androidkonteneryprojekt", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Compile the regex patterns in order to validate further user input
        elementsNamePattern = Pattern.compile(ELEMENT_NAME_PATTERN);
        elementsUnitPattern = Pattern.compile(ELEMENT_UNIT_PATTERN);

        //Get the elements and display them
        elementsList = decodeList(getIntent().getStringExtra("USERNAME"), getIntent().getStringExtra("CONTAINER"));
        customListAdapter = new CustomListAdapter(this, elementsList);
        listView.setAdapter(customListAdapter);

        //Sort alphabetical order
        ImageButton sortButton = findViewById(R.id.sort_button);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortList();
            }
        });

        //Sort numeric order
        ImageButton sortButtonNumeric = findViewById(R.id.sort_number_button);
        sortButtonNumeric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortListNumeric();
            }
        });

        //Add element button clicked
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //Add container_list_element button
                displayElementCreatePrompt();
            }
        });

        //Element clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayElementEditPrompt((Element) listView.getAdapter().getItem(i)); //Snackbar.make(view, "Clicked " + ((Element) listView.getAdapter().getItem(i)).name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    /**
     * Sorts runtime list of elements based on it's alphabetical order
     */
    private void sortList() {
        if (ascending)
            Collections.sort(elementsList);
        else
            Collections.reverse(elementsList);

        customListAdapter.notifyDataSetChanged();
        ascending = !ascending;
    }

    /**
     * Sorts runtime list of elements based on their amount
     */
    private void sortListNumeric() {
        if (ascendingNumeric)
            Collections.sort(elementsList, new ElementComparator());
        else
            Collections.sort(elementsList, Collections.reverseOrder(new ElementComparator()));

        customListAdapter.notifyDataSetChanged();
        ascendingNumeric = !ascendingNumeric;
    }

    /**
     * Displays a prompt to get edited values needed to alter the supplied element
     *
     * @param editedElement element that is to be edited
     */
    private void displayElementEditPrompt(final Element editedElement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogueTheme);

        //Add new fields to default theme
        View view = this.getLayoutInflater().inflate(R.layout.element_add_prompt, null);
        builder.setView(view);

        //Get references to those fields and fill them with current values
        final EditText editTextName = view.findViewById(R.id.nameFieldElementPrompt);
        editTextName.setText(editedElement.name);
        editTextName.setSelection(editTextName.getText().length());
        final EditText editTextAmount = view.findViewById(R.id.amountFieldElementPrompt);
        editTextAmount.setText(editedElement.amount + "");
        final EditText editTextUnit = view.findViewById(R.id.unitFieldElementPrompt);
        editTextUnit.setText(editedElement.unit);

        //Create the prompt window
        builder.setTitle(getString(R.string.edit_element_prompt_title))
                .setMessage(getString(R.string.name_must_be_unique_to_edit))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = editTextName.getText().toString();
                        String amount = editTextAmount.getText().toString();
                        String unit = editTextUnit.getText().toString();

                        try {
                            int amountParsed = Integer.parseInt(amount);
                            if ((amountParsed == 0) && deleteZeroed()) {
                                deleteElement(editedElement);
                                Snackbar.make(findViewById(R.id.container_list_view), getString(R.string.delete_if_zeroed), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            } else if ((amountParsed >= 0) && elementsNamePattern.matcher(name).matches() && elementsUnitPattern.matcher(unit).matches() && (!contains(name) || editedElement.name.equals(name)))
                                //Check if input name and type are valid and if element with such name hasn't been already created (unless referring the currently edited object)
                                editElement(editedElement, name, amountParsed, unit);
                        } catch (NumberFormatException ex) {
                            //Failed to parse the amount value
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //cancel
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteElement(editedElement);
                    }
                });

        AlertDialog b = builder.create();
        b.show();
    }

    /**
     * Displays a prompt to get values needed to create a new element and then creates it
     */
    private void displayElementCreatePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogueTheme);

        //Add new fields to default theme
        View view = this.getLayoutInflater().inflate(R.layout.element_add_prompt, null);
        builder.setView(view);

        //Get references to those fields
        final EditText editTextName = view.findViewById(R.id.nameFieldElementPrompt);
        final EditText editTextAmount = view.findViewById(R.id.amountFieldElementPrompt);
        final EditText editTextUnit = view.findViewById(R.id.unitFieldElementPrompt);

        //Create the prompt window
        builder.setTitle(getString(R.string.name_must_be_unique_to_add))
                .setMessage(getString(R.string.add_prompt_msg))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = editTextName.getText().toString();
                        String amount = editTextAmount.getText().toString();
                        String textType = editTextUnit.getText().toString();

                        try {
                            int amountParsed = Integer.parseInt(amount);
                            if ((amountParsed == 0) && deleteZeroed())
                                Snackbar.make(findViewById(R.id.container_list_view), getString(R.string.delete_if_zeroed), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            else if ((amountParsed >= 0) && elementsNamePattern.matcher(name).matches() && elementsUnitPattern.matcher(textType).matches() && !contains(name))
                                //Check if input name and type are valid and if element with such a name hasn't been already created
                                createElement(name, amountParsed, textType);
                        } catch (NumberFormatException ex) {
                            //Failed to parse the amount value
                        }
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
     * Used to encode array of elements into a string array which is possible to store in Shared Preferences
     *
     * @return encoded array of String paths
     */
    private List<String> encodeList() {
        ArrayList<String> encodedList = new ArrayList<>();

        for (Element el : elementsList)
            encodedList.add(encodeElement(el));

        return encodedList;
    }

    /**
     * Generates the path used for storing element in Shared Preferences (for element supplied in parameter)
     *
     * @param el element for which we generate the path
     * @return path in sharedPreferences
     */
    private String encodeElement(Element el) {
        String path = getIntent().getStringExtra("USERNAME") + "_" + getIntent().getStringExtra("CONTAINER") + "_";

        return path + el.name + "_" + el.amount + "_" + el.unit;
    }

    /**
     * Decodes a string array from sharedPreferences into an element array
     *
     * @return array of elements previously stored in Shared Preferences
     */
    List<Element> decodeList(String username, String container) {
        List<String> encodedList = Util.asSortedList(sharedPreferences.getStringSet(username + "_" + container, null)); //load saved elements and display as sorted list
        List<Element> newList = new ArrayList<>();

        for (String element : encodedList) {
            String[] parts = element.split("_");
            newList.add(new Element(parts[2], Integer.parseInt(parts[3]), parts[4]));
        }

        return newList;
    }

    /**
     * Removes the old element from Shared Preferences and runtime elements array then proceeds to add a new one with edited values.
     * In case the element values haven't been altered it skips the operation.
     *
     * @param element element that is being edited
     * @param name    a new name for the element
     * @param amount  a new amount for the element
     * @param unit    a new type that defines amount for the element
     */
    private void editElement(Element element, String name, int amount, String unit) {
        if (element.name.equals(name) && element.amount == amount && element.unit.equals((unit)))
            return; //The element hasn't been changed, no action is necessary
        else {
            //First remove the old element
            deleteElement(element);

            //Then add the new one
            createElement(name, amount, unit);
        }
    }

    /**
     * Function used to create an element from supplied parameters that later stores in runtime elements array and Shared Preferences.
     *
     * @param name   name of the element
     * @param amount amount of the element
     * @param unit   units in which the amount is defined, eg. kilograms, meters, liters
     */
    private void createElement(String name, int amount, String unit) {
        elementsList.add(elementsList.size(), new Element(name, amount, unit));
        refreshList();
        saveElementsList();
    }

    /**
     * Method that deletes element specified in param from Shared Preferences and runtime elements array
     *
     * @param element element that is to be deleted
     */
    private void deleteElement(Element element) {
        editor.remove(encodeElement(element));
        editor.commit();
        elementsList.remove(element);

        refreshList();
    }

    /**
     * Function converts the runtime elements array into a string set and then saves it in Shared Preferences
     */
    private void saveElementsList() {
        Set<String> setToSave = new HashSet<>(encodeList());

        editor.putStringSet(getIntent().getStringExtra("USERNAME") + "_" + getIntent().getStringExtra("CONTAINER"), setToSave);
        editor.commit();
    }

    /**
     * Method checks if the supplied string is a name parameter in any of the runtime array elements
     *
     * @param name a string to be checked against elements' names
     * @return true if the string occurred and false otherwise
     */
    private boolean contains(String name) {
        for (Element el : elementsList)
            if (el.name.equals(name))
                return true;

        return false;
    }

    /**
     * Notifies the adapter that data set has changed in order for it to refresh it's elements and scrolls to the end of the list
     */
    private void refreshList() {
        customListAdapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(elementsList.size());
    }

    /**
     * Method checking Shared Preferences whether to delete elements with amount equal to zero
     *
     * @return true if Reminder DELETE was chosen, false otherwise
     */
    private boolean deleteZeroed() {
        String option = sharedPreferences.getString(getIntent().getStringExtra("USERNAME") + "_ACTION", null);
        if (option == null)
            return false;

        return option.equals("DELETE");
    }

    /**
     * Custom adapter that allows to create a list view consisting of multiple fields
     */
    private class CustomListAdapter extends BaseAdapter {

        private Context context;
        private List<Element> items;
        LayoutInflater inflater;

        public CustomListAdapter(Context context, List<Element> items) {
            this.context = context;
            this.items = items;
            inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.element_list_element, null);

            TextView itemName = convertView.findViewById(R.id.itemName);
            itemName.setText(items.get(position).name);

            TextView itemAmount = convertView.findViewById(R.id.itemAmount);
            String itemsAmountTxt = items.get(position).amount + " " + items.get(position).unit;
            itemAmount.setText(itemsAmountTxt);

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

    /**
     * Class used to compare two elements based on their amount
     */
    private class ElementComparator implements Comparator<Element> {
        @Override
        public int compare(Element element, Element t1) {
            return element.amount - t1.amount;
        }
    }

    /**
     * A class used to represent elements stored in the containers
     */
    public class Element implements Comparable<Element> {
        String name;
        int amount;
        String unit;

        public Element(String name, int amount, String unit) {
            this.name = name;
            this.amount = amount;
            this.unit = unit;
        }

        @Override
        public int compareTo(@NonNull Element o) {
            return name.compareTo(o.name);
        }
    }
}

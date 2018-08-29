package com.example.frank.shoppinglisthq;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// TODO Listen zwischen Geräten austauschen oder versenden
// TODO nächstes Geschäft auf GoogleMaps anzeigen
// TODO Lebensmittel nach verschiedenen Kriterien gruppieren
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ShoppingMemoDataSource dataSource;
    private ListView shoppingMemosListView;
    private Spinner units;
    private ArrayList<String> liste;
    private AutoCompleteTextView editTextProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        units = findViewById(R.id.spinner_unit);
        dataSource = new ShoppingMemoDataSource(this);

        initializeShoppingMemosListView();
        activateAddButton();
        initializeContextualActionBar();
    }

    private void initializeShoppingMemosListView() {
        List<ShoppingMemo> emptyListForInitialization = new ArrayList<>();
        shoppingMemosListView = findViewById(R.id.listview_shopping_memos);

        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<ShoppingMemo>(
                this, android.R.layout.simple_list_item_multiple_choice, emptyListForInitialization) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                ShoppingMemo memo = (ShoppingMemo) shoppingMemosListView.getItemAtPosition(position);
                if (memo.isChecked()) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(Color.rgb(175, 175, 175));
                } else {
                    textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(Color.DKGRAY);
                }
                return view;
            }
        };

        shoppingMemosListView.setAdapter(shoppingMemoArrayAdapter);
        shoppingMemosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingMemo memo = (ShoppingMemo) parent.getItemAtPosition(position);
                dataSource.updateShoppingMemo(memo.getId(), memo.getProduct(), memo.getQuantity(), (!memo.isChecked()));
                showAllListEntries();
            }
        });
    }

    // TODO Artikel durch Barcodescan in Liste aufnehmen
    //TODO Artikel durch Spracheingabe in Liste aufnehmen
    //TODO Liste vorlesen lassen und per Sprachbefehl abhaken
    //TODO OCR für Inhaltsstoffe
    private void activateAddButton() {
        Button buttonAddProduct = findViewById(R.id.button_add_product);
        final EditText editTextQuantity = findViewById(R.id.editText_quantity);
        editTextProduct = findViewById(R.id.editText_product);
        final EditText editTextPrice = findViewById(R.id.editText_price);

        liste = new ArrayList<>();


        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = editTextQuantity.getText().toString();
                String product = editTextProduct.getText().toString();

                int unit = units.getSelectedItemPosition() + 1;
                String priceString = editTextPrice.getText().toString();

                if (TextUtils.isEmpty(quantityString)) {
                    editTextQuantity.setError(getString(R.string.editText_errorMessage));
                    editTextQuantity.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(product)) {
                    editTextProduct.setError(getString(R.string.editText_errorMessage));
                    editTextProduct.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(priceString)) {
                    editTextPrice.setError(getString(R.string.editText_errorMessage));
                    editTextPrice.requestFocus();
                    return;
                }

                double price = Double.parseDouble(priceString);
                int quantity = Integer.parseInt(quantityString);

                HashSet<String> set = new HashSet<>();
                set.addAll(liste);
                set.add(product);
                liste.clear();
                liste.addAll(set);

                editTextProduct.setAdapter(new ArrayAdapter<String>(
                        MainActivity.this,android.R.layout.simple_list_item_1, liste));

                editTextQuantity.setText("");
                editTextProduct.setText("");
                editTextPrice.setText("");
                //TODO Wenn Artikel vorhanden, addier Mengen und update
                dataSource.createShoppingMemo(product, quantity, price, unit);

//                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                if(getCurrentFocus()!=null){
//                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
//                }
                editTextQuantity.requestFocus();
                showAllListEntries();


            }
        });
    }

    private void initializeContextualActionBar() {
        final ListView shoppingMemoListView = findViewById(R.id.listview_shopping_memos);
        shoppingMemoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        shoppingMemoListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }
                String cabTitel = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitel);
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if (selCount == 1) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                SparseBooleanArray touchedShoppingMemosPosition =
                        shoppingMemoListView.getCheckedItemPositions();
                switch (item.getItemId()) {
                    case R.id.cab_delete:
                        for (int i = 0; i < touchedShoppingMemosPosition.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPosition.keyAt(i);
                                ShoppingMemo memo = (ShoppingMemo) shoppingMemoListView
                                        .getItemAtPosition(positionInListView);
                                Log.d(LOG_TAG, "Position: " + positionInListView + " Inhalt: " +
                                        memo.toString());
                                dataSource.deleteShoppingMemo(memo);
                            }

                        }
                        showAllListEntries();
                        mode.finish();
                        return true;
                    case R.id.cab_change:
                        for (int i = 0; i < touchedShoppingMemosPosition.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPosition.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemoListView.getItemAtPosition(positionInListView);
                                AlertDialog editShoppingMemoDialog = createEditShoppingMemoDialog(shoppingMemo);
                                editShoppingMemoDialog.show();
                            }

                        }
                        mode.finish();
                        return true;
                    default:
                        return false;

                }

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;
            }
        });
    }

    private AlertDialog createEditShoppingMemoDialog(final ShoppingMemo shoppingMemo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogsView = inflater.inflate(R.layout.dialog_edit_shopping_memo, null);
        final EditText editTextNewQuantity = dialogsView.findViewById(R.id.editText_new_quantity);
        editTextNewQuantity.setText(String.valueOf(shoppingMemo.getQuantity()));
        final EditText editTextNewProduct = dialogsView.findViewById(R.id.editText_new_product);
        editTextNewProduct.setText(shoppingMemo.getProduct());

        builder.setView(dialogsView)
                .setTitle(R.string.dialog_titel)
                .setPositiveButton(R.string.dialog_button_positiv, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String quantityString = editTextNewQuantity.getText().toString();
                        String product = editTextNewProduct.getText().toString();

                        if (TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(product)) {
                            return;
                        }

                        int quantity = Integer.parseInt(quantityString);

                        ShoppingMemo updatedShoppingMemo = dataSource.updateShoppingMemo(shoppingMemo.getId(),
                                product, quantity, shoppingMemo.isChecked());
                        Log.d(LOG_TAG, "Alter Eintrag: " + shoppingMemo.getId() + " : " + shoppingMemo.toString());
                        Log.d(LOG_TAG, "Neuer Eintrag: " + updatedShoppingMemo.getId() + " : " +
                                updatedShoppingMemo.toString());
                        showAllListEntries();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_button_negativ, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onCreate: Datenquelle wird geöffnet");
        dataSource.open();
        initSpinner();
        Log.d(LOG_TAG, "Folgende Einträge sind in der DB vorhanden");
        showAllListEntries();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }else {
                ladeArtikelliste();
            }
        }else {
            ladeArtikelliste();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1 && (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)){
            ladeArtikelliste();
        }
    }

    private void initSpinner() {
        List<String> units = dataSource.getAllUnits();
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, units);
        this.units.setAdapter(spinnerArrayAdapter);
    }

    //TODO savedInstanceState zum halten der Infos bei ActionBar
    @Override
    protected void onPause() {
        super.onPause();
        speicherArtikelliste();
        Log.d(LOG_TAG, "onCreate: Datenquelle wird geschlossen");
        dataSource.close();
    }

    private String getFileName(){
        File dir = new File(Environment.getExternalStorageDirectory(),"shoppinglist");
        dir.mkdirs();
        File file = new File(dir,"artikelliste");
        return file.getAbsolutePath();
    }

    private void speicherArtikelliste(){
        String filename = getFileName();
        try(FileOutputStream outputStream = new FileOutputStream(filename);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter)){
            for(String string : liste){
                writer.write(string);
                writer.newLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ladeArtikelliste(){
        String fileName = getFileName();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            liste.clear();
            String string;
            while((string = reader.readLine())!=null){
                liste.add(string);
            }
            editTextProduct.setAdapter(new ArrayAdapter<String>(
                    MainActivity.this,android.R.layout.simple_list_item_1, liste));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAllListEntries() {
        List<ShoppingMemo> shoppingMemoList = dataSource.getAllShoppingMemos();

        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = (ArrayAdapter<ShoppingMemo>) shoppingMemosListView.getAdapter();
        shoppingMemoArrayAdapter.clear();
        shoppingMemoArrayAdapter.addAll(shoppingMemoList);
        shoppingMemoArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}

package com.polybot.poluluman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final String TAG = "MainActivity";

    final static String PREFERENCES = "pololuman.preferences";
    final static String PREFERENCES_MAC = "pololuman.preferences.mac";

    final static String INTENT_NB_POLOLU = "pololuman.nb";
    final static String INTENT_MACS = "pololuman.macs";
    final static String INTENT_GRID_SETUP = "pololuman.gridSetup";

    private Context context;
    private SharedPreferences sh;

    private EditText nbPololu;
    private LinearLayout macAdressesLayout;

    private ArrayList<EditText> editMac = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        context = this;
        sh = getSharedPreferences(PREFERENCES, MODE_PRIVATE);

        nbPololu = findViewById(R.id.nbPololu);
        macAdressesLayout = findViewById(R.id.macAdressesList);

        // Impossible de mettre un chiffre plus élevé que 5
        nbPololu.setFilters(new InputFilter[] {
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        if(Integer.parseInt(source.toString()) > 5)
                            return "5";
                        else
                            return source;
                    }
                }
        });

        // Generation de la liste de MAC quaand on change le nombre
        nbPololu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int nb = Integer.parseInt(s.toString());

                // Ajout dynamique d'élement dans la liste
                editMac.clear();
                macAdressesLayout.removeAllViews();
                for(int i=0; i<nb ; i++) {
                    EditText e = new EditText(context);
                    e.setLayoutParams(new LinearLayout.LayoutParams
                            (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    // Check if a mac adresse has been registered already
                    String str = sh.getString(PREFERENCES_MAC+i, "");

                    if(str.isEmpty())
                        e.setHint("MAC Pololu " + i);
                    else
                        e.setText(str);

                    editMac.add(e);
                    macAdressesLayout.addView(e);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        boolean error = false;
        int nb = editMac.size();
        String[] macs = new String[nb];

        if(editMac.size() == 0) {
            toast("Choisir un nombre de pololu");
            error = true;
        }
        else {
            for(int i=0; i<nb ; i++) {
                macs[i] = editMac.get(i).getText().toString();

                if(macs[i].isEmpty()) {
                    error = true;
                    toast("MAC incomplet");
                }
            }
        }

        if(!error) {
            // Saving MAC Adresses
            SharedPreferences.Editor edit = sh.edit();
            for(int i=0 ; i<nb ; i++)
                edit.putString(PREFERENCES_MAC+i, macs[i]);

            edit.apply();

            // Lancement de la prochaine activité
            Intent i = new Intent(this, GridSetupActivity.class);
            i.putExtra(INTENT_NB_POLOLU, nb);
            i.putExtra(INTENT_MACS, macs);
            startActivity(i);
        }
    }

    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}

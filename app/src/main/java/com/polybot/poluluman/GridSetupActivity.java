package com.polybot.poluluman;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import static com.polybot.poluluman.MainActivity.INTENT_GRID_SETUP;
import static com.polybot.poluluman.MainActivity.INTENT_MACS;
import static com.polybot.poluluman.MainActivity.INTENT_NB_POLOLU;

public class GridSetupActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;

    private Button next;
    private EditText editX, editY;
    private boolean OKEditX = false, OKEditY = false;

    private LinearLayout grid;

    private String[] macs;
    private int nbPololu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_setup);

        Intent i = getIntent();
        macs = i.getStringArrayExtra(INTENT_MACS);
        nbPololu = i.getIntExtra(INTENT_NB_POLOLU, 2);

        next = findViewById(R.id.doneConfig);
        editX = findViewById(R.id.editX);
        editY = findViewById(R.id.editY);
        grid = findViewById(R.id.gridSetupLayout);

        next.setOnClickListener(this);

        InputFilter[] filters = new InputFilter[] {
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        if(Integer.parseInt(source.toString()) > 10)
                            return "10";
                        else
                            return source;
                    }
                }
        };

        editX.setFilters(filters);
        editY.setFilters(filters);

        editX.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()) {
                    int val = Integer.parseInt(s.toString());

                    if(val > 0) {
                        OKEditX = true;
                        createGrid();
                    }
                    else
                        OKEditX = false;
                }
                else
                    OKEditX = false;
            }
        });
        editY.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()) {
                    int val = Integer.parseInt(s.toString());

                    if(val > 0) {
                        OKEditY = true;
                        createGrid();
                    }
                    else
                        OKEditY = false;
                }
                else
                    OKEditY = false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int x = Integer.parseInt(editX.getText().toString()), y = Integer.parseInt(editY.getText().toString());
        StringBuilder str = new StringBuilder(x*y);
        int acc = 0;

        for(int j=0 ; j<y ; j++) {
            LinearLayout line = (LinearLayout)grid.getChildAt(j);

            for(int i=0 ; i<x ; i++) {
                TextView tv = (TextView)line.getChildAt(i);

                if(!tv.getText().toString().equals(" "))
                    acc++;

                str.append(tv.getText().toString());
            }
        }

        if(acc == nbPololu) {
            // Lancement de la prochaine activité
            Intent i = new Intent(this, GameActivity.class);
            i.putExtra(INTENT_NB_POLOLU, nbPololu);
            i.putExtra(INTENT_MACS, macs);
            i.putExtra(INTENT_GRID_SETUP, str.toString());
            startActivity(i);
        }
        else
            toast("Grille invalide");
    }

    private void createGrid() {
        int x = Integer.parseInt(editX.getText().toString()), y = Integer.parseInt(editY.getText().toString());

        grid.removeAllViews();

        for(int j=0 ; j<y ; j++) {
            LinearLayout line = new LinearLayout(this);

            for(int i=0; i<x ; i++) {
                TextView tv = new TextView(this);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                tv.setText(" ");
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView view = (TextView)v;
                        String s = String.valueOf(view.getText());

                        if(s.equals(" "))
                            view.setText("0");
                        else {
                            int val = Integer.parseInt(view.getText().toString());

                            if(val < nbPololu)
                                view.setText(String.valueOf(val+1));
                            else
                                view.setText(" ");
                        }
                    }
                });
                line.addView(tv);
            }

            grid.addView(line);
        }
    }

    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}

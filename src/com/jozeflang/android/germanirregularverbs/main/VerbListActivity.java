package com.jozeflang.android.germanirregularverbs.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.jozeflang.android.germanirregularverbs.db.VerbDTO;

import java.util.List;

/**
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class VerbListActivity extends Activity {

    private GermanIrregularVerbsApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (GermanIrregularVerbsApplication) getApplication();
        setContentView(R.layout.verblist_layout);
        generateTableData((TableLayout) findViewById(R.id.verblist_table));
    }

    private void generateTableData(TableLayout table) {
        List<VerbDTO> verbs = application.getVerbs();
        for (VerbDTO verb : verbs) {
            TableRow tr = new TableRow(this);
            setRow(tr, verb);
            table.addView(tr);
        }
    }

    private void setRow(TableRow row, VerbDTO verb) {
        row.addView(createTextView(verb.getPresent()));
        row.addView(createTextView(verb.getPerfects().toArray(new VerbDTO.Perfect[] {})[0].toString()));
        row.addView(createTextView(verb.getPreterites().toArray(new VerbDTO.Preterite[] {})[0].toString()));
    }

    private TextView createTextView(String text) {
        TextView tw = new TextView(this);
        tw.setGravity(Gravity.CENTER);
        tw.setText(text);
        return tw;
    }
}

package com.jozeflang.android.germanirregularverbs.main;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
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
        EditText searchText = (EditText) findViewById(R.id.verblistSearchEdit);
        searchText.addTextChangedListener(new SearchTextWatcher());
        generateTableData((TableLayout) findViewById(R.id.verblist_table), false, "");
    }

    private void generateTableData(TableLayout table, boolean onlyActive, String filter) {
        table.removeAllViewsInLayout();
        List<VerbDTO> verbs = application.getVerbs(onlyActive, filter);
        for (VerbDTO verb : verbs) {
            TableRow tr = new TableRow(this);
            setRow(tr, verb);
            table.addView(tr);
        }
        table.requestLayout();
    }

    private void setRow(TableRow row, VerbDTO verb) {
        row.addView(createTextView(verb.getPresent()));
        row.addView(createTextView(verb.getPerfects().toArray(new VerbDTO.Perfect[] {})[0].toString()));
        row.addView(createTextView(verb.getPreterites().toArray(new VerbDTO.Preterite[] {})[0].toString()));
    }

    private TextView createTextView(String text) {
        TextView tw = new TextView(this);
        tw.setText(text);
        return tw;
    }

    private final class SearchTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }
        @Override
        public void afterTextChanged(Editable editable) {
            generateTableData((TableLayout) findViewById(R.id.verblist_table), false, editable.toString());
        }
    }
}

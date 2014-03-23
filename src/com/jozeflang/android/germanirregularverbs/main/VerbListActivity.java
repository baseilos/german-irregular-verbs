package com.jozeflang.android.germanirregularverbs.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
import com.jozeflang.android.germanirregularverbs.util.Utils;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class VerbListActivity extends Activity {

    private GermanIrregularVerbsApplication application;
    private final Logger logger = Logger.getLogger(VerbListActivity.class.getName());

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
        TableRow tableHeaderRow = (TableRow) table.getChildAt(0);
        table.removeAllViewsInLayout();
        table.addView(tableHeaderRow);
        // Add a row for every found verb
        List<VerbDTO> verbs = application.getVerbs(onlyActive, filter);
        for (VerbDTO verb : verbs) {
            table.addView(new VerbListTableRow(this, verb));
        }
        table.requestLayout();
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

    private final class VerbListTableRow extends TableRow implements View.OnLongClickListener {

        private VerbDTO verb;

        private VerbListTableRow(Context context, VerbDTO verb) {
            super(context);
            this.verb = verb;
            setClickable(true);
            setOnLongClickListener(this);
            redrawRow();
        }

        private VerbDTO getVerb() {
            return verb;
        }

        private void setVerb(VerbDTO verb) {
            this.verb = verb;
            redrawRow();
        }

        @Override
        public boolean onLongClick(View view) {
            if (!(view instanceof VerbListTableRow)) {
                return false;
            }
            VerbListTableRow row = (VerbListTableRow) view;
            VerbDTO updatedVerb = row.getVerb().switchActive();
            application.updateVerb(updatedVerb);
            row.setVerb(updatedVerb);
            return true;
        }

        private void redrawRow() {
            removeAllViewsInLayout();
            if (verb.isActive()) {
                addView(createTickView());
            } else {
                addView(createTextView(""));
            }
            addView(createTextView(verb.getPresent()));
            addView(createTextView(Utils.buildDelimitedString(verb.getPerfects(), ",")));
            addView(createTextView(Utils.buildDelimitedString(verb.getPreterites(), ",")));
        }

        private TextView createTextView(String text) {
            TextView tw = new TextView(getContext());
            tw.setText(text);
            return tw;
        }

        private ImageView createTickView() {
            ImageView iw = new ImageView(getContext());
            iw.setImageResource(R.drawable.tick);
            return iw;
        }

    }
}

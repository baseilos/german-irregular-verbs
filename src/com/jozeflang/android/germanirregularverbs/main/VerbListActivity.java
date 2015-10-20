package com.jozeflang.android.germanirregularverbs.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
import com.jozeflang.android.germanirregularverbs.util.Utils;

import java.util.Collections;
import java.util.List;

/**
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class VerbListActivity extends Activity {

    private GermanIrregularVerbsApplication application;
    private Context activityContext;
    private TableLayout verbTable;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verblist_layout);
        application = (GermanIrregularVerbsApplication) getApplication();
        activityContext = this;
        verbTable = (TableLayout) findViewById(R.id.verblist_table);
        searchEditText = (EditText) findViewById(R.id.verblistSearchEdit);
        searchEditText.addTextChangedListener(new SearchTextWatcher());
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadVerbsAsyncTask(this, verbTable).execute(getFilterString());
        application.sendAnalyticsHit(VerbListActivity.class.getSimpleName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        try {
            inflater.inflate(R.menu.verblist_menu, menu);
        } catch (InflateException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.verblist_activate_all:
                application.setVerbsActivness(true);
                break;
            case R.id.verblist_inactivate_all:
                application.setVerbsActivness(false);
                break;
            case R.id.verblist_invert_all:
                application.invertVerbActivness();
                break;
        }
        new LoadVerbsAsyncTask(activityContext, verbTable).execute(getFilterString());
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long activeVerbCount = application.getVerbCount(true);
            // Atleast one verb must be active!
            if (activeVerbCount == 0) {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.verblist_atleast_one_active), Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void generateTableData(TableLayout table, List<VerbDTO> verbs) {
        TableRow tableHeaderRow = (TableRow) table.getChildAt(0);
        table.removeAllViewsInLayout();
        table.addView(tableHeaderRow);
        // Add a row for every found verb
        for (VerbDTO verb : verbs) {
            table.addView(new VerbListTableRow(this, verb));
        }
        table.requestLayout();
    }

    private String getFilterString() {
        return searchEditText.getText().toString();
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
            new LoadVerbsAsyncTask(activityContext, verbTable).execute(editable.toString());
        }
    }

    private final class VerbListTableRow extends TableRow implements View.OnLongClickListener, View.OnTouchListener {

        private VerbDTO verb;

        private VerbListTableRow(Context context, VerbDTO verb) {
            super(context);
            this.verb = verb;
            setClickable(true);
            setOnLongClickListener(this);
            setOnTouchListener(this);
            prepareRow();
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
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!(view instanceof VerbListTableRow))
                return false;
            VerbListTableRow row = (VerbListTableRow) view;
            int mouseActionId = motionEvent.getAction();
            switch (mouseActionId) {
                case MotionEvent.ACTION_DOWN:
                    row.setBackgroundColor(getResources().getColor(R.color.verb_clicked));
                    changeFontColor(row, getResources().getColor(R.color.black));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_CANCEL:
                    setBackground(row);
                    setFontColor(row);
                    break;
            }
            return false;
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
            prepareRow();
            setBackground(this);
            setFontColor(this);
        }

        private void prepareRow() {
            removeAllViewsInLayout();
            addView(createTickView());
            addView(createTextView(verb.getPresent()));
            addView(createTextView(Utils.buildDelimitedString(verb.getPerfects(), ",")));
            addView(createTextView(Utils.buildDelimitedString(verb.getPreterites(), ",")));
        }

        private TextView createTextView(String text) {
            TextView tw = new TextView(getContext());
            tw.setText(text);
            return tw;
        }

        private TextView createTickView() {
            TextView tw = new TextView(getContext());
            tw.setTypeface(application.getFontAwesome());
            tw.setGravity(Gravity.CENTER);
            tw.setText(verb.isActive() ? getResources().getText(R.string.fontawesome_circle) : getResources().getText(R.string.fontawesome_cross));
            tw.setTextColor(getResources().getColor(R.color.white));
            return tw;
        }

        private void setBackground(TableRow row) {
            if (verb.isActive()) {
                row.setBackgroundColor(getResources().getColor(R.color.verb_active));
            } else {
                row.setBackgroundColor(getResources().getColor(R.color.verb_inactive));
            }
        }

        private void setFontColor(TableRow row) {
            if (verb.isActive()) {
                changeFontColor(row, getResources().getColor(R.color.black));
            } else {
                changeFontColor(row, getResources().getColor(R.color.white));
            }
        }

        private void changeFontColor(TableRow row, int toColor) {
            int childCount = row.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (!(row.getChildAt(i) instanceof TextView))
                    continue;
                TextView tw = (TextView) row.getChildAt(i);
                tw.setTextColor(toColor);
            }
        }

    }

    protected final class LoadVerbsAsyncTask extends AsyncTask<String, Void, List<VerbDTO>> {

        private final ProgressDialog progressDialog;
        private final TableLayout verbTable;

        protected LoadVerbsAsyncTask(Context context, TableLayout verbTable) {
            progressDialog = ProgressDialog.show(context, "", context.getResources().getString(R.string.pleaseWaitLabel));
            this.verbTable = verbTable;
        }

        @Override
        protected List<VerbDTO> doInBackground(String... filter) {
            if (filter == null || filter.length < 1) {
                return Collections.EMPTY_LIST;
            }
            return application.getVerbs(false, filter[0]);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            progressDialog.dismiss();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(List<VerbDTO> verbDTOs) {
            generateTableData(verbTable, verbDTOs);
            progressDialog.dismiss();
            super.onPostExecute(verbDTOs);
        }
    }

}

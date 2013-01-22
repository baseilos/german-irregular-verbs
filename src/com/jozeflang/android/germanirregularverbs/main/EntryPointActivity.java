package com.jozeflang.android.germanirregularverbs.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
import com.jozeflang.android.germanirregularverbs.db.VerbProvider;
import com.jozeflang.android.germanirregularverbs.validator.AnswerValidator;

/** 
 * Entry point activity
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class EntryPointActivity extends Activity {
	
	private VerbProvider verbProvider;
	private VerbDTO activeVerb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		initScreenElements(savedInstanceState);
		
		verbProvider = new VerbProvider(getApplicationContext());
		displayNewVerb();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		verbProvider.closeProvider();
	}



	private void initScreenElements(Bundle savedInstanceState) {
		initButtonsCallback(savedInstanceState);
	}
	
	private void initButtonsCallback(Bundle savedInstanceState) {
		Button nextBtn = (Button) findViewById(R.id.nextBtn);
		Button skipBtn = (Button) findViewById(R.id.skipBtn);
		
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String answer = ((TextView) findViewById(R.id.inputTE)).getText().toString();
				if (!AnswerValidator.validate(activeVerb, answer, activeVerb.getAnswerType())) {
					Toast.makeText(getApplicationContext(), R.string.incorrect_answer, Toast.LENGTH_SHORT).show();
				} else {
					displayNewVerb();
				}
			}
		});
		
		skipBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayNewVerb();
			}
		});
	}
	
	private void displayNewVerb() {
		activeVerb = verbProvider.getNextVerb();
		displayVerb(activeVerb);
	}
	
	private void displayVerb(VerbDTO verb) {
		TextView display = (TextView) findViewById(R.id.displayTW);
		TextView answerType = (TextView) findViewById(R.id.answerTypeTW);
		EditText input = (EditText) findViewById(R.id.inputTE);
		
		// Display present 
		display.setText(verb.getPresent());
		answerType.setText(verb.getAnswerType().getStringId());
		// Clear edit
		input.setText("");
	}

}

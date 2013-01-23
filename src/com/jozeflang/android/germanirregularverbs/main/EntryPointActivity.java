package com.jozeflang.android.germanirregularverbs.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
import com.jozeflang.android.germanirregularverbs.db.VerbProvider;
import com.jozeflang.android.germanirregularverbs.validator.AnswerType;
import com.jozeflang.android.germanirregularverbs.validator.AnswerValidator;

/** 
 * Entry point activity
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class EntryPointActivity extends Activity {
	
	private VerbProvider verbProvider;
	private Question activeQuestion = new Question();
	
	private EditText perfectAuxTE;
	private EditText perfectInputTE;
	private EditText preteriteInputTE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		initScreenElements(savedInstanceState);
		
		verbProvider = new VerbProvider(getApplicationContext());
		displayNewQuestion();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		verbProvider.closeProvider();
	}



	private void initScreenElements(Bundle savedInstanceState) {
		perfectAuxTE = ((EditText) findViewById(R.id.perfectAuxVerbInputTE));
		perfectInputTE = ((EditText) findViewById(R.id.perfectInputTE));
		preteriteInputTE = ((EditText) findViewById(R.id.preteriteInputTE));
		initButtonsCallback(savedInstanceState);
	}
	
	private void initButtonsCallback(Bundle savedInstanceState) {
		Button nextBtn = (Button) findViewById(R.id.nextBtn);
		Button skipBtn = (Button) findViewById(R.id.skipBtn);
		
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!AnswerValidator.validate(activeQuestion.verb, getAnswer(), activeQuestion.answerType)) {
					//Toast.makeText(getApplicationContext(), R.string.incorrect_answer, Toast.LENGTH_SHORT).show();
					if (perfectAuxTE.hasFocus()) {
						perfectAuxTE.setError(getString(R.string.incorrect_answer));
					} else if (perfectInputTE.hasFocus()) {
						perfectInputTE.setError(getString(R.string.incorrect_answer));
					} else {
						preteriteInputTE.setError(getString(R.string.incorrect_answer));
					}
				} else {
					displayNewQuestion();
				}
			}
		});
		
		skipBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayNewQuestion();
			}
		});
	}
	
	private void displayNewQuestion() {
		activeQuestion.verb = verbProvider.getNextVerb();
		activeQuestion.answerType = verbProvider.getAnswerType();
		displayQuestion(activeQuestion);
	}
	
	private void displayQuestion(Question question) {
		TextView display = (TextView) findViewById(R.id.displayTW);
		TextView answerType = (TextView) findViewById(R.id.answerTypeTW);
		display.setText(question.verb.getPresent());
		answerType.setText(question.answerType.getStringId());
		
		switch (question.answerType) {
			case PERFECT:
				 // Hide/show layout for a perfect question
				 findViewById(R.id.perfectLayout).setVisibility(View.VISIBLE);
				 findViewById(R.id.preteriteInputTE).setVisibility(View.GONE);
				 perfectAuxTE.setText("");
				 perfectInputTE.setText("");
				break;
			case PRETERITE:
				// Hide/show layout for a perfect question
				findViewById(R.id.perfectLayout).setVisibility(View.GONE);
				findViewById(R.id.preteriteInputTE).setVisibility(View.VISIBLE);
				preteriteInputTE.setText("");
				break;
		}
		
		// Hide errors
		perfectAuxTE.setError(null);
		perfectInputTE.setError(null);
		preteriteInputTE.setError(null);
		
	}
	
	private Answer getAnswer() {
		switch (activeQuestion.answerType) {
			case PERFECT:
				return new Answer(perfectAuxTE.getText().toString(), perfectInputTE.getText().toString());
			case PRETERITE:
				return new Answer(null, preteriteInputTE.getText().toString());
		}
		return new Answer(null, null);
	}
	
	
	private class Question {
		private AnswerType answerType;
		private VerbDTO verb;
		
	}

}

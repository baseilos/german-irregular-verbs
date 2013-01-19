package com.jozeflang.android.germanirregularverbs.main;

import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
import com.jozeflang.android.germanirregularverbs.db.VerbProvider;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** 
 * Entry point activity
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class EntryPointActivity extends Activity {
	
	private VerbProvider verbProvider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		initScreenElements(savedInstanceState);
	
		// Create verb provider
		verbProvider = new VerbProvider(getApplicationContext());
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
				// TODO Auto-generated method stub
				
			}
		});
		
		skipBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				VerbDTO nextVerb = verbProvider.getNextVerb();
				displayVerb(v, nextVerb);
			}
		});
	}
	
	private void displayVerb(View v, VerbDTO verb) {
		TextView display = (TextView) v.findViewById(R.id.displayTW);
		EditText input = (EditText) v.findViewById(R.id.inputTE);
		
		// Display present 
		display.setText(verb.getPresent());
		// Clear edit
		input.setText("");
	}

}

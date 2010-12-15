package com.cornerofseven.castroid.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class DialogBuilder {
	
	private DialogBuilder(){}
	
	/**
	 * Create a single line text input dialog.
	 * If the okay button is selected, sets the result of the text field into
	 * the result holder. 
	 * If the cancel button is selected, value of the result field is undefined.
	 * 
	 * After the result value is set, the okayAction or cancelAction is invoked.
	 * @param context
	 * @param title
	 * @param message
	 * @param okayAction
	 * @param cancelAction
	 * @param clearInputField whether or not the text input box should be cleared.
	 * @param resultHolder 
	 * @return
	 */
	public static Dialog makeInputDialog(final Context context, 
			final String title, 
			final String message, 
			final DialogInterface.OnClickListener okayAction,
			final DialogInterface.OnClickListener cancelAction,
			final boolean clearInputField,
			final DialogHelpers.ChoiceResponse<String> resultHolder){
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(title);
		alert.setMessage(message);
		final EditText input = new EditText(context);
		alert.setView(input);
		
		//delegate to the other action, after loading the result value
		final DialogInterface.OnClickListener fullOkayAction = 
			new DialogInterface.OnClickListener() {
			@Override
			public final void onClick(DialogInterface dialog, int which) {
				final String value = input.getText().toString();
				resultHolder.setWasOkay(true);
				resultHolder.setResult(value);
				
				if(okayAction != null) okayAction.onClick(dialog, which);
				
				if(clearInputField)
					input.getText().clear();
			}};
			
		final DialogInterface.OnClickListener fullCancelAction = 
			new DialogInterface.OnClickListener() {
			@Override
			public final void onClick(DialogInterface dialog, int which) {
				resultHolder.setWasOkay(false);
				if(cancelAction!=null) cancelAction.onClick(dialog, which);
				if(clearInputField)
					input.getText().clear();
				
			}
		};
		
		alert.setPositiveButton(android.R.string.ok, fullOkayAction);
		alert.setNegativeButton(android.R.string.cancel, fullCancelAction);
		
		
		return alert.create();
	}

}

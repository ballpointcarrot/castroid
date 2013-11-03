package com.cornerofseven.castroid.dialogs;


public class DialogResult<T> implements DialogHelpers.ChoiceResponse<T> {

	private T result = null;
	private boolean okay = false; 
	
	@Override
	public T getResult() {
		return result;
	}

	@Override
	public void setResult(T t) {
		this.result = t;
	}

	@Override
	public boolean wasOkay() {
		return okay;
	}

	@Override
	public void setWasOkay(boolean b) {
		this.okay = b;
	}

}

package com.cornerofseven.castroid.dialogs;

/**
 * Define various callback type interfaces for dialog inputs.
 * @author Sean Mooney
 *
 */
public interface DialogHelpers {
	/**
	 * hold the result of some input action.
	 * @author sean
	 *
	 * @param <T>
	 */
	public interface ChoiceResponse<T>{
		/**
		 * Get the result of the input
		 * @return
		 */
		public T getResult();
		/**
		 * Set the result
		 * @param t
		 */
		public void setResult(T t);
		/**
		 * Check to see if they selected okay or not.
		 * @return
		 */
		public boolean wasOkay();
		/**
		 * 
		 * @param b
		 */
		public void setWasOkay(boolean b);
	}
}

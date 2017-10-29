package com.tinytimrob.ppse.nmo;

public abstract class Action
{
	/** 
	 * Runs this action
	 * 
	 * @throws Exception
	 */
	public abstract void onAction() throws Exception;

	public abstract String getName();
	
	/**
	 * 
	 * @return Description that shows more info about this action.
	 */
	public String getDescription() {
		return "FOO";
	}

	public abstract boolean isHiddenFromFrontend();

	public abstract boolean isHiddenFromWebUI();

	public abstract boolean isBlockedFromWebUI();
}

package com.tinytimrob.ppse.nmo;

public interface Action
{
	public void onAction() throws Exception;

	public String getName();

	public boolean isHiddenFromFrontend();

	public boolean isHiddenFromWebUI();

	public boolean isBlockedFromWebUI();
}

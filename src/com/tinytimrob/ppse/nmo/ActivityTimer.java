package com.tinytimrob.ppse.nmo;

import com.google.gson.annotations.Expose;

public class ActivityTimer
{
	@Expose
	public String name;

	@Expose
	public long secondsForFirstWarning;

	@Expose
	public long secondsForSubsequentWarnings;
}

package com.tinytimrob.ppse.nmo.integration.filewriter;

import com.google.gson.annotations.Expose;

public class FileWriterConfiguration
{
	@Expose
	public boolean scheduleName;

	@Expose
	public boolean scheduleStartedOn;

	@Expose
	public boolean scheduleLastOversleep;

	@Expose
	public boolean schedulePersonalBest;

	@Expose
	public boolean timeToNextSleepBlock;
}
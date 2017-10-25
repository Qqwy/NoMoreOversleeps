package com.tinytimrob.ppse.nmo.config;

import com.google.gson.annotations.Expose;
import com.tinytimrob.common.Configuration;

public class NMOStatistics
{
	public static NMOStatistics instance;

	public static void load() throws Exception
	{
		instance = Configuration.load(NMOStatistics.class, "stats.json");
	}

	public static void save() throws Exception
	{
		Configuration.save(instance, "stats.json");
	}

	@Expose
	public long scheduleStartedOn = 0;

	@Expose
	public long scheduleLastOversleep = 0;

	@Expose
	public long schedulePersonalBest = 0;
}

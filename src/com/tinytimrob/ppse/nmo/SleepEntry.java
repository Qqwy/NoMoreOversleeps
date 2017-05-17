package com.tinytimrob.ppse.nmo;

import com.google.gson.annotations.Expose;

public class SleepEntry implements Comparable<SleepEntry>
{
	public SleepEntry(int start, int end, String name)
	{
		this.start = start;
		this.end = end;
		this.name = name;
	}

	public SleepEntry(int startH, int startM, int endH, int endM, String name)
	{
		this((60 * startH) + startM, (60 * endH) + endM, name);
	}

	@Expose
	public final int start;

	@Expose
	public final int end;

	@Expose
	public final String name;

	@Override
	public int compareTo(SleepEntry o)
	{
		int i = Integer.compare(this.start, o.start);
		if (i == 0)
		{
			i = Integer.compare(this.end, o.end);
		}
		return i;
	}

	public boolean containsTime(int currentMinuteOfDay)
	{
		if (this.start > this.end)
		{
			return currentMinuteOfDay >= this.start || currentMinuteOfDay < this.end;
		}
		else
		{
			return currentMinuteOfDay >= this.start && currentMinuteOfDay < this.end;
		}
	}
}

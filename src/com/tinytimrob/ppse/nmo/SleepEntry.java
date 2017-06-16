package com.tinytimrob.ppse.nmo;

import org.apache.commons.lang3.StringUtils;
import com.google.gson.annotations.Expose;

public class SleepEntry implements Comparable<SleepEntry>
{
	public SleepEntry()
	{
		this.approachWarning = 5;
	}

	public SleepEntry(int start, int end, String name)
	{
		this.start = start;
		this.end = end;
		this.name = name;
		this.approachWarning = 5;
	}

	public SleepEntry(int startH, int startM, int endH, int endM, String name)
	{
		this((60 * startH) + startM, (60 * endH) + endM, name);
	}

	@Expose
	public int start;

	@Expose
	public int end;

	@Expose
	public String name;

	@Expose
	public int approachWarning;

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

	public String describe()
	{
		return this.name + " :: " + this.describeTime();
	}

	public String describeTime()
	{
		return StringUtils.leftPad("" + (this.start / 60), 2, "0") + ":" + StringUtils.leftPad("" + (this.start % 60), 2, "0") + " - " + StringUtils.leftPad("" + (this.end / 60), 2, "0") + ":" + StringUtils.leftPad("" + (this.end % 60), 2, "0");
	}
}

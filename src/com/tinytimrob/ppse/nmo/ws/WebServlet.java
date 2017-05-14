package com.tinytimrob.ppse.nmo.ws;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.ListIterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.annotations.Expose;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.ppse.nmo.Main;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import com.tinytimrob.ppse.nmo.Pavlok;
import com.tinytimrob.ppse.nmo.PhoneControl;
import freemarker.template.TemplateException;

public class WebServlet extends HttpServlet
{
	public static class JsonData
	{
		@Expose
		public String update;

		@Expose
		public String activity;

		@Expose
		public String schedule;

		@Expose
		public String pause_state;
	}

	private static final long serialVersionUID = 6713485873809808119L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String PATH = request.getPathInfo();
		if (PATH.equals("/log"))
		{
			// send log
			int x = 0;
			StringWriter writer = new StringWriter();
			writer.write("log updated " + CommonUtils.convertTimestamp(System.currentTimeMillis()) + "\n\n");
			ListIterator<String> li = MainDialog.events.listIterator(MainDialog.events.size());
			while (li.hasPrevious())
			{
				x++;
				String s = li.previous();
				writer.write(s + "\n");
				if (x == 20)
					break;
			}
			response.getWriter().append(writer.toString());
		}
		else if (PATH.equals("/json"))
		{
			// send json update
			JsonData data = new JsonData();
			long now = System.currentTimeMillis();
			boolean paused = MainDialog.isCurrentlyPaused.get();
			data.update = CommonUtils.convertTimestamp(now);
			data.activity = paused ? "Disabled while paused" : CommonUtils.convertTimestamp(MainDialog.lastActivityTime) + " (" + (now - MainDialog.lastActivityTime) + "ms ago)";
			if (paused)
			{
				data.pause_state = "PAUSED for \"" + MainDialog.pauseReason + "\" until " + CommonUtils.dateFormatter.format(MainDialog.pausedUntil);
			}
			else
			{
				data.pause_state = "RUNNING";
			}
			response.getWriter().append(CommonUtils.GSON.toJson(data));
		}
		else if (PATH.equals("/"))
		{
			// send main web page
			HashMap<String, Object> model = new HashMap<String, Object>();
			model.put("version", Main.VERSION);
			model.put("phoneSwitchboard", NMOConfiguration.instance.phoneSwitchboard);
			model.put("phoneMobile", NMOConfiguration.instance.phoneMobile);
			try
			{
				WebTemplate.renderTemplate("nmo.ftl", response, model);
			}
			catch (TemplateException e)
			{
				throw new ServletException(e);
			}
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			String PATH = request.getPathInfo();
			if (PATH.equals("/beep"))
			{
				Pavlok.beep(255, "WEB UI remotely triggered beep");
				MainDialog.addEvent("<BEEP> from WEB UI");
				response.sendRedirect("/");
			}
			else if (PATH.equals("/vibration"))
			{
				Pavlok.vibration(255, "WEB UI remotely triggered vibration");
				MainDialog.addEvent("<VIBRATION> from WEB UI");
				response.sendRedirect("/");
			}
			else if (PATH.equals("/shock"))
			{
				Pavlok.shock(255, "WEB UI remotely triggered shock");
				MainDialog.addEvent("<SHOCK> from WEB UI");
				response.sendRedirect("/");
			}
			else if (PATH.equals("/call_switchboard"))
			{
				PhoneControl.callSwitchboard();
				MainDialog.addEvent("<CALL " + NMOConfiguration.instance.phoneSwitchboard + "> from WEB UI");
				response.sendRedirect("/");
			}
			else if (PATH.equals("/call_mobile"))
			{
				PhoneControl.callMobile();
				MainDialog.addEvent("<CALL " + NMOConfiguration.instance.phoneMobile + "> from WEB UI");
				response.sendRedirect("/");
			}
			else
			{
				response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}

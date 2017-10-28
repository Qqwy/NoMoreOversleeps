package com.tinytimrob.ppse.nmo.integration.webui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import com.google.gson.annotations.Expose;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.PlatformData;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.Main;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;
import com.tinytimrob.ppse.nmo.config.NMOStatistics;
import com.tinytimrob.ppse.nmo.integration.noise.IntegrationNoise;
import com.tinytimrob.ppse.nmo.integration.philipshue.IntegrationPhilipsHue;
import com.tinytimrob.ppse.nmo.integration.tplink.IntegrationTPLink;
import com.tinytimrob.ppse.nmo.integration.tplink.TPLinkDeviceEntry;
import com.tinytimrob.ppse.nmo.utils.FormattingHelper;
import freemarker.template.TemplateException;

public class WebServlet extends HttpServlet
{
	public static class JsonData
	{
		@Expose
		public String update;

		@Expose
		public String active_timer;

		@Expose
		public String activity;

		@Expose
		public String schedule_name;

		@Expose
		public String schedule;

		@Expose
		public String pause_state;

		@Expose
		public int conn_count;

		@Expose
		public String noise_state;

		@Expose
		public String ha_state;
	}

	private static final long serialVersionUID = 6713485873809808119L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String PATH = request.getPathInfo();
		if (PATH.equals("/favicon.ico"))
		{
			InputStream fis = null;
			OutputStream out = null;
			try
			{
				response.setContentType("image/x-icon");
				fis = WebServlet.class.getResourceAsStream("/resources/icon.ico");
				out = response.getOutputStream();
				IOUtils.copy(fis, out);
			}
			finally
			{
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(fis);
			}
			return;
		}
		else if (PATH.equals("/ui/log"))
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
		else if (PATH.equals("/ui/json"))
		{
			// send json update
			JsonData data = new JsonData();
			long now = System.currentTimeMillis();
			boolean paused = MainDialog.isCurrentlyPaused.get();
			data.update = CommonUtils.convertTimestamp(now);
			data.activity = paused ? "Disabled while paused" : CommonUtils.convertTimestamp(MainDialog.lastActivityTime) + " (" + String.format("%.3f", (now - MainDialog.lastActivityTime) / 1000.0) + "s ago from " + MainDialog.lastActivitySource + ")";
			data.active_timer = MainDialog.timer == null ? "null" : MainDialog.timer.name + " (" + MainDialog.timer.secondsForFirstWarning + "s/" + MainDialog.timer.secondsForSubsequentWarnings + "s)";
			if (paused)
			{
				data.pause_state = "PAUSED for \"" + MainDialog.pauseReason + "\" until " + CommonUtils.dateFormatter.format(MainDialog.pausedUntil);
			}
			else
			{
				data.pause_state = "RUNNING";
			}
			data.conn_count = WebcamCapture.count();
			data.noise_state = IntegrationNoise.INSTANCE.getNoiseList();
			String state = "";
			if (IntegrationPhilipsHue.INSTANCE.isEnabled())
			{
				for (String key : IntegrationPhilipsHue.INSTANCE.lightStates.keySet())
				{
					state += (!state.isEmpty() ? "<br/>" : "");
					int val = IntegrationPhilipsHue.INSTANCE.lightStates.get(key);
					state += "<b>" + key + "</b>:  " + (val > -1 ? "ON, LIGHT LEVEL " + val : "OFF");
				}
			}
			if (IntegrationTPLink.INSTANCE.isEnabled())
			{
				for (int i = 0; i < NMOConfiguration.instance.integrations.tplink.devices.length; i++)
				{
					TPLinkDeviceEntry tpde = NMOConfiguration.instance.integrations.tplink.devices[i];
					state += (!state.isEmpty() ? "<br/>" : "");
					state += "<b>" + tpde.name + "</b>:  " + (tpde.isSwitchedOn ? "ON" : "OFF");
				}
			}
			data.ha_state = state;
			String sn = NMOConfiguration.instance.scheduleName;
			if (sn == null || sn.isEmpty())
			{
				sn = "UNKNOWN SCHEDULE";
			}
			data.schedule_name = "<b>" + sn + "</b>";
			if (NMOStatistics.instance.scheduleStartedOn > 0)
			{
				data.schedule_name += "<br/>Started: " + CommonUtils.dateFormatter.format(NMOStatistics.instance.scheduleStartedOn) + "<br/>(" + FormattingHelper.formatTimeElapsedWithDays(now, NMOStatistics.instance.scheduleStartedOn) + " ago)";
			}
			data.schedule = MainDialog.scheduleStatus;
			response.getWriter().append(CommonUtils.GSON.toJson(data));
		}
		else if (PATH.equals("/ui/"))
		{
			// send main web page
			HashMap<String, Object> model = new HashMap<String, Object>();
			model.put("version", Main.VERSION);
			// determine buttons
			String actionButtons = "";
			String[] colours = { "danger", "info", "success", "primary", "purple", "warning" };
			int colour = -1;
			for (Integration integration : Main.integrations)
			{
				LinkedHashMap<String, Action> actions = integration.getActions();
				// secret action fix
				int actionsNotSecret = 0;
				for (Action action : actions.values())
				{
					if (!action.isSecret())
					{
						actionsNotSecret++;
					}
				}
				if (actionsNotSecret > 0)
				{
					colour++;
					if (colour >= colours.length)
						colour = 0;
				}
				for (String key : actions.keySet())
				{
					Action action = actions.get(key);
					if (!action.isSecret())
					{
						actionButtons += "<form method='POST' data-js-ajax-form='true' action='/ui" + key + "'><button type='submit' class='btn btn-" + colours[colour] + " nmo-button'>" + action.getName() + "</button></form>";
					}
				}
			}
			model.put("system", PlatformData.computerName);
			model.put("actionButtons", actionButtons);
			model.put("webcamKey", NMOConfiguration.instance.integrations.webUI.webcamSecurityKey);
			for (Integration integration : Main.integrations)
			{
				model.put("integration_" + integration.id, integration.isEnabled());
			}
			try
			{
				WebTemplate.renderTemplate("nmo.ftl", response, model);
			}
			catch (TemplateException e)
			{
				throw new ServletException(e);
			}
		}
		else if (PATH.equals("/"))
		{
			response.sendRedirect("/ui/");
			return;
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
			if (PATH.startsWith("/ui/"))
			{
				for (Integration integrations : Main.integrations)
				{
					Action button = integrations.getActions().get(PATH.substring(3));
					if (button != null && !button.isSecret())
					{
						button.onAction();
						MainDialog.triggerEvent("<" + button.getName() + "> from /" + request.getRemoteAddr(), null);

						// When calling through AJAX, no response HTML necessary
						if (request.getParameter("ajax_form") != null)
						{
							response.setStatus(HttpServletResponse.SC_OK);
						}
						else
						{
							response.sendRedirect("/");
						}
						return;
					}
				}
			}
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}

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
import com.tinytimrob.ppse.nmo.integration.noise.IntegrationNoise;
import com.tinytimrob.ppse.nmo.integration.philipshue.IntegrationPhilipsHue;
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

		@Expose
		public int conn_count;

		@Expose
		public String noise_state;

		@Expose
		public String light_state;
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
		else if (PATH.equals("/log"))
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
			data.activity = paused ? "Disabled while paused" : CommonUtils.convertTimestamp(MainDialog.lastActivityTime) + " (" + (now - MainDialog.lastActivityTime) + "ms ago from " + MainDialog.lastActivitySource + ")";
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
			data.light_state = IntegrationPhilipsHue.INSTANCE.lightState > -1 ? "ON, LIGHT LEVEL " + IntegrationPhilipsHue.INSTANCE.lightState : "OFF";
			data.schedule = MainDialog.scheduleStatus;
			response.getWriter().append(CommonUtils.GSON.toJson(data));
		}
		else if (PATH.equals("/"))
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
				if (!actions.isEmpty())
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
						actionButtons += "<form method='POST' action='" + key + "'><button type='submit' class='btn btn-" + colours[colour] + "' style='width:286px;'>" + action.getName() + "</button></form>";
					}
				}
			}
			model.put("system", PlatformData.computerName);
			model.put("actionButtons", actionButtons);
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
			for (Integration integrations : Main.integrations)
			{
				Action button = integrations.getActions().get(PATH);
				if (button != null && !button.isSecret())
				{
					button.onAction();
					MainDialog.triggerEvent("<" + button.getName() + "> from /" + request.getRemoteAddr(), null);
					response.sendRedirect("/");
					return;
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

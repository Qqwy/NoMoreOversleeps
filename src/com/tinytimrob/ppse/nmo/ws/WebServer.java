package com.tinytimrob.ppse.nmo.ws;

import java.io.File;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import com.tinytimrob.common.PlatformData;
import com.tinytimrob.ppse.nmo.Main;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

public class WebServer
{
	/** Jetty server */
	static Server SERVER;

	public static void initialize() throws Exception
	{
		//=================================
		// Start embedded Jetty server for napcharts
		//=================================
		SERVER = new Server();
		ServerConnector httpConnector = new ServerConnector(SERVER);
		httpConnector.setPort(NMOConfiguration.instance.jettyPort);
		httpConnector.setName("Main");
		SERVER.addConnector(httpConnector);
		HandlerCollection handlerCollection = new HandlerCollection();
		StatisticsHandler statsHandler = new StatisticsHandler();
		statsHandler.setHandler(handlerCollection);
		SERVER.setStopTimeout(5000);
		SERVER.setHandler(statsHandler);
		WebSocketServlet websocketServlet = new WebSocketServlet()
		{
			private static final long serialVersionUID = -4394403163936790144L;

			@Override
			public void configure(WebSocketServletFactory factory)
			{
				factory.register(WebcamWebSocketHandler.class);
			}
		};
		ServletContextHandler contextHandler = new ServletContextHandler();
		contextHandler.setContextPath("/");
		ServletHolder webcamServletHolder = new ServletHolder("webcam", websocketServlet);
		contextHandler.addServlet(webcamServletHolder, "/webcam");
		ServletHolder napchartServlet = new ServletHolder("default", new WebServlet());
		contextHandler.addServlet(napchartServlet, "/*");
		handlerCollection.addHandler(contextHandler);
		NCSARequestLog requestLog = new NCSARequestLog(new File(PlatformData.installationDirectory, "logs/requestlog-yyyy_mm_dd.request.log").getAbsolutePath());
		requestLog.setAppend(true);
		requestLog.setExtended(false);
		requestLog.setLogTimeZone("GMT");
		requestLog.setLogLatency(true);
		requestLog.setRetainDays(90);
		requestLog.setLogServer(true);
		requestLog.setPreferProxiedForAddress(true);
		SERVER.setRequestLog(requestLog);
		SERVER.start();
		HttpGenerator.setJettyVersion("NoMoreOversleeps/" + Main.VERSION);
	}

	public static void shutdown() throws Exception
	{
		if (SERVER != null)
		{
			SERVER.stop();
		}
	}
}

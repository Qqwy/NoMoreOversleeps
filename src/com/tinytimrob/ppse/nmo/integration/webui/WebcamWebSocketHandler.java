package com.tinytimrob.ppse.nmo.integration.webui;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.auth.AuthenticationException;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

@WebSocket
public class WebcamWebSocketHandler implements Runnable
{
	private static final Logger log = LogWrapper.getLogger();
	private Session session;
	public String connectionIP;

	private void teardown()
	{
		if (this.session != null)
		{
			if (this.connectionIP != null)
			{
				log.info("WebSocket disconnect from " + this.connectionIP);
			}
			try
			{
				this.session.close();
				this.session = null;
			}
			catch (Throwable t)
			{
				//
			}
		}
		WebcamCapture.removeSocketHandler(this);
	}

	@OnWebSocketConnect
	public void onConnect(Session session) throws AuthenticationException
	{
		Map<String, List<String>> params = session.getUpgradeRequest().getParameterMap();
		List<String> keys = params.get("key");
		if (keys == null || keys.size() != 1 || !keys.get(0).equals(NMOConfiguration.INSTANCE.integrations.webUI.webcamSecurityKey))
		{
			throw new AuthenticationException("Not authorized");
		}
		this.session = session;
		this.connectionIP = session.getRemoteAddress().getAddress().toString();
		log.info("WebSocket connect from " + this.connectionIP);
		WebcamCapture.addSocketHandler(this);
		new Thread(this).start();
	}

	@Override
	public void run()
	{
		log.info(">> Started sending data to " + this.connectionIP);
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("type", "image");
		while (this.session != null)
		{
			message.put("image", WebcamData.imageBase64);
			try
			{
				this.send(message);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				break;
			}
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		log.info(">> Stopped sending data to " + this.connectionIP);
	}

	@OnWebSocketMessage
	public void onMessage(String message)
	{
		log.info("WebSocket message: {}", message);
	}

	@OnWebSocketError
	public void onError(Throwable t)
	{
		log.error("WebSocket error", t);
		this.teardown();
	}

	@OnWebSocketClose
	public void onClose(int status, String reason)
	{
		this.teardown();
	}

	private void send(String message) throws IOException
	{
		if (this.session != null && this.session.isOpen())
		{
			this.session.getRemote().sendString(message);
		}
	}

	private void send(Object object) throws IOException
	{
		this.send(CommonUtils.GSON.toJson(object));
	}
}
package com.tinytimrob.ppse.nmo.integration.webui;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

@WebSocket
public class WebcamWebSocketHandler implements WebcamListener
{
	private static final Logger log = LogWrapper.getLogger();
	private Session session;
	private String connectionIP;

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
		WebcamCapture.removeListener(this);
	}

	@OnWebSocketConnect
	public void onConnect(Session session)
	{
		this.session = session;
		this.connectionIP = session.getRemoteAddress().getAddress().toString();
		log.info("WebSocket connect from " + this.connectionIP);
		WebcamCapture.addListener(this);
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

	private void send(String message)
	{
		if (this.session != null && this.session.isOpen())
		{
			try
			{
				this.session.getRemote().sendStringByFuture(message);
			}
			catch (Exception e)
			{
				log.error("Exception when sending string", e);
			}
		}
	}

	private void send(Object object)
	{
		try
		{
			this.send(CommonUtils.GSON.toJson(object));
		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void webcamOpen(WebcamEvent we)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void webcamClosed(WebcamEvent we)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void webcamDisposed(WebcamEvent we)
	{
		// TODO Auto-generated method stub
	}

	int frame = -1;
	boolean send = true;

	@Override
	public void webcamImageObtained(WebcamEvent we)
	{
		this.frame++;
		if (this.frame % NMOConfiguration.instance.webcamFrameSkip != 0)
		{
			return;
		}
		this.frame = 0;
		BufferedImage image = WebcamCapture.getImage();
		if (image == null)
			return;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(image, "JPG", baos);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		String base64 = null;
		try
		{
			base64 = new String(Base64.getEncoder().encode(baos.toByteArray()), "UTF8");
		}
		catch (UnsupportedEncodingException e)
		{
			log.error(e.getMessage(), e);
		}
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("type", "image");
		message.put("image", base64);
		this.send(message);
	}
}
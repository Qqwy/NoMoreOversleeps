package com.tinytimrob.ppse.nmo.ws;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
import com.tinytimrob.ppse.nmo.WebcamCapture;

@WebSocket
public class WebcamWebSocketHandler implements WebcamListener
{
	public static final AtomicInteger connectionCounter = new AtomicInteger(0);
	private static final Logger log = LogWrapper.getLogger();
	private Session session;

	private void teardown()
	{
		if (this.session != null)
		{
			connectionCounter.decrementAndGet();
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
		connectionCounter.incrementAndGet();
		log.info("WebSocket connect, from = {}", session.getRemoteAddress().getAddress());
		WebcamCapture.addListener(this);
	}

	@OnWebSocketMessage
	public void onMessage(String message)
	{
		log.info("WebSocket message, text = {}", message);
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
		log.info("WebSocket closed, status = {}, reason = {}", status, reason);
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
		frame++;
		if (this.frame % NMOConfiguration.instance.webcamFrameSkip != 0)
		{
			return;
		}
		this.frame = 0;
		BufferedImage image = WebcamCapture.getImage();
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
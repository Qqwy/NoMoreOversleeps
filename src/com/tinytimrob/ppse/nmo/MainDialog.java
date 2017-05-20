package com.tinytimrob.ppse.nmo;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.integrations.Integration;
import com.tinytimrob.ppse.nmo.integrations.IntegrationMouse;
import com.tinytimrob.ppse.nmo.integrations.IntegrationPavlok;
import com.tinytimrob.ppse.nmo.utils.JavaFxHelper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MainDialog extends Application
{
	private static final Logger log = LogWrapper.getLogger();
	public static Scene scene;

	public static volatile String pauseReason = "";
	public static volatile long pausedUntil = 0;
	public static volatile long nextActivityWarningTimeDiff;
	public static volatile long lastActivityTime = System.currentTimeMillis();
	public static volatile SimpleStringProperty loginTokenValidUntilString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty lastActivityTimeString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty lastCursorPositionString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty timeDiffString = new SimpleStringProperty("");
	public static volatile SimpleBooleanProperty isCurrentlyPaused = new SimpleBooleanProperty(false);
	public static volatile SimpleObjectProperty<Image> lastWebcamImage = new SimpleObjectProperty<Image>();
	public static volatile SleepEntry lastSleepBlockWarning = null;
	public static volatile SleepEntry nextSleepBlock = null;
	public static volatile String scheduleStatus = "";
	public static volatile WritableImage writableImage = null;
	public static ObservableList<String> events = FXCollections.observableArrayList();
	public static volatile int tick = 0;

	@Override
	public void start(Stage stage) throws Exception
	{
		log.info("JavaFX application start");
		triggerEvent("Application started", null);

		nextActivityWarningTimeDiff = NMOConfiguration.instance.activityWarningTimeInitialMs;

		Collections.sort(NMOConfiguration.instance.schedule);
		for (SleepEntry entry : NMOConfiguration.instance.schedule)
		{
			triggerEvent("Adding sleep block: " + entry.name + " " + StringUtils.leftPad("" + (entry.start / 60), 2, "0") + ":" + StringUtils.leftPad("" + (entry.start % 60), 2, "0") + "-" + StringUtils.leftPad("" + (entry.end / 60), 2, "0") + ":" + StringUtils.leftPad("" + (entry.end % 60), 2, "0"), null);
		}

		if (NMOConfiguration.instance.integrations.pavlok.enabled)
		{
			try
			{
				IntegrationPavlok.INSTANCE.vibration(255, "Connection test");
				triggerEvent("<VIBRATION> Connection test", null);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				NMOConfiguration.instance.integrations.pavlok.auth = null;
			}
		}

		//==================================================================
		// CONFIGURE THE STAGE
		//==================================================================
		stage.setTitle("NoMoreOversleeps v" + Main.VERSION);
		stage.getIcons().add(new Image(JavaFxHelper.buildResourcePath("icon.png")));
		stage.setResizable(false);
		stage.setMinWidth(1000);
		stage.setMinHeight(940);

		ImageView webcamImageView = new ImageView();

		//==================================================================
		// CONFIGURE ANIMATION TIMER
		//==================================================================
		// this is an absurd workaround
		final AnimationTimer at = new AnimationTimer()
		{
			@Override
			public void handle(long now)
			{
				MainDialog.this.tick();
			}
		};

		//==================================================================
		// CONFIGURE WEBCAM CAPTURE
		//==================================================================

		webcamImageView.imageProperty().bind(lastWebcamImage);
		webcamImageView.setFitWidth(256);
		webcamImageView.setPreserveRatio(true);
		final BorderPane webcamPane = new BorderPane();
		webcamPane.setPadding(new Insets(2, 2, 2, 2));
		webcamPane.setCenter(webcamImageView);

		//==================================================================
		// CONFIGURE THE SCENE
		//==================================================================
		final StackPane outerPane = new StackPane();
		outerPane.setId("root");
		scene = new Scene(outerPane, 800, 600, Color.WHITE);
		scene.getStylesheets().add(JavaFxHelper.buildResourcePath("application.css"));

		//==================================================================
		// BUILD THE PRIMARY PANE
		//==================================================================
		final BorderPane innerPane = new BorderPane();
		{ // Main container pane
			innerPane.setStyle("-fx-background-color: #222;");
		}

		final HBox innerTopPane = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 35, new Insets(8, 4, 8, 4));
		{ // Top section
			innerTopPane.setStyle("-fx-background-color: #414C7D;");
			final Label label = JavaFxHelper.createLabel(" NoMoreOversleeps", Color.WHITE, "-fx-font-weight: bold; -fx-font-size:16;", new Insets(0, 0, 0, 3), 320, Control.USE_COMPUTED_SIZE);
			label.setGraphic(JavaFxHelper.createIcon(FontAwesomeIcon.BED, "16", Color.WHITE));
			innerTopPane.getChildren().add(label);
			innerPane.setTop(innerTopPane);
		}

		final BorderPane centerPaneI = new BorderPane();
		{ // Real center pane
			innerPane.setCenter(centerPaneI);
		}

		final GridPane centerPane = new GridPane();
		{ // Center pane
			final Label loginTokenValidUntil = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			loginTokenValidUntil.textProperty().bind(loginTokenValidUntilString);
			loginTokenValidUntil.setPadding(new Insets(6, 0, 10, 8));
			centerPane.addRow(0, loginTokenValidUntil);

			final Label lastCursorTime = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			lastCursorTime.textProperty().bind(lastActivityTimeString);
			lastCursorTime.setPadding(new Insets(6, 0, 0, 8));
			centerPane.addRow(1, lastCursorTime);

			final Label lastCursorPosition = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			lastCursorPosition.textProperty().bind(lastCursorPositionString);
			lastCursorPosition.setPadding(new Insets(3, 0, 0, 8));
			centerPane.addRow(2, lastCursorPosition);

			final Label timeDiff = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			timeDiff.textProperty().bind(timeDiffString);
			timeDiff.setPadding(new Insets(3, 0, 0, 8));
			centerPane.addRow(3, timeDiff);

			centerPaneI.setCenter(centerPane);
		}

		final ListView<String> listView = new ListView<String>(events);
		listView.getItems().addListener(new ListChangeListener<String>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c)
			{
				listView.scrollTo(c.getList().size() - 1);
			}
		});
		listView.setMinHeight(540);
		centerPaneI.setBottom(listView);

		final BorderPane rightPaneI = new BorderPane();
		{ // Real center pane
			innerPane.setRight(rightPaneI);
		}
		rightPaneI.setTop(webcamPane);

		final GridPane innerRightPane = new GridPane();
		{ // Manual controls
			int row = 0;
			innerRightPane.setMinWidth(260);
			innerRightPane.setMaxWidth(260);
			innerRightPane.setStyle("-fx-background-color: #444;");
			innerRightPane.setVgap(6);
			innerRightPane.setPadding(new Insets(10, 10, 10, 10));
			final Label label = JavaFxHelper.createLabel("Manual controls", Color.WHITE, "", new Insets(0, 0, 0, 3), 160, Control.USE_COMPUTED_SIZE);
			innerRightPane.addRow(row++, label);

			// Integration buttons
			for (Integration integration : Main.integrations)
			{
				System.out.println(integration);
				for (String buttonKey : integration.getActions().keySet())
				{
					System.out.println("*" + buttonKey);
					final Action clickableButton = integration.getActions().get(buttonKey);
					final Button jfxButton = new Button(clickableButton.getName());
					jfxButton.setMinWidth(240);
					jfxButton.setMaxWidth(240);
					jfxButton.setAlignment(Pos.BASELINE_LEFT);
					jfxButton.setContentDisplay(ContentDisplay.RIGHT);
					jfxButton.setOnAction(new EventHandler<ActionEvent>()
					{
						@Override
						public void handle(ActionEvent arg0)
						{
							try
							{
								clickableButton.onAction();
								triggerEvent("<" + clickableButton.getName() + "> from frontend", null);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					});
					innerRightPane.addRow(row++, jfxButton);
				}
			}

			// Pause controls
			final Label label2 = JavaFxHelper.createLabel("Pause/Resume", Color.WHITE, "", new Insets(0, 0, 0, 3), 160, Control.USE_COMPUTED_SIZE);
			innerRightPane.addRow(row++, label2);

			int[] periods = new int[] { 5, 10, 15, 20, 25, 30, 45, 60, 90, 105, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720 };
			GridPane btnGridPane = null;
			for (int p = 0; p < periods.length; p++)
			{
				if (p % 3 == 0)
				{
					if (btnGridPane != null)
					{
						innerRightPane.addRow(row++, btnGridPane);
					}
					btnGridPane = new GridPane();
					btnGridPane.setHgap(9);
				}
				final int pp = periods[p];
				int hours = pp / 60;
				int minutes = pp % 60;
				final String hm = (((hours > 0) ? hours + "h" : "") + ((minutes > 0) ? minutes + "m" : ""));
				final Button pauseButton = JavaFxHelper.createButton(hm);
				pauseButton.setMinWidth(74);
				pauseButton.setMaxWidth(74);
				pauseButton.setAlignment(Pos.BASELINE_LEFT);
				pauseButton.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent arg0)
					{
						TextInputDialog dialog = new TextInputDialog("");
						dialog.setTitle("Pause for " + hm);
						dialog.setContentText("Please input why you are pausing:");

						// Traditional way to get the response value.
						Optional<String> result = dialog.showAndWait();
						if (result.isPresent() && !result.get().isEmpty())
						{
							long now = System.currentTimeMillis();
							pausedUntil = now + (pp * 60000);
							pauseReason = result.get();
							triggerEvent("Paused for " + hm + " (until " + CommonUtils.dateFormatter.format(pausedUntil) + ") for \"" + pauseReason + "\"", NMOConfiguration.instance.events.pauseInitiated);
						}
					}
				});
				pauseButton.disableProperty().bind(isCurrentlyPaused);
				btnGridPane.addColumn(p % 3, pauseButton);
			}
			if (btnGridPane != null)
			{
				innerRightPane.addRow(row++, btnGridPane);
			}
			final Button unpauseButton = JavaFxHelper.createButton("Unpause");
			unpauseButton.setMinWidth(240);
			unpauseButton.setMaxWidth(240);
			unpauseButton.setAlignment(Pos.BASELINE_LEFT);
			unpauseButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					pausedUntil = 0;
					isCurrentlyPaused.set(false);
					triggerEvent("Unpaused manually", NMOConfiguration.instance.events.pauseCancelled);
				}
			});
			unpauseButton.disableProperty().bind(isCurrentlyPaused.not());
			innerRightPane.addRow(row++, unpauseButton);

			rightPaneI.setCenter(innerRightPane);
		}

		//==================================================================
		// PAVLOK CRAP
		//==================================================================

		if (NMOConfiguration.instance.integrations.pavlok.enabled && NMOConfiguration.instance.integrations.pavlok.auth == null)
		{
			final String url = "https://pavlok-mvp.herokuapp.com/oauth/authorize?client_id=" + Main.CLIENT_ID + "&redirect_uri=" + Main.CLIENT_CALLBACK + "&response_type=code";
			BorderPane authPane = new BorderPane();
			WebView browser = new WebView();
			WebEngine webEngine = browser.getEngine();
			webEngine.load(url);
			authPane.setCenter(browser);
			webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>()
			{
				boolean handledCallbackYet = false;

				@Override
				public void handle(WebEvent<String> event)
				{
					if (!this.handledCallbackYet && event.getSource() instanceof WebEngine)
					{
						WebEngine we = (WebEngine) event.getSource();
						String location = we.getLocation();
						if (location.startsWith(Main.CLIENT_CALLBACK) && location.contains("?code"))
						{
							this.handledCallbackYet = true;
							System.out.println("Detected location: " + location);
							try
							{
								String[] params = location.split("\\Q?\\E")[1].split("\\Q&\\E");
								Map<String, String> map = new HashMap<String, String>();
								for (String param : params)
								{
									String name = param.split("=")[0];
									String value = param.split("=")[1];
									map.put(name, value);
								}
								NMOConfiguration.instance.integrations.pavlok.auth = IntegrationPavlok.postAuthToken(map.get("code"));
								Configuration.save();
								IntegrationPavlok.INSTANCE.vibration(255, "Connection test");
								triggerEvent("<VIBRATION> Connection test", null);
								outerPane.getChildren().clear();
								outerPane.getChildren().add(innerPane);
								at.start();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			});
			outerPane.getChildren().add(authPane);
		}
		else
		{
			outerPane.getChildren().add(innerPane);
			at.start();
		}

		//==================================================================
		// SHOW STAGE
		//==================================================================
		stage.setScene(scene);
		stage.show();
	}

	protected void tick()
	{
		tick++;
		if (tick >= 180)
		{
			tick -= 180;
			System.gc();
		}
		if (tick % 2 == 1)
		{
			return;
		}

		long now = System.currentTimeMillis();
		boolean paused = pausedUntil > now;
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int currentMinuteOfDay = ((hour * 60) + minute);

		SleepEntry nextSleepBlockDetected = null;
		for (SleepEntry entry : NMOConfiguration.instance.schedule)
		{
			if (entry.containsTime(currentMinuteOfDay) || entry.start >= currentMinuteOfDay)
			{
				nextSleepBlockDetected = entry;
				break;
			}
		}
		if (nextSleepBlockDetected == null && !NMOConfiguration.instance.schedule.isEmpty())
		{
			nextSleepBlockDetected = NMOConfiguration.instance.schedule.get(0);
		}
		if (nextSleepBlockDetected != null)
		{
			if (nextSleepBlockDetected.containsTime(currentMinuteOfDay))
			{
				Calendar calendar2 = Calendar.getInstance();
				calendar2.set(Calendar.HOUR_OF_DAY, nextSleepBlockDetected.end / 60);
				calendar2.set(Calendar.MINUTE, nextSleepBlockDetected.end % 60);
				calendar2.set(Calendar.SECOND, 0);
				calendar2.set(Calendar.MILLISECOND, 0);
				long tims = calendar2.getTimeInMillis();
				if (nextSleepBlockDetected.end < currentMinuteOfDay)
				{
					tims += 86400000L; // nap loops over to next day. add 1 day.
				}
				if (!scheduleStatus.startsWith("SLEEPING"))
				{
					triggerEvent("Entering sleep block: " + nextSleepBlockDetected.name, NMOConfiguration.instance.events.sleepBlockStarted);
				}
				scheduleStatus = "SLEEPING [" + nextSleepBlockDetected.name + "] UNTIL " + CommonUtils.convertTimestamp(tims);
				nextSleepBlock = nextSleepBlockDetected;
				if (!paused)
				{
					triggerEvent("Automatically pausing until " + CommonUtils.convertTimestamp(tims) + " due to sleep block '" + nextSleepBlockDetected.name + "' having started", null);
					paused = true;
					pausedUntil = tims;
					pauseReason = "Sleep block: " + nextSleepBlockDetected.name;
				}
			}
			else
			{
				Calendar calendar2 = Calendar.getInstance();
				calendar2.set(Calendar.HOUR_OF_DAY, nextSleepBlockDetected.start / 60);
				calendar2.set(Calendar.MINUTE, nextSleepBlockDetected.start % 60);
				calendar2.set(Calendar.SECOND, 0);
				calendar2.set(Calendar.MILLISECOND, 0);
				long tims = calendar2.getTimeInMillis();
				if (nextSleepBlockDetected.start < currentMinuteOfDay)
				{
					tims += 86400000L; // nap loops over to next day. add 1 day.
				}
				long minutesRemaining = (((tims + 59999) - System.currentTimeMillis()) / 60000);
				scheduleStatus = "AWAKE [" + nextSleepBlockDetected.name + " STARTS IN " + minutesRemaining + " MINUTE" + (minutesRemaining == 1 ? "" : "S") + "]";
				if (minutesRemaining == NMOConfiguration.instance.sleepBlockApproachingTimeMins && lastSleepBlockWarning != nextSleepBlockDetected)
				{
					triggerEvent(minutesRemaining + " minute" + (minutesRemaining == 1 ? "" : "s") + " until next sleep block", NMOConfiguration.instance.events.sleepBlockApproaching);
					lastSleepBlockWarning = nextSleepBlockDetected;
				}
			}
		}
		if (nextSleepBlock != null && nextSleepBlock != nextSleepBlockDetected)
		{
			triggerEvent("Exiting sleep block: " + nextSleepBlock.name, NMOConfiguration.instance.events.sleepBlockEnded);
		}
		boolean wasPaused = isCurrentlyPaused.get();
		isCurrentlyPaused.set(paused);
		if (!paused && wasPaused)
		{
			triggerEvent("Unpaused automatically - time alotted for \"" + pauseReason + "\" has expired", NMOConfiguration.instance.events.pauseExpired);
		}
		if (nextSleepBlockDetected != null && nextSleepBlock != nextSleepBlockDetected)
		{
			nextSleepBlock = nextSleepBlockDetected;

			Calendar calendar3 = Calendar.getInstance();
			calendar3.set(Calendar.HOUR_OF_DAY, nextSleepBlockDetected.start / 60);
			calendar3.set(Calendar.MINUTE, nextSleepBlockDetected.start % 60);
			calendar3.set(Calendar.SECOND, 0);
			calendar3.set(Calendar.MILLISECOND, 0);
			long tims = calendar3.getTimeInMillis();
			if (nextSleepBlockDetected.start < currentMinuteOfDay)
			{
				tims += 86400000L; // nap loops over to next day. add 1 day.
			}
			long minutesRemaining = (((tims + 59999) - System.currentTimeMillis()) / 60000);
			triggerEvent("The next sleep block is " + nextSleepBlockDetected.name + " which starts in " + minutesRemaining + " minute" + (minutesRemaining == 1 ? "" : "s"), null);
		}

		for (Integration integration : Main.integrations)
		{
			if (integration.isEnabled())
			{
				try
				{
					integration.update();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		if (paused)
		{
			resetActivityTimer();
		}
		if (NMOConfiguration.instance.integrations.pavlok.enabled)
		{
			loginTokenValidUntilString.set("Login token to Pavlok API expires on " + CommonUtils.dateFormatter.format(1000 * (NMOConfiguration.instance.integrations.pavlok.auth.created_at + NMOConfiguration.instance.integrations.pavlok.auth.expires_in)));
		}
		if (NMOConfiguration.instance.integrations.mouse.enabled)
		{
			lastCursorPositionString.set(paused ? "" : "Last cursor position: " + IntegrationMouse.lastCursorPoint.getX() + ", " + IntegrationMouse.lastCursorPoint.getY());
		}
		if (paused)
		{
			lastActivityTimeString.set("PAUSED for \"" + pauseReason + "\" until " + CommonUtils.dateFormatter.format(pausedUntil));
			timeDiffString.set("");
		}
		else
		{
			lastActivityTimeString.set("Last input activity: " + CommonUtils.dateFormatter.format(lastActivityTime));
			long timeDiff = paused ? 0 : (now - lastActivityTime);
			if (timeDiff > nextActivityWarningTimeDiff)
			{
				try
				{
					// the first time, you get an alternative lighter warning, just in case you forgot to pause
					if (nextActivityWarningTimeDiff == NMOConfiguration.instance.activityWarningTimeInitialMs)
					{
						triggerEvent("No activity detected for " + (nextActivityWarningTimeDiff / 1000) + " seconds", NMOConfiguration.instance.events.activityWarning1);
					}
					else
					{
						triggerEvent("No activity detected for " + (nextActivityWarningTimeDiff / 1000) + " seconds", NMOConfiguration.instance.events.activityWarning2);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				nextActivityWarningTimeDiff += NMOConfiguration.instance.activityWarningTimeIncrementMs;
			}
			timeDiffString.set("Time difference: " + timeDiff + " (next activity warning: " + nextActivityWarningTimeDiff + ")");
		}

		try
		{
			BufferedImage img = WebcamCapture.getImage();
			if (img != null && tick % 4 < 2)
			{
				writableImage = SwingFXUtils.toFXImage(img, writableImage);
				img.flush();
				lastWebcamImage.set(writableImage);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void resetActivityTimer()
	{
		lastActivityTime = System.currentTimeMillis();
		nextActivityWarningTimeDiff = NMOConfiguration.instance.activityWarningTimeInitialMs;
	}

	public static void triggerEvent(String eventDescription, String[] actionArray)
	{
		ArrayList<Action> actionsByLookup = new ArrayList<Action>();
		String actionString = "";
		if (actionArray != null)
		{
			actionLoop: for (String action : actionArray)
			{
				for (Integration integration : Main.integrations)
				{
					LinkedHashMap<String, Action> iactions = integration.getActions();
					Action aaction = iactions.get(action);
					if (aaction != null)
					{
						actionsByLookup.add(aaction);
						actionString += (actionString.isEmpty() ? "" : ", ") + aaction.getName();
						continue actionLoop;
					}
				}
			}
			for (Action aaction : actionsByLookup)
			{
				try
				{
					aaction.onAction();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		final String eventString = (actionString.isEmpty() ? "" : "<" + actionString + "> ") + eventDescription;
		log.info("APPEVENT: " + eventString);
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				events.add(CommonUtils.dateFormatter.format(System.currentTimeMillis()) + ": " + eventString);
			}
		});
	}
}

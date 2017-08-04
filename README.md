# NoMoreOversleeps #

NoMoreOversleeps (NMO) is a very simple JavaFX application which is designed to help you adapt to a new polyphasic sleeping schedule. NMO is essentially an alarm clock on steroids -
it tries to make sure you are awake during waking blocks, and sleeping during naps.

It was originally created only for my own use, so some parts of the software are a little crude and there are a lot of rough edges. The procedure to set up NMO is also a bit convoluted.
I'm slowly working on improving it to make it more suitable for use by others, but in the meantime you can consider it an alpha version. Despite this, there have been a couple of people
now besides me who have made use of the program and its popularity is slowly rising. Hopefully you can find it helpful for your own polyphasic adaptations.

### Features ###

NoMoreOversleeps consists of two parts - an automated background monitor and a manual remote control. The automated part will try to wake you back up if it thinks you are asleep when
you shouldn't be. The manual portion offers the ability for a third party to monitor you remotely, so that they can do the same. In this way, you have the advantage of both a computer
and a human (or more than one) being able to monitor you, which will hopefully allow you to avoid any unwanted oversleeps.

Most parts of the program are configurable, with the number of customization options being slowly expanded. The software goes with a modular approach, allowing both the automated tracking
system and the selection of wake-up actions to be tailored to your needs as desired.

#### Automated monitoring ####

To determine if you are asleep when you shouldn't be, NoMoreOversleeps takes a look at your sleeping schedule and compares it with the system clock. If it is currently a time when you
should be awake, NoMoreOversleeps monitors the activity of input devices on your computer (currently supporting keyboard, mouse, Xbox360-compatible controllers and MIDI devices) and will
try to get your attention every X seconds if it fails to detect activity in any of those devices within a Y minute interval. The number of seconds, the detection interval and the method
used to get your attention can be customized as desired; you can also set multiple of these for different situations.

In the event you are deliberately going to be away from your computer for a certain amount of time, e.g. you're going shopping or going to work, you can pause the activity detection
feature of NoMoreOversleeps for a choosable length of time between 5 minutes and 12 hours. This will prevent NoMoreOversleeps from trying to get your attention. When pausing the
automated monitoring system, you are required to input the reason for the pause.

The pause function is also automatically activated during each of your sleep blocks in order to avoid a scenario where you go to sleep on time but get woken up mid-nap because you
forgot to pause the activity tracking.

An action of your choice can also be automatically performed X minutes before each sleep block starts so that you remember to go to sleep on time.

#### Manual monitoring ####

NoMoreOversleeps features a port-forwardable password-protected web UI which displays what part of your sleep schedule you are currently in, along with a live webcam feed. This allows
people to remotely monitor you, letting them see whether or not you are at your computer, along with how awake you are, if you're nodding off in front of your screen, etc.

In the event that the people who are monitoring you think that you're asleep at the wrong time, they can perform any of the actions you have configured to try and wake you up.

#### Configurable actions ####

The following actions can currently be configured for use with both automated and manual monitoring:

* **Activity warning timer change**: Switch to a different activity warning timer
* **Noise/Sound playback**: Configure any number of sounds of your choice
* **Command line execution**: Run any application on your machine
* **Pavlok**: Beep, vibrate or shock
* **Twilio**: Call a phone number (e.g. your mobile)
* **Philips Hue smart light bulbs**: Turn the light bulbs on or off

In particular, the command line execution can be particularly valuable, as it allows you to link NMO to other utility programs to perform more advanced functionality beyond that which
is offered by NMO out of the box. For example, a popular utility for NMO is [NirCmd](http://www.nirsoft.net/utils/nircmd.html) which can be used to create actions that automatically
unmute your sound card, change system volume to maximum and/or switch your sound output back to speakers if you left your headphones plugged in - then these actions can be remotely
triggered from the Web UI or as part of your alarm routine.

In the future, the list of supported actions will hopefully be expanded. If you're good at programming in Java, feel free to add your own (the implementation of new actions is quite simple).

#### Live text file data output ####

NoMoreOversleeps can optionally output text files containing information such as current schedule, time since start of schedule, time since last oversleep, personal best time, and current
schedule status (time remaining of/until sleep block). These text files can be used with software such as OBS to overlay the data onto a video stream, allowing you to live-stream your
polyphasic sleeping attempt data to Twitch, or even just render the data onto a personal local recording of your polyphasic sleeping attempt for archival or investigation purposes.

### Features that might be added in the future ###

Some ideas I've had include:

* Better GUI to allow for configuration of the software without having to edit the config file by hand
* Load the sleep schedule from a Napchart link rather than having to configure it by hand
* GPS tracking of phone so you can see where someone is if they aren't at their computer
* Tracking function so you can record your sleeping (and possible oversleeping) along with how good each sleep/nap felt
* Send a message to the polysleeping Discord if you fail to wake up so that you can be really embarrased for being such a huge failure
* Food tracker so you can monitor what you're eating in case this affects your sleep schedule
* Productivity tracker so you can input what you're doing in each sleep block to avoid wasting time
* Integration with EEG to track SWS/REM acquisition

I probably won't implement most of these, but we will see.

### General disclaimer ###

NoMoreOversleeps was designed to be used by people who mostly sit at their computer, have access to most/all of the integration features, and have a reliable buddy to watch their feed. While it can be
quite an effective alarm when used properly, it can be entirely useless if used wrongly. If you choose to use NMO, then you do so at your own risk - I will not take any responsibility for the success or
failure of your polyphasic adaptation ;)

### Setup and usage instructions ###

A pre-compiled version of the latest supported release can be found pinned to `#sleep_tech` in the Polyphasic Sleep Discord. This should be compatible with Windows, Mac and Linux.

After downloading the package, you should unzip it and launch the application to generate a base config file which can be edited. You must then modify the config files to your liking (`config.json` turns
features on/off and configures them, and `webusers.properties` contains a list of username/password combos which are valid to access the Web UI). Some example config files are located in the `sample-configs`
folder so you can see how the configuration of each section works, because from the blank autogenerated config file it is not fully obvious.

If you want the Web UI to be accessible from the internet, you should set up a dynamic DNS service (I would recommend [DuckDNS](https://www.duckdns.org/) myself) and then port-forward the application in your
router control panel (the default port is 19992, but you can change it in the config file if you wish). An automatic port-forward function is accessible from the Web UI if you're not sure how to do that part,
but the dynamic DNS must currently be set up by hand. This requires a little bit of technical knowledge - if you have no idea how to do this, you can consult me for help.

### Development instructions ###

Here is the basic procedure:

* Download the code (preferably using `hg clone` so that you can pull and update to future versions easily)
* Open a command window or terminal in the folder where you placed the code
* Use the gradle wrapper to generate project files for the IDE of your choice (either `gradlew eclipse` or `gradlew idea`)
* Import the project into your IDE and run the application from the Main class to verify it launches
* Close the application
* Go into the `run` folder (it should be next to the `src` folder) and modify the configs
* Restart the application

To update the code to the newest release, the procedure is simply to `hg pull` and `hg update`. After that, simply run `gradlew eclipse` or `gradlew idea` again in order to rebuild the IDE project for latest version.

### Known issues ###

* Occasionally logging in to Pavlok just results in a 404 screen. I have no idea why. Just keep restarting the app and retrying until it works.
* When your login key to Pavlok API expires it isn't renewed. This doesn't seem to make any difference because it doesn't appear to matter that it's expired and is accepted by the API anyway :/
* The Pavlok integration is not very useful due to the glitchy and unreliable nature of the Pavlok's bluetooth connection and push notifications.
* The webcam feed isn't visible on iOS devices because the authentication is not passed along to the web socket. Need to come up with a strategy to fix this.
* The log in the web UI flashes because it was implemented as a refreshing iframe. It should eventually be replaced with Ajax.
* Pressing buttons in the web UI is done as a form action, causing the entire page to reload, including a reset of the webcam socket. It should eventually be replaced with Ajax.
* Currently the webcam feed has to be uploaded separately to every person watching your feed and does not include any form of automated frame skip. This means if you have slow upload the webcam feed can fall behind.

### Contribution guidelines ###

This program is open source and completely free. If you want to contribute then you can fork the project, patch it, and submit a pull request. As long as your patch works and your code is not awful, I'll probably accept it.

### Who do I talk to? ###

If you have any queries about this program please contact Robert Dennington (Tinytimrob) over Discord or via e-mail, tinytimrob [at] googlemail [dot] com
# NoMoreOversleeps #

NoMoreOversleeps is a very simple JavaFX application which is designed to help you adapt to a new polyphasic sleeping schedule. It was originally created only for my own use, so some parts
of the software are a little crude, but I'm slowly working on improving it to make it more suitable for use by others. Hopefully you can find it helpful for your own polyphasic adaptations.

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
used to get your attention can be customized as desired.

In the event you are deliberately going to be away from your computer for a certain amount of time, e.g. you're going shopping or going to work, you can pause the activity detection
feature of NoMoreOversleeps for a choosable length of time between 5 minutes and 12 hours. This will prevent NoMoreOversleeps from trying to get your attention. When pausing the
automated monitoring system, you are required to input the reason for the pause.

The pause function is also automatically activated during each of your sleep blocks in order to avoid a scenario where you go to sleep on time but get woken up mid-nap because you
forgot to pause the activity tracking.

An action of your choice can also be automatically performed X minutes before each sleep block starts so that you remember to go to sleep on time.

#### Manual monitoring ####

NoMoreOversleeps features a port-forwardable password-protected web UI which displays what part of your sleep schedule you are currently in, along with a live webcam feed. This allows
people to remotely monitoring you, and to see whether or not you are at your computer, along with how awake you are, if you're nodding off in front of your screen, etc.

In the event that the people who are monitoring you think that you're asleep at the wrong time, they can perform any of the actions you have configured to try and wake you up.

#### Configurable actions ####

The following actions can currently be configured for use with both automated and manual monitoring:

* **Noise/Sound playback**: configure any number of sounds of your choice
* **Command line execution**: run any application on your machine
* **Pavlok**: Beep, vibrate or shock
* **Twilio**: Call a phone number (e.g. your mobile)
* **Philips Hue smart light bulbs**: Turn the light bulbs on or off

In the future the list of supported actions will hopefully be expanded.

### Best usage ###

NoMoreOversleeps is designed for continuous monitoring. It works best if you mostly sit at your computer, have access to most/all of the integration features, and have a reliable buddy to
watch your feed.

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

### Known issues ###

* The RAM usage is too high. There may be a possible memory leak somewhere I haven't found. It appears to be related to the webcam code, as it worked fine prior to that :/
* Some features only work on Windows. For other platforms YMMV. It is possible that right now the application even would just crash out on other platforms or be largely unusable (because I haven't tested it).
* When your login key to Pavlok API expires it isn't renewed. This doesn't seem to make any difference because it doesn't appear to matter that it's expired and is accepted by the API anyway :/
* The Pavlok integration is not very useful due to the glitchy and unreliable nature of the Pavlok's bluetooth connection and push notifications.
* The webcam feed isn't visible on iOS devices because the authentication is not passed along to the web socket. Need to come up with a strategy to fix this.
* The log in the web UI flashes because it was implemented as a refreshing iframe. It should eventually be replaced with Ajax.
* Pressing buttons in the web UI is done as a form action, causing the entire page to reload, including a reset of the webcam socket. It should eventually be replaced with Ajax.

### Contribution guidelines ###

This program is open source and completely free. If you want to contribute then you can fork the project, patch it, and submit a pull request. As long as your patch works and your code is not awful, I'll probably accept it.

### Who do I talk to? ###

If you have any queries about this program please contact Robert Dennington (Tinytimrob) over Discord or via e-mail, tinytimrob [at] googlemail [dot] com
# NoMoreOversleeps #

NoMoreOversleeps is a very simple JavaFX application which is designed to help you adapt to a new polyphasic sleeping schedule by making oversleeping more difficult.
Currently it does this by zapping your Pavlok if it detects no activity on your computer within a certain time interval (in case you fell asleep or failed to wake up on time).
Activity detection is supported on keyboard, mouse and Xbox360-compatible controller.

In addition, it includes a port-forwardable password-protected web UI with a live webcam feed, along with buttons to get your attention in various ways:

* Send beep/vibrate/shock to a Pavlok
* Call a switchboard or mobile using Twilio
* Play a noise out of computer speakers/headphones

This gives people the ability attempt to wake you up via whatever means they feel is necessary if you appear to be oversleeping.

You can pause the activity detection feature of NoMoreOversleeps while you're intentionally doing other stuff, e.g. if you're going shopping or going to work and thus you will be AFK
for a while. The pause reason and duration is displayed on both the JavaFX frontend and on the web UI.

At the moment it has no other features.

### Best usage ###

NoMoreOversleeps is designed for continuous monitoring works best if you have access to all of the integration features, it also 

### Features that might be added in the future ###

It would be nice to turn this into a general tool to help people adapt to and track their sleeping schedules. On that basis there are lots of good possibilites. Some ideas I've had include:

* Support for controlling smart lighting
* GPS tracking of phone so you can see where someone is if they aren't at their computer
* Input your sleeping pattern so you don't have to manually pause during this period
* Tracking function so you can record your sleeping (and possible oversleeping) along with how good each sleep/nap felt
* Automate the calling and noise playback if you fail to wake up
* Send a message to the polysleeping Discord if you fail to wake up so that you can be really embarrased for being such a huge failure
* Food tracker so you can monitor what you're eating in case this affects your sleep schedule
* Productivity tracker so you can input what you're doing in each sleep block to avoid wasting time
* Integration with EEG to track SWS/REM acquisition

I probably won't implement most of these, but we will see.

### Known issues ###

* The RAM usage is too high. There may be a possible memory leak somewhere I haven't found. It appears to be related to the webcam code, as it worked fine prior to that :/
* Some features only work and/or were only tested on Windows. For other platforms YMMV.

### Contribution guidelines ###

This program is open source and completely free. If you want to contribute then you can fork the project, patch it, and submit a pull request. As long as your patch works and your code is not awful, I'll probably accept it.

### Who do I talk to? ###

If you have any queries about this program please contact Robert Dennington (Tinytimrob) over Discord or via e-mail, tinytimrob [at] googlemail [dot] com
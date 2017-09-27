# Encephalophone-GUI

### A supercollider UI developed for calibration, testing, and usage of the encephalophone

## Help


The code below provides instruction on installation and use of the Encephalophone-GUI
Copy the into a blank .scd file for use

```
// To run code..
// --------------
// 1) Click click inside of parenthesis
// 2) Mac: Command + Enter
//    PC:  Control + Enter

(
// To install Encephalophone-GUI 
// --------------
Quarks.install("https://github.com/teamencephalophone/Encephalophone-GUI");
)

// Code and data location ..
// --------------
// 1) Click file
// 2) Open User Support Directory
// 3) Click downloaded-quarks
// 4) Click Encephalophone-GUI

(
// Runs Encephalophone GUI
Encephalophone-GUI(s)
)

// Check OSC messages from /fred
(
var oscFunc, oscdef;
            oscFunc = { |msg, time, addr|
                    ("getting - " + msg[1]).postln;
            };
            oscdef = OSCdef(\aName, oscFunc, '/fred');
)
// Stops OSC messages
OSCdef(\aName).free
```

The following code is a processing sketch for generating OSC Messages internally 

```
import oscP5.*;
import netP5.*;
OscP5 osc;
NetAddress supercollider;
void setup() {
  frameRate(2);
  osc = new OscP5(this, 12000);
  supercollider = new NetAddress("127.0.0.1", 57120);
}
void draw() {
  background(0);
  
      OscMessage msg = new OscMessage("/fred");
      int num = (int) random(8) + 1;
      msg.add(num);
      osc.send(msg, supercollider);
      println(num);
}
```

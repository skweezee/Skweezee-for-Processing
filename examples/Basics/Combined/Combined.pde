/*

  Instructions: Run sketch and squeeze the object; the 
  circle changes size, color, and position according to
  the squeeze.
  
  Note that here, multiple parameters depend on the squeeze:
  size, both color hue and satruation, and position change.
  
  You might notice a 'strange' behavior at the beginning:
  this is caused by a continuously defined reference point
  as 'the  maximum observed squeeze'. In the beginning,
  this shifting quickly following the first squeezes,
  resulting in this specific behavior.
 
  The quickest way to get rid of it is to start off with a
  firm squeeze! From then on, the reference point is set to
  this squeeze. If you happen to squeeze harder afterwards,
  the reference point will be adjusted, but chance is low
  this will change much.
  
 */

import net.skweezee.processing.*;
import processing.serial.*;

void setup() {
  
  // set environment
  size(400, 400);
  colorMode(HSB, 360, 100, 100);  // I prefer working in HSB mode when changing color
  background(0, 0, 92);
  fill(9, 68, 97);
  noStroke();
  
  /*** CONNECT SKWEEZEE (2 options: automatic, or manual) ***/
  
  // 1. Automatic connection (on Mac only)
  Skweezee.connect(this);  // COMMENT OR REMOVE THIS LINE IF YOU NEED OR WISH TO CONNECT MANUALLY
  
  // 2. Manual connection on Windows & Mac
  
  /* ----- REMOVE THIS LINE IF YOU NEED OR WISH TO CONNECT MANUALLY ---
  
    // list of serial ports is printed, run sketch to identify the correct PORT_NR
    // and complete Skweezee.connect(this, PORT_NR); with the actual number.
    printArray(Serial.list());  // this line can be commented once the correct PORT_NR is identified
    int PORT_NR = 0;  // change with correct port number
    Skweezee.connect(this, PORT_NR);
  
  */ //--- REMOVE THIS LINE IF YOU NEED OR WISH TO CONNECT MANUALLY ---
  
  
  // optional, optimize frame rate for visual display
  frameRate(30);

}

void draw() {
  
  background(0, 0, 92);

  // read squeeze signal, result is number from 0 to 1
  // see reference for more options
  float squeeze = Skweezee.norm();
  
  // min means parameter when the squeeze equals '0'
  // max means parameter when the squeeze equals '1' (maximum)
  
  float margin = height/10;
  
  float minR = margin;
  float maxR = 2*margin;
  
  float minH = 9;   // hue 9°: coral
  float maxH = 36;  // hue 36°: yellow
  float minS = 20;  // saturation 20%: faded
  float maxS = 80;  // saturation 80%: saturated
  
  float minX = margin;
  float maxX = width-margin;
  float minY = height-margin;
  float maxY = height-2*margin;
  
  float r = minR + (maxR - minR)*squeeze;
  float h = minH + (maxH - minH)*squeeze;
  float s = minS + (maxS - minS)*squeeze;
  float x = minX + (maxX - minX)*squeeze;
  float y = minY + (maxY - minY)*squeeze;
  
  fill(h, s, 97);
  ellipse(x, y, r, r);
  
}

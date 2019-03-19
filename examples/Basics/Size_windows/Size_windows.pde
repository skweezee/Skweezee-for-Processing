/*

  Instructions: Run sketch and squeeze the object; the 
  circle changes size according to the squeeze.
  
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
  background(235);
  fill(210);
  noStroke();
  
  /*** CONNECT SKWEEZEE (2 options: automatic, or manual) ***/
  
  // 1. Automatic connection (on Mac only)
  // Skweezee.connect(this);  // UNCOMMENT THIS LINE IF YOU NEED OR WISH TO CONNECT AUTOMATICALLY
  
  // 2. Manual connection on Windows & Mac
  // COMMENT THE BLOCK BELOW IF YOU NEED OR WISH TO CONNECT AUTOMATICALLY
  
    // list of serial ports is printed, run sketch to identify the correct PORT_NR
    // and complete Skweezee.connect(this, PORT_NR); with the actual number.
    printArray(Serial.list());  // this line can be commented once the correct PORT_NR is identified
    int PORT_NR = 0;  // change with correct port number
    Skweezee.connect(this, PORT_NR);
  
  // optional, optimize frame rate for visual display
  frameRate(30);

}

void draw() {
  
  background(235);
   
  // read squeeze signal, result is number from 0 to 1
  // see reference for more options
  float squeeze = Skweezee.norm();
  
  // min means the radius when the squeeze equals '0' (
  // max means the radius when the squeeze equals '1' (maximum)
  float min = height/10;
  float max = 2*height/3;
  
  float x = min+(max-min)*squeeze;
  ellipse(width/2, height/2, x, x);
  
}

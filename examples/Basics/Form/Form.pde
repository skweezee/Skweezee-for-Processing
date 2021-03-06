/*

  Instructions: Run sketch and squeeze the object; the 
  form changes according to the squeeze.
  
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

ArrayList<PVector> points = new ArrayList<PVector>();
int w = 250;
  int h = 200;
  
void setup() {
  
  // set environment
  size(400, 400);
  background(235);
  fill(210);
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
  
  points.add(new PVector(-w/2, h/2));
  points.add(new PVector(w/2, h/2));
  points.add(new PVector(w/2, -h/2));
  points.add(new PVector(-w/2, -h/2));
  
  // optional, optimize frame rate for visual display
  frameRate(30);

}

void draw() {
  
  background(235);

  // read squeeze signal, result is number from 0 to 1
  // see reference for more options
  float squeeze = Skweezee.norm();
  
  curveTightness(0.5*Skweezee.square());
    
  float dy1 = 20*Skweezee.square();
  float dy2 = 20*(1-Skweezee.root());
  
  points.set(0, new PVector(-w/2-dy1/3, h/2-dy2)); 
  points.set(1, new PVector(w/2+dy1/3, h/2-dy2));
  points.set(2, new PVector(w/2, -h/2+dy1)); 
  points.set(3, new PVector(-w/2, -h/2+dy1));
  
  pushMatrix();
  translate(width/2, height/2);
  beginShape();
  for(int i = 0; i < points.size()+3; i++) {
    int index = i;
    if(i >= points.size()) index = i - points.size();
      curveVertex(points.get(index).x, points.get(index).y); 
  }
  endShape();
  popMatrix();
  
}

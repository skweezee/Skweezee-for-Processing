import net.skweezee.processing.*;
import processing.serial.*;

void setup() {
  
  // set environment
  size(700, 300);
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
  
  
  // optional, optimize frame rate for visual display
  frameRate(30);

}

void draw() {
  
  background(235);
  fill(120);
  noStroke();
  // read squeeze signal, result is number from 0 to 1
  // see reference for more options
  textAlign(CENTER, CENTER);
  ellipse(75+50, height/2, 25+75*Skweezee.norm(), 25+75*Skweezee.norm());
  text("NORM", 75+50, height/2+60);
  ellipse(75+100+50+50, height/2, 25+75*Skweezee.inv(), 25+75*Skweezee.inv());
  text("INV", 75+100+50+50, height/2+60);
  ellipse(75+100+50+100+50+50, height/2, 25+75*Skweezee.square(), 25+75*Skweezee.square());
  text("SQUARE", 75+100+50+100+50+50, height/2+60);
  ellipse(75+100+50+100+50+100+50+50, height/2, 25+75*Skweezee.root(), 25+75*Skweezee.root());
  text("ROOT", 75+100+50+100+50+100+50+50, height/2+60);
  
  noFill();
  stroke(255);
  ellipse(75+50, height/2, 25, 25);
  ellipse(75+100+50+50, height/2, 25, 25);
  ellipse(75+100+50+100+50+50, height/2, 25, 25);
  ellipse(75+100+50+100+50+100+50+50, height/2, 25, 25);
  ellipse(75+50, height/2, 100, 100);
  ellipse(75+100+50+50, height/2, 100, 100);
  ellipse(75+100+50+100+50+50, height/2, 100, 100);
  ellipse(75+100+50+100+50+100+50+50, height/2,100, 100);
}

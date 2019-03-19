import net.skweezee.processing.*;
import processing.serial.*;

void setup() {
  
  // set environment
  size(400, 400);
  background(235);
  fill(120);
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
  
  textAlign(CENTER, TOP);
  text("Press any key to (re-)record a form", width/2, 20);

  textAlign(CENTER, TOP);
  ArrayList<Form> list = (ArrayList<Form>) Skweezee.forms();
  for(int i = 0; i < list.size(); i++) {
    rect(10+i*50, height-50-Skweezee.rcg(list.get(i).label())*(height-100), 40, Skweezee.rcg(list.get(i).label())*(height-100));
    text(list.get(i).label(), 10+i*50+20, height-45);
    text(((int) (Skweezee.rcg(list.get(i).label())*100)), 10+i*50+20, height-30);
  }
  
}

void keyPressed() {

  Skweezee.rcd(key+"");
  
}

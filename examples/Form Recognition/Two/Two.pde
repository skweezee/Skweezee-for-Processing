import net.skweezee.processing.*;
import processing.serial.*;

int i;

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
  
  i = 1;
  
  // optional, optimize frame rate for visual display
  frameRate(30);

}

void draw() {
  
  background(235);
  
  textAlign(CENTER, TOP);
  if(i == 1) {
    text("Press any key to (re-)record FORM ONE", width/2, 20);
  } else if (i == 2) { 
    text("Press any key to (re-)record FORM TWO", width/2, 20);
  }

  rect(width/2-50, height-50-Skweezee.rcg("one")*(height-100), 40, Skweezee.rcg("one")*(height-100));
  rect(width/2+10, height-50-Skweezee.rcg("two")*(height-100), 40, Skweezee.rcg("two")*(height-100));
  
  textAlign(CENTER, TOP);
  text("ONE", width/2-30, height-45);
  text("TWO", width/2+30, height-45);
  text(((int) (Skweezee.rcg("one")*100)), width/2-30, height-30);
  text(((int) (Skweezee.rcg("two")*100)), width/2+30, height-30);
}

void keyPressed() {

  if (i == 1) {
    
    Skweezee.rcd("one");
    i++;
    
  } else if (i == 2) {
    
    Skweezee.rcd("two");
    i = 1;
  
  } else { i = 1; }
  
}

/*

  Instructions: Run sketch and squeeze the object;
  the bar graph shows raw data per pair of electrodes.
  
  Note that the raw measurements reverse squeezes:
  255 is the default, minimum value (meaning max resistance);
  0 is the theoretical maximum value (meaning resistance = 0).

 */

import net.skweezee.processing.*;
import processing.serial.*;

void setup() {
  
  // set environment
  size(700, 400);
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

  int margin = 24;
  int offset = 50;
  int hspace = 6;
  int vspace = 9;
  int bar = 12;
  int max = 255;
  

  // read squeeze signal, result is number from 0 to 1
  // see reference for more options
  
  
  // printArray(Skweezee.raw(i));
  
  for(int i = 0; i < 8; i++) {
    
    int[] sub = Skweezee.raw(i);
    
    for(int j = 0; j < sub.length; j++) {
      fill(210);
      rect( 24 + i*70 + j*7, 24, 6, 255);
      fill(120);
      rect( 24 + i*70 + j*7, 24 + 255 - sub[j], 6, sub[j]);
      
      textAlign(LEFT, TOP);
      text(Skweezee.LABELS[Skweezee.SUBVECTOR_MAP[i][j]] + ": " + sub[j] , 24 + i*70, 24 + 255 + 4 + 14*j);
      
    }
    
      textAlign(LEFT, BOTTOM);
      text("e"+i, 24 + i*70, 20);
  }
  
}

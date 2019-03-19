/*

  Instructions: Run sketch and squeeze the object;
  the bar graph shows raw data per pair of electrodes.
  
  Note that the raw measurements reverse squeezes:
  255 is the default, minimum value (meaning max resistance);
  0 is the theoretical maximum value (meaning resistance = 0).

 */

import net.skweezee.processing.*;
import processing.serial.*;

ArrayList<Float> amplitude = new ArrayList<Float>();
ArrayList<Float> amplitude0 = new ArrayList<Float>();
ArrayList<Float> amplitude1 = new ArrayList<Float>();
ArrayList<Float> amplitude2 = new ArrayList<Float>();
ArrayList<Float> amplitude3 = new ArrayList<Float>();
ArrayList<Float> amplitude4 = new ArrayList<Float>();
ArrayList<Float> amplitude5 = new ArrayList<Float>();
ArrayList<Float> amplitude6 = new ArrayList<Float>();
ArrayList<Float> amplitude7 = new ArrayList<Float>();

void setup() {

  size(1300, 785);
  colorMode(HSB, 360, 100, 100);
  background(0, 0, 98);
  noStroke();
  Skweezee.connect(this);

  frameRate(25);
}

void draw() {

  background(0, 0, 98);
  noStroke();
  fill(0, 0, 95);
  rect(10, 10, 200, 380);
  rect(215, 10, 675, 380);
  rect(895, 10, 395, 380);
  rect(10, 395, 200, 380);
  rect(215, 395, 675, 380);
  rect(895, 395, 395, 380);

  float m = Skweezee.mag();
  getAmplitudes();
  

  // draw bar graph current features
  textSize(8);
  noStroke();
  textAlign(LEFT);
  for (int i = 0; i < Skweezee.vector().length; i++) {
    fill(210, 40, 15);
    rect(20, 19+13*i, 180*Skweezee.vector()[i], 10);
    fill(210, 0, 100);
    text(i, 25, 27+13*i);
  }
  
  // draw bar graph points
  textSize(12);
  noStroke();
  textAlign(LEFT);
  for (int i = 0; i < 8; i++) {
    fill(210, 40, 15);

    if(Skweezee.mag(i) > 0) rect(20, 399+47*i, (180*Skweezee.norm(i)), 30);
    if(i == 3) { 
      fill(90, 100, 70);
    } else if (i == 6) {
      fill(20, 100, 80);
    } else {
      fill(210, 0, 100);
    }
    text(i, 25, 420+47*i);
  }


  // draw line graph
  noFill();
  strokeWeight(1);
  stroke(210, 60, 15);

  beginShape();
  int speed = 2;
  int s = amplitude.size();
  int p = 0;
  int i = 0;
  float a = amplitude.get(s-1);
  float y = 0;
  if (s > (655/speed)) p = s - (655/speed);
  for (i = p; i < s; i++) {
    y = 10+10+360 - amplitude.get(i)/5.3 * 360;
    vertex(225+(i-p)*speed, y);
  }
  endShape();
  
  // draw filtered line graph
  /*noFill();
  strokeWeight(1);
  stroke(210, 60, 15);

  beginShape();
  int speeda = 2;
  int sa = filtered.size();
  int pa = 0;
  int ia = 0;
  float ya = 0;
  if (sa > (655/speeda)) pa = sa - (655/speeda);
  for (ia = pa; ia < sa; ia++) {
    ya = filtered.get(ia);
    ya = 380*(ya/255);
    vertex(225+(ia-pa)*speeda, 10+ya);
  }
  endShape();  
  */
  
  // draw line graph
  noFill();
  strokeWeight(1);
  stroke(210, 10, 70);
  
  beginShape();
  int z = amplitude0.size();
  p = 0;
  i = 0;
  y = 0;
  if (z > (655/speed)) p = z - (655/speed);
  for (i = p; i < z; i++) {
    y = 395+10+360-360*amplitude0.get(i)/2.6;
    vertex(225+(i-p)*speed, y);
  }
  endShape();
  
  beginShape();
  z = amplitude1.size();
  p = 0;
  i = 0;
  y = 0;
  if (z > (655/speed)) p = z - (655/speed);
  for (i = p; i < z; i++) {
    y = 395+10+360-360*amplitude1.get(i)/2.6;
    vertex(225+(i-p)*speed, y);
  }
  endShape();
    
  beginShape();
  z = amplitude2.size();
  p = 0;
  i = 0;
  y = 0;
  if (z > (655/speed)) p = z - (655/speed);
  for (i = p; i < z; i++) {
    y = 395+10+360-360*amplitude2.get(i)/2.6;
    vertex(225+(i-p)*speed, y);
  }
  endShape();
  
  beginShape();
  z = amplitude4.size();
  p = 0;
  i = 0;
  y = 0;
  if (z > (655/speed)) p = z - (655/speed);
  for (i = p; i < z; i++) {
    y = 395+10+360-360*amplitude4.get(i)/2.6;
    vertex(225+(i-p)*speed, y);
  }
  endShape();

  beginShape();
  z = amplitude5.size();
  p = 0;
  i = 0;
  y = 0;
  if (z > (655/speed)) p = z - (655/speed);
  for (i = p; i < z; i++) {
    y = 395+10+360-360*amplitude5.get(i)/2.6;
    vertex(225+(i-p)*speed, y);
  }
  endShape();
  
  beginShape();
  z = amplitude7.size();
  p = 0;
  i = 0;
  y = 0;
  if (z > (655/speed)) p = z - (655/speed);
  for (i = p; i < z; i++) {
    y = 395+10+360-360*amplitude7.get(i)/2.6;
    vertex(225+(i-p)*speed, y);
  }
  endShape();  
  
  stroke(90, 100, 70);  
  beginShape();
  z = amplitude6.size();
  p = 0;
  i = 0;
  y = 0;
  float c = 0;
  if(z > 1) c = amplitude6.get(z-1);
  if (z > (655/speed)) p = z - (655/speed);
  for (i = p; i < z; i++) {
    y = 395+10+360-360*amplitude6.get(i)/2.6;
    vertex(225+(i-p)*speed, y);
  }
  endShape();
 
  
  
  stroke(20, 100, 80);  
  beginShape();
  z = amplitude3.size();
  p = 0;
  i = 0;
  y = 0;
  float b = 0;
  if(z > 1) b = amplitude3.get(z-1);
  if (z > (655/speed)) p = z - (655/speed);
  for (i = p; i < z; i++) {
    y = 395+10+360-360*amplitude3.get(i)/2.6;
    vertex(225+(i-p)*speed, y);
  }
  endShape();

  // draw stable
  fill(255);
  textSize(12);
  textAlign(LEFT);
  text("std: "+nf(getStdDev(), 1, 2), 800, 50);
  if(getStdDev() < 0.1) text("stable", 800, 30);
  else  text("not stable",800, 30);
  if(getStdDev() < 0.1) fill(90, 100, 90);
  else fill(10, 100, 80);
  noStroke();
  ellipse(870, 25, 12, 12);


  // output
  // Canvas: rect(895, 10, 395, 380);
      fill(10+20*Skweezee.norm(), 100, 100);
      float r = 40+Skweezee.norm()*240;
    //}

    noStroke();
    ellipse(1092, 200, r, r);

  
  
  
  
  
    /*if (!invert) {
      fill((180+30*int(a)/255), 90, 100);
      rx = (40+240*int(b*b/a)/255);
      ry = (40+240*int(c*c/a)/255);
    } else {*/
      a = Skweezee.norm();
      fill((180+30*a), 90, 100);
      float rx = (40+240*Skweezee.square(3));
      float ry = (40+240*Skweezee.square(6));
    //}

    noStroke();
    ellipse(1092, 600, rx, ry);

  //sine.freq(330-220*a/255);
  //sine.amp(0.25+3*(1-a/255)/4);

  // Text
  fill(255);
  textSize(12);
  textAlign(LEFT);
  text("Amplitude: "+amplitude.get(s-1), 225, 30);
  textAlign(RIGHT);
  text("Features: "+m, 200, 30);
}

/*void getSqueeze() {

  int n = port.available();

  if (n > 0) {

    byte[] u = new byte[n];
    port.readBytes(u);

    for (int i = 0; i < u.length; i++) {

      int t = (int) u[i] & 0xff;

      if (!reading) {
        if (t == 0) {
          reading = true;
          buffer = new ArrayList<Integer>();
        }
      } else {
        if (t == 0) {
          values = buffer;
          buffer = new ArrayList<Integer>();
        } else {
          buffer.add(t);
        }
      }
    }
  }
}*/
/*
int getAmplitude() {

  float t = 0;

  int m = values.size();

  for (int i = 0; i < values.size(); i++) {

    int u = values.get(i);

    if (u != 255) {
      t += values.get(i);
    } else m--;
  }

  amplitude.add(t/m);
  //filtered.add(movingAverage());
  //println(memory.size());

  return m;
}
*/
void getAmplitudes() {
  
    amplitude.add(Skweezee.mag());
    amplitude0.add(Skweezee.mag(0));
    amplitude1.add(Skweezee.mag(1));
    amplitude2.add(Skweezee.mag(2));
    amplitude3.add(Skweezee.mag(3));
    amplitude4.add(Skweezee.mag(4));
    amplitude5.add(Skweezee.mag(5));
    amplitude6.add(Skweezee.mag(6));
    amplitude7.add(Skweezee.mag(7));
    
  }

void keyPressed() {

  saveFrame("screenshot####.png");
}

float movingAverage() {

  int n = amplitude.size();
  float t = 0.0;

  if (n  > 5) {

    for (int i = 0; i < 5; i++) {
      t += amplitude.get(n-1-i);
    }
    
  }
  
  return t/5;
  
}

float getStdDev() {
  
  return Skweezee.stdev();
  
}

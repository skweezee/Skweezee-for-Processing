import net.skweezee.processing.*;
import processing.serial.*;

Line raw;
Line max;
Line avg;
Line stdev;
Line diff;

Graph graph;

Boolean freeze;

float t;

void setup() {
  
  
  size(1000, 600);
  colorMode(HSB, 360, 100, 100);
  frameRate(25);
  freeze = false;
  Skweezee.connect(this);
  
  raw = new Line(color(10, 100, 100));   // RED
  max = new Line(color(120, 100, 80));     // GREEN
  avg = new Line(color(10, 80, 70));  // ORANGE
  stdev = new Line(color(210, 100, 100)); // BLUE
  diff = new Line(color(280, 100, 100)); // PURPLE
  
  /*raw = new Line(color(196, 76, 80));   // RED
  max = new Line(color(196, 26, 60));     // GREEN
  avg = new Line(color(163, 66, 100));  // ORANGE
  stdev = new Line(color(5, 41, 100)); // BLUE
  diff = new Line(color(346, 76, 80)); // PURPLE*/
  
  graph = new Graph(10, 10, 980, 580, 500, 0, 0);
  graph.addSignal(raw);
  graph.addSignal(max);
  graph.addSignal(avg);
  graph.addSignal(stdev);
  graph.addSignal(diff);
  
  stroke(255);

}

void draw() {

  if(!freeze) background(0);
  
  t += TWO_PI/10;
  
  raw.push(Skweezee.mag());
  max.push(Skweezee.max());
  avg.push(Skweezee.avg());
  stdev.push(Skweezee.stdev());
  diff.push(Skweezee.diff());
  
  if(!freeze) graph.display();
  
}

void keyPressed() {
  freeze = !freeze;
}

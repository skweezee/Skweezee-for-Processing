class Graph {

  int x;
  int y;
  int w;
  int h;
  
  float min;
  float max;
  
  float margin;
  
  int resolution;
  
  color background;
  
  ArrayList<Line> signals;
  
  Graph(int x, int y, int w, int h, int resolution, int min, int max) {
  
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    
    this.min = min;
    this.max = max;
    
    margin = 0.1;
    
    this.resolution = resolution;
    
    background = color(0, 0, 100);
    
    signals = new ArrayList<Line>();
    
  }
  
  void setPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  void setSize(int w, int h) {
    this.w = w;
    this.h = h;
  }
  
  void setResolution(int resolution) {
    this.resolution = resolution;
  }
  
  void setBackground(color c) {
    this.background = c;
  }
  
  void addSignal(Line s) {
    signals.add(s);
  }
  
  void setRange(int min, int max) {
    this.min = min;
    this.max = max;
  }
  
  void setMargin(int margin) {
    this.margin = (float) margin;
  }
  
  void display() {
  
    fill(background);
    noStroke();
    rect(x, y, w, h);
    
    if (signals.size() > 0) {
    
      for (int i = 0; i < signals.size(); i++) {
         if(signals.get(i).min < min) {
           min = signals.get(i).min;
         }
         
         if (signals.get(i).max > max) {
           max = signals.get(i).max;
         }
      }
      
      for (int i = 0; i < signals.size(); i++) {
      
        float[] data = signals.get(i).frame(resolution);
        
        stroke(signals.get(i).c);
        noFill();
    
        beginShape();
        for (int j = 0; j < resolution; j++) {
          if(j <= data.length-1) vertex(margin+x+j*(((float) w-2*margin)/(resolution-1)), y+h-data[j]*h/(2*margin+max-min)+(min-margin)*h/(2*margin+max-min));
        }
        endShape();
  
      }
    
    }
    
  }
  
  
  
}

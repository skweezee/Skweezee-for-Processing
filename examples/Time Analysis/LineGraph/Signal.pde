class Line {
  
    ArrayList<Float> points;
    
    float min;
    float max;
    
    color c;

  Line() {
    
    points = new ArrayList<Float>();
    
    min = 0;
    max = 0;
    
    c = color(0, 0, 0);
    
  }
  
  Line(color c) {
    
    points = new ArrayList<Float>();
    
    min = 0;
    max = 0;
    
    this.c = c;
    
  }
  
  void push(float x) {
    
    points.add(x);
    
    if (x > max) {
      max = x;
    } else if (x < min) {
      min = x;
    }
    
  }
  
  float[] frame(int size) {

    float[] temp = new float[size];
    
    if (points.size() >= size) {
      
      for (int i = size; i > 0; i--) {
        temp[size-i] = points.get(points.size() - i);
      }
      
    } else if (points.size() > 0) {
      
      temp = new float[points.size()];
      
      for(int i = 0; i < points.size(); i++) {
        temp[i] = points.get(i);
      }
      
    }
    
    return temp;
    
  }
  
}

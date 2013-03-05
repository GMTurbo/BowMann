
/*

 so this is going to be a copy of bowman
 we need to fire arrows at two different player
 
 mouse does all the interaction
 arrow trajectory is mouse y position (mousedown to mouseup delta)
 power is mouse x position (mousedown to mouseup delta)

 NEED physics or parabolic arc formula (it needs a timestep) (i can find this)
  and mouse interation is important too
  
  http://en.wikipedia.org/wiki/Projectile_motion
*/
float toRad(float deg) { return deg * PI / 180.0; }
float toDeg(float rad) { return rad * 180.0 / PI; }
ArrayList<Arrow> arrows;
ArrayList<Ellipse> trail;
PFont f;
float time = 0;

void setup(){
  smooth();
  size(1000, 500, P3D);
  
  arrows = new ArrayList<Arrow>();
  trail = new ArrayList<Ellipse>();
  f = createFont("Berthold Akzidenz Grotesk BE", 64, true);//32
  //frameRate(32);
  textFont(f);
  textAlign(LEFT);
  textSize(15);
  
}

float timeStep = 0.1;
void draw(){
  
  if(arrows.size() == 0){
    camera(width/2.0, height/2.0, (height/2.0) / tan(PI*30.0 / 180.0),
          width/2.0, height/2.0, 0.0,
          0.0, 1.0, 0.0);
    trail.clear(); 
  }
  background(176,226,255);
  fill(0);  
  if(tempArrow != null){
    text("Power: " + tempArrow.getVelocity(), 20, height - 50);
    text("Angle: " + tempArrow.getAngleInDegrees(), 20, height - 35);
    text("xPos: " + tempArrow.xpos, 20, height - 20);
    text("yPos: " + tempArrow.ypos, 90, height - 20);
  }
  
  stroke(0);
  strokeWeight(2);
  fill(0,0,0,155);  
  for(int i = 0 ; i < arrows.size() ; i++){
     if(arrows.get(i).deleteMe)
        arrows.remove(i); 
  }
  beginCamera();
  //camera();
  for(int i = 0 ; i < arrows.size() ; i++){
    arrows.get(i).step(time);
    ellipse(arrows.get(i).xpos, arrows.get(i).ypos, 20, 20);
    trail.add(new Ellipse(arrows.get(i).xpos, arrows.get(i).ypos));
    camera(arrows.get(i).xpos, height/2.0, (height/2.0) / tan(PI*30.0 / 180.0),
          arrows.get(i).xpos, height/2.0, 0.0,
          0.0, 1.0, 0.0);
    println( i + " :ArrowX = " + arrows.get(i).xpos + " ArrowY = " + arrows.get(i).ypos);
  }
  endCamera();
  
  for(int i = 0 ; i < trail.size(); i++)
    ellipse(trail.get(i).xPos, trail.get(i).yPos, 20,20);
    
  if(mousePressed){
    line(mouseX, mouseY, mouseDownX, mouseDownY);
  }
  fill(0,204,0);
  for(int i = -width; i< width; i++){
    float scale = (float) i / (float) width;
    rect(i*5, height, 5, -1 * abs(25 * sin(2 * PI * scale * 10)));
    //print(PI * scale);
  }
    
   time+=timeStep;
  //++count;
}

int mouseDownX, mouseDownY;

PVector v1;
void MyNiceFloor() {
  //stroke(0);
  
}

Arrow tempArrow;

void mousePressed(){
  mouseDownX = mouseX;
  mouseDownY = mouseY;
  tempArrow = new Arrow(0, 0, mouseX, mouseY, height, time);
  println("Arrow added:  = x:" + tempArrow.xpos + " y:" + tempArrow.ypos);
  println("mouseX = " + mouseX + " mouseY = " + mouseY);
}



void mouseReleased(){
   //arrows.add( new Arrow(, min(power,100), mouseX, mouseY) );
   mouseDownX = 0;
   mouseDownY = 0;
   tempArrow.setTime(time);
   arrows.add(tempArrow);
   tempArrow = null;
}

void mouseDragged(){
  if(mousePressed){
    int deltaX = mouseDownX - mouseX;
    int deltaY = mouseDownY - mouseY;
    v1 = new PVector(deltaX, deltaY); //<>//
    String s1 = v1.mag()+"";
    
    float angleInDegrees = atan2(deltaY, deltaX) * 180 / PI;
    
    println("Power: " + s1);
    float mag = min(v1.mag(), 400);
    tempArrow.setVelocity(mag);
    tempArrow.setAngle(toRad(angleInDegrees));
    
  }
}

void keypress(){

}

class Arrow{
  boolean stopped = false;
  float angle, velocity;
  int xpos, ypos;
  int xposStart, yposStart;
  float gravity = 25;
  int floor = 0;
  float startTime=0;
  Arrow(float ang, float vel, int x, int y, int limit, float start ){
     angle = ang;
     velocity = vel; 
     xpos = x;
     ypos = y;
     xposStart = xpos;
     yposStart = ypos;
     startTime = start;
     floor = limit;
  }
  
  void setVelocity(float val){
    velocity = val;
  }
  
  void setTime(float time){
    startTime = time;
  }
  
  void setAngle(float val){
    angle = val;
  }
  
  float getVelocity(){
     return velocity; 
  }
  
  float getAngleInDegrees(){
     return angle * 180.0/PI; 
  }
  
  boolean deleteMe = false;
  
  void step(float time){
    
    if( ypos > floor){
      deleteMe = true;
      return;
    }
   
    xpos = xposStart+(int)(velocity * (time - startTime) * cos(angle));
    ypos = yposStart+(int)(velocity * (time - startTime) * sin(angle) + 0.5 * gravity * pow((time - startTime),2));
    println("x pos = " + xpos + " y pos = " + ypos + " limit: " + floor);
 
  }
  
}

class Ellipse{
  int xPos, yPos; 
  Ellipse(int x, int y){
      xPos = x;
      yPos = y;
    }
}

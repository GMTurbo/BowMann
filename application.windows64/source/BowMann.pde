
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
PImage arrow;
PImage cloud;
Animation bow;

void setup(){
  smooth();
  size(1100, 700, P3D);
  arrows = new ArrayList<Arrow>();
  trail = new ArrayList<Ellipse>();
  arrow = loadImage("Resources\\arrow.png");
  f = createFont("Berthold Akzidenz Grotesk BE", 64, true);//32
  cloud = loadImage("Resources\\cloud.png");
  bow = new Animation("Resources\\bow\\bow", 5);
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
    trail.add(new Ellipse(arrows.get(i).xpos, arrows.get(i).ypos));
    arrows.get(i).step(time);
    UpdateArrow(arrows.get(i).xpos, arrows.get(i).ypos, arrows.get(i).getCurrentAngleInRads(time, time - timeStep), 0);
   // ellipse(arrows.get(i).xpos, arrows.get(i).ypos, 20, 20);
    
    camera( arrows.get(i).xpos, arrows.get(i).ypos, (height/2.0) / tan(PI*30.0 / 180.0),
            arrows.get(i).xpos, arrows.get(i).ypos < (height/2.0) ? arrows.get(i).ypos :(height/2.0) , 0.0,
            0.0,                1.0,        0.0);
          
    println( i + " :ArrowX = " + arrows.get(i).xpos + " ArrowY = " + arrows.get(i).ypos);
  }
  endCamera();
  
  for(int i = 0 ; i < trail.size(); i++)
    ellipse(trail.get(i).xPos, trail.get(i).yPos, 5, 5);
    
  if(mousePressed){
    //line(mouseX, mouseY, mouseDownX, mouseDownY);
    text("Power: " + tempArrow.getVelocity()/4.0, mouseDownX + 30, mouseDownY - 30);
    text("Angle: " + tempArrow.getAngleInDegrees(), mouseX, mouseY - 30);
    text("xPos: " + tempArrow.xpos, 20, height - 20);
    text("yPos: " + tempArrow.ypos, 90, height - 20);
    bow.show(tempArrow.xpos, tempArrow.ypos, tempArrow.getAngleInRads(), tempArrow.getVelocity()/4.0);
    UpdateArrow(tempArrow.xpos, tempArrow.ypos, tempArrow.getAngleInRads(), tempArrow.getVelocity()/4.0);
  }
  
  fill(0,204,0);
  
  for(int i = -width; i< width; i++){
    float scale = (float) i / (float) width;
    rect(i*5, height, 5, -1 * abs(25 * sin(2 * PI * scale * 10)));
  }
  DrawCloud();
  
  time += timeStep;
}

int mouseDownX, mouseDownY;

PVector v1;
void DrawCloud(){
   image(cloud, 2*width/4, -1.25*height);
   image(cloud, 2*width/2, -2.25*height);
   image(cloud,       0,   -height);
   image(cloud,-2*width/2, -2.25*height);
   image(cloud,-2*width/4, -1.25*height);
}

void UpdateArrow(int xpos, int ypos, float angle, float power){
  pushMatrix();
  translate(xpos, ypos);
  rotate( angle );
  image( arrow, -(arrow.width/2.0)- 30*(power/100.0), -(arrow.height/2.0) );
  popMatrix();
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
void mouseMoved(){
  //UpdateImage(mouseX, mouseY, atan2(pmouseY - mouseY, pmouseX - mouseX));
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
  PImage arrow;
  Arrow(float ang, float vel, int x, int y, int limit, float start ){
     arrow = loadImage("Resources\\arrow.png");
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
  float getAngleInRads(){
     return angle;
  }
  
  float getCurrentAngleInRads(float current, float prev){
    float xpos1 = xposStart+(int)(velocity * (current - startTime) * cos(angle));
    float ypos1 = yposStart+(int)(velocity * (current - startTime) * sin(angle) + 0.5 * gravity * pow((current - startTime),2));
    float xpos2 = xposStart+(int)(velocity * (prev - startTime) * cos(angle));
    float ypos2 = yposStart+(int)(velocity * (prev - startTime) * sin(angle) + 0.5 * gravity * pow((prev - startTime),2));
    
    return atan2((ypos1-ypos2),(xpos1-xpos2));
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

class Animation {
  PImage[] images;
  int imageCount;
  int frame;
  
  Animation(String imagePrefix, int count) {
    imageCount = count;
    images = new PImage[imageCount];

    for (int i = 0; i < imageCount; i++) {
      // Use nf() to number format 'i' into four digits
      String filename = imagePrefix + nf(i, 4) + ".gif";
      images[i] = loadImage(filename);
    }
  }

  void display(float xpos, float ypos) {
    frame = (frame+1) % imageCount;
    image(images[frame], xpos, ypos);
  }
  void show(float xpos, float ypos, float angle, float velocity) {
    pushMatrix();
    translate(xpos, ypos);
    rotate( angle );
    frame = (int)((velocity/100.0) * imageCount);
    if( frame >= imageCount)
      frame = imageCount-1;
    image( images[frame], -images[frame].width/2.0, -images[frame].height/2.0 );
    println("frame = " + frame + " velocity: " + velocity);
    
    popMatrix();
  }
  int getWidth() {
    return images[0].width;
  }
}

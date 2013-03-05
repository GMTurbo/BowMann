import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class BowMann extends PApplet {


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
public float toRad(float deg) { return deg * PI / 180.0f; }
public float toDeg(float rad) { return rad * 180.0f / PI; }
ArrayList<Arrow> arrows;
ArrayList<Ellipse> trail;
PFont f;
float time = 0;
PImage arrow;
PImage cloud;
Animation bow;

public void setup(){
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

float timeStep = 0.1f;
public void draw(){
  
  if(arrows.size() == 0){
    camera(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f),
          width/2.0f, height/2.0f, 0.0f,
          0.0f, 1.0f, 0.0f);
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
    
    camera( arrows.get(i).xpos, arrows.get(i).ypos, (height/2.0f) / tan(PI*30.0f / 180.0f),
            arrows.get(i).xpos, arrows.get(i).ypos < (height/2.0f) ? arrows.get(i).ypos :(height/2.0f) , 0.0f,
            0.0f,                1.0f,        0.0f);
          
    println( i + " :ArrowX = " + arrows.get(i).xpos + " ArrowY = " + arrows.get(i).ypos);
  }
  endCamera();
  
  for(int i = 0 ; i < trail.size(); i++)
    ellipse(trail.get(i).xPos, trail.get(i).yPos, 5, 5);
    
  if(mousePressed){
    //line(mouseX, mouseY, mouseDownX, mouseDownY);
    text("Power: " + tempArrow.getVelocity()/4.0f, mouseDownX + 30, mouseDownY - 30);
    text("Angle: " + tempArrow.getAngleInDegrees(), mouseX, mouseY - 30);
    text("xPos: " + tempArrow.xpos, 20, height - 20);
    text("yPos: " + tempArrow.ypos, 90, height - 20);
    bow.show(tempArrow.xpos, tempArrow.ypos, tempArrow.getAngleInRads(), tempArrow.getVelocity()/4.0f);
    UpdateArrow(tempArrow.xpos, tempArrow.ypos, tempArrow.getAngleInRads(), tempArrow.getVelocity()/4.0f);
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
public void DrawCloud(){
   image(cloud, 2*width/4, -1.25f*height);
   image(cloud, 2*width/2, -2.25f*height);
   image(cloud,       0,   -height);
   image(cloud,-2*width/2, -2.25f*height);
   image(cloud,-2*width/4, -1.25f*height);
}

public void UpdateArrow(int xpos, int ypos, float angle, float power){
  pushMatrix();
  translate(xpos, ypos);
  rotate( angle );
  image( arrow, -(arrow.width/2.0f)- 30*(power/100.0f), -(arrow.height/2.0f) );
  popMatrix();
}

Arrow tempArrow;

public void mousePressed(){
  mouseDownX = mouseX;
  mouseDownY = mouseY;
  tempArrow = new Arrow(0, 0, mouseX, mouseY, height, time);
  println("Arrow added:  = x:" + tempArrow.xpos + " y:" + tempArrow.ypos);
  println("mouseX = " + mouseX + " mouseY = " + mouseY);
}



public void mouseReleased(){
   //arrows.add( new Arrow(, min(power,100), mouseX, mouseY) );
   mouseDownX = 0;
   mouseDownY = 0;
   tempArrow.setTime(time);
   arrows.add(tempArrow);
   tempArrow = null;
}

public void mouseDragged(){
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
public void mouseMoved(){
  //UpdateImage(mouseX, mouseY, atan2(pmouseY - mouseY, pmouseX - mouseX));
}
public void keypress(){
  
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
  
  public void setVelocity(float val){
    velocity = val;
  }
  
  public void setTime(float time){
    startTime = time;
  }
  
  public void setAngle(float val){
    angle = val;
  }
  
  public float getVelocity(){
     return velocity; 
  }
  
  public float getAngleInDegrees(){
     return angle * 180.0f/PI; 
  }
  public float getAngleInRads(){
     return angle;
  }
  
  public float getCurrentAngleInRads(float current, float prev){
    float xpos1 = xposStart+(int)(velocity * (current - startTime) * cos(angle));
    float ypos1 = yposStart+(int)(velocity * (current - startTime) * sin(angle) + 0.5f * gravity * pow((current - startTime),2));
    float xpos2 = xposStart+(int)(velocity * (prev - startTime) * cos(angle));
    float ypos2 = yposStart+(int)(velocity * (prev - startTime) * sin(angle) + 0.5f * gravity * pow((prev - startTime),2));
    
    return atan2((ypos1-ypos2),(xpos1-xpos2));
  }
  
  boolean deleteMe = false;
  
  public void step(float time){
    
    if( ypos > floor){
      deleteMe = true;
      return;
    }
    xpos = xposStart+(int)(velocity * (time - startTime) * cos(angle));
    ypos = yposStart+(int)(velocity * (time - startTime) * sin(angle) + 0.5f * gravity * pow((time - startTime),2));
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

  public void display(float xpos, float ypos) {
    frame = (frame+1) % imageCount;
    image(images[frame], xpos, ypos);
  }
  public void show(float xpos, float ypos, float angle, float velocity) {
    pushMatrix();
    translate(xpos, ypos);
    rotate( angle );
    frame = (int)((velocity/100.0f) * imageCount);
    if( frame >= imageCount)
      frame = imageCount-1;
    image( images[frame], -images[frame].width/2.0f, -images[frame].height/2.0f );
    println("frame = " + frame + " velocity: " + velocity);
    
    popMatrix();
  }
  public int getWidth() {
    return images[0].width;
  }
}
// The Nature of Code
// Daniel Shiffman
// http://natureofcode.com

// Genetic Algorithm, Evolving Shakespeare

// A class to describe a psuedo-DNA, i.e. genotype
//   Here, a virtual organism's DNA is an array of character.
//   Functionality:
//      -- convert DNA into a string
//      -- calculate DNA's "fitness"
//      -- mate DNA with another set of DNA
//      -- mutate DNA


class DNA {

  // The genetic sequence
  char[] genes;
  
  float fitness;
  
  // Constructor (makes a random DNA)
  DNA(int num) {
    genes = new char[num];
    for (int i = 0; i < genes.length; i++) {
      genes[i] = (char) random(32,128);  // Pick from range of chars
    }
  }
  
  // Converts character array to a String
  public String getPhrase() {
    return new String(genes);
  }
  
  // Fitness function (returns floating point % of "correct" characters)
  public void fitness (String target) {
     int score = 0;
     for (int i = 0; i < genes.length; i++) {
        if (genes[i] == target.charAt(i)) {
          score++;
        }
     }
     fitness = pow(2,score);
  }
  
  // Crossover
  public DNA crossover(DNA partner) {
    // A new child
    DNA child = new DNA(genes.length);
    
    int midpoint = PApplet.parseInt(random(genes.length)); // Pick a midpoint
    
    // Half from one, half from the other
    for (int i = 0; i < genes.length; i++) {
      if (i > midpoint) child.genes[i] = genes[i];
      else              child.genes[i] = partner.genes[i];
    }
    return child;
  }
  
  // Based on a mutation probability, picks a new random character
  public void mutate(float mutationRate) {
    for (int i = 0; i < genes.length; i++) {
      if (random(1) < mutationRate) {
        genes[i] = (char) random(32,128);
      }
    }
  }
}
// The Nature of Code
// Daniel Shiffman
// http://natureofcode.com

// Genetic Algorithm, Evolving Shakespeare

// A class to describe a population of virtual organisms
// In this case, each organism is just an instance of a DNA object

class Population {

  float mutationRate;           // Mutation rate
  DNA[] population;             // Array to hold the current population
  ArrayList<DNA> matingPool;    // ArrayList which we will use for our "mating pool"
  String target;                // Target phrase
  int generations;              // Number of generations
  boolean finished;             // Are we finished evolving?
  int perfectScore;

  Population(String p, float m, int num) {
    target = p;
    mutationRate = m;
    population = new DNA[num];
    for (int i = 0; i < population.length; i++) {
      population[i] = new DNA(target.length());
    }
    calcFitness();
    matingPool = new ArrayList<DNA>();
    finished = false;
    generations = 0;
    
    perfectScore = PApplet.parseInt(pow(2,target.length()));
  }

  // Fill our fitness array with a value for every member of the population
  public void calcFitness() {
    for (int i = 0; i < population.length; i++) {
      population[i].fitness(target);
    }
  }
  public float GetMax(){
    float max = 0;
    for(int i = 0 ; i < population.length; i++)
      max = max(max, population[i].fitness);
      
      return max;
  }
  public float GetMin(){
    float min = 1000;
    for(int i = 0 ; i < population.length; i++)
      min = min(min, population[i].fitness);
      
      return min;
  }
  // Generate a mating pool
  public void naturalSelection() {
    // Clear the ArrayList
    matingPool.clear();

    float maxFitness = 0;
    for (int i = 0; i < population.length; i++) {
      if (population[i].fitness > maxFitness) {
        maxFitness = population[i].fitness;
      }
    }

    // Based on fitness, each member will get added to the mating pool a certain number of times
    // a higher fitness = more entries to mating pool = more likely to be picked as a parent
    // a lower fitness = fewer entries to mating pool = less likely to be picked as a parent
    for (int i = 0; i < population.length; i++) {
      
      float fitness = map(population[i].fitness,0,maxFitness,0,1);
      int n = PApplet.parseInt(fitness * 100);  // Arbitrary multiplier, we can also use monte carlo method
      for (int j = 0; j < n; j++) {              // and pick two random numbers
        matingPool.add(population[i]);
      }
    }
  }
  public float GetFitness(int index){
     return population[index].fitness; 
  }
  // Create a new generation
  public void generate() {
    // Refill the population with children from the mating pool
    for (int i = 0; i < population.length; i++) {
      int a = PApplet.parseInt(random(matingPool.size()));
      int b = PApplet.parseInt(random(matingPool.size()));
      DNA partnerA = matingPool.get(a);
      DNA partnerB = matingPool.get(b);
      DNA child = partnerA.crossover(partnerB);
      child.mutate(mutationRate);
      population[i] = child;
    }
    generations++;
  }


  // Compute the current "most fit" member of the population
  public String getBest() {
    float worldrecord = 0.0f;
    int index = 0;
    for (int i = 0; i < population.length; i++) {
      if (population[i].fitness > worldrecord) {
        index = i;
        worldrecord = population[i].fitness;
      }
    }

    if (worldrecord == perfectScore ) finished = true;
    return population[index].getPhrase();
  }

  public boolean finished() {
    return finished;
  }

  public int getGenerations() {
    return generations;
  }

  // Compute average fitness for the population
  public float getAverageFitness() {
    float total = 0;
    for (int i = 0; i < population.length; i++) {
      total += population[i].fitness;
    }
    return total / (population.length);
  }

  public String allPhrases() {
    String everything = "";
    
    int displayLimit = min(population.length,50);
    
    for (int i = 0; i < displayLimit; i++) {
      everything += population[i].getPhrase() + "\n";
    }
    return everything;
  }
  
  public String allPhrases(int columnNum, int columnsToDisplay) {
    
    String everything = "";
    
    int starting = columnsToDisplay * columnNum;
    
    int displayLimit = min(population.length - starting, columnsToDisplay);
    
    for (int i = starting; i < starting + displayLimit ; i++) {
      everything += population[i].getPhrase() + "\n";
    }
    return everything;
  }
  
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "BowMann" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

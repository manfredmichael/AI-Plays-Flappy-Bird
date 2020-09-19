class Bird {
  NeuralNetwork brain;

  float x;
  float y;
  float vy;
  float g=0.2;
  float size=33;
  float traveled=0;
  boolean dead=false;
  Bird() {
    brain=new NeuralNetwork(layer);
    x=100;
    y=height/3;
  }
  //----------------------------------------------------------
  void move() {
    if (!dead) {
      traveled++;
      if ((y>=size/2)&&(y<=height-size/2)) {
        y+=vy;
        vy+=g;
      } else if (y<size/2) {
        vy=0;
        y=size/2;
        dead=true;
      } else if (y>height-size/2) {
        vy=0;
        y=height-size/2;
        dead=true;
      }
    } else {
      x-=gameSpeed;
    }
    if (!dead)
      checkHit();
  }
  //---------------------------------------------------------------------
  void jump() {
    if (!dead)
      vy=-5;
  }
  //---------------------------------------------------------------------
  void show() {    
    //stroke(255);
    //line(0, y, width, y);
    //line(x, 0, x, height);

    imageMode(CENTER);
    pushMatrix();
    translate(x, y);
    rotate(vy/10);
    image(birdPic, 0, 0);
    //ellipse(0,0,47,34);
    popMatrix();

    //rectMode(CENTER);
    //noFill();
    //rect(x, y, size, size);
  }
  //------------------------------------------------------------------------
  //------------------------------------------------------------------------
  void checkHit() {
    boolean thereIsNearby=false;
    Obstacle nearby=new Obstacle();
    for (Obstacle obstacle : obstacles) {
      if ((obstacle.x<150)&&(obstacle.x>50)) {
        nearby=obstacle;
        thereIsNearby=true;
      }
    }
    if (thereIsNearby) {
      float obsX=nearby.x+4;
      float obsYtop=nearby.y;
      float obsYbottom=nearby.y+nearby.gap;
      float obsH=195;
      if ((x+size/2>obsX)&&(x-size/2<obsX)&&((y>obsYbottom-obsH)||(y<obsYtop+obsH)))
        dead=true;

      PVector topNeedle=new PVector(obsX, obsYtop+obsH);
      PVector bottomNeedle=new PVector(obsX, obsYbottom-obsH);
      PVector pos=new PVector(x, y);
      float distanceTop=PVector.sub(pos, topNeedle).mag();
      float distanceBottom=PVector.sub(pos, bottomNeedle).mag();

      if ((distanceTop<size/2)||(distanceBottom<size/2))
        dead=true;
    }
  }

  void think() {
    Obstacle nearby=obstacles.get(0);
    if (obstacles.get(0).x<70)
      nearby=obstacles.get(1);

    float obsH=195;
    float obsX=map(nearby.x+4, 0, width, 0, width);
    float obsYtop=nearby.y+obsH;
    float obsYbottom=nearby.y+nearby.gap-obsH;

    //line(obsX, obsYtop, obsX, obsYbottom);
    float [] input={y/height,obsX/width, obsYtop/height, obsYbottom/height,y/20};
    float [] output=brain.feedForward(input);
    if (output[0]>output[1])
      jump();
  }
}

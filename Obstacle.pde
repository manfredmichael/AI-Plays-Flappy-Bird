class Obstacle {
  float y;
  float x;
  float w=0;
  float h=430;
  float gap=385+120;
  Obstacle() {
    y=round(random(-65, 160));
    x=width+100;
  }
  //--------------------------------------------
  void show() {
    imageMode(CENTER);
    image(pillar, x, y+gap);
    pushMatrix();
    translate(x, y);
    scale(-1, 1);
    rotate(PI);
    image(pillar, 0, 0);
    popMatrix();
  }
  //---------------------------------------------  
  void move() {
    x-=gameSpeed;
  }
}

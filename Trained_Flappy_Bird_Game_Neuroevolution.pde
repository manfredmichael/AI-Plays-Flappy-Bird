import java.util.*;

JSONObject brainData;

ArrayList<Bird> birds=new ArrayList<Bird>();
ArrayList<Obstacle> obstacles=new ArrayList<Obstacle>();

PImage birdPic;
PImage pillar;

PFont font;

float mutationRate=0.008;
float gameSpeed=2;

int [] layer={5, 5, 2};
int birdPopulation=250;
int generation=0;
int score=0;
int highestScore=0;

boolean allDead=false;
boolean showBestOnly=false;

int generationTraveled=0;
int previousGenerationTraveled=0;

float obstacleSpawner=0;
float bestAnimation=0;
void setup() {
  size(800, 600, P2D);
  font=createFont("Calibri", 27);
  textFont(font);
  textAlign(CENTER);

  birdPic=loadImage("flappyBird.png");
  birdPic.resize(47, 34);
  pillar=loadImage("building.png");
  pillar.resize(130, 430);

  for (int i=0; i<birdPopulation; i++)
    birds.add(new Bird());

  restartRound();
  loadBrain();  //*** Comment this out to start from generation 1 ***
}
void draw() {
  background(#87CEEB);

  for (Obstacle obstacle : obstacles) {
    obstacle.move();
    obstacle.show();
  }
  for (Bird bird : birds) {
    bird.think();
    bird.move();
    if (!showBestOnly)
      bird.show();
  }
  showBest();

  checkLife();
  if (allDead) {
    restartRound();
    regenerateBird();
    allDead=false;
  }
  spawnObstacles();

  fill(#E15C6D);
  text("Generation "+str(generation), width/2, height-30);
  text(str(score), width/2, 30);
  text(str(highestScore), width-40, 30);
  generationTraveled++;
}
//-------------------------------------------
void keyPressed() {
  if ((key=='b')||(key=='B'))
    showBestOnly=!showBestOnly;
}
//----------------------------------------------
void spawnObstacles() {

  obstacleSpawner+=gameSpeed;
  if (obstacleSpawner>=400) {
    obstacleSpawner=0;
    obstacles.add(new Obstacle());
  }
  if (obstacles.get(0).x<-65) {
    obstacles.remove(0);
    score++;
  }
}
//------------------------------------------------
void restartRound() {
  obstacles.clear();
  if (score>highestScore)
    highestScore=score;
  score=0;
  for (int i=0; i<2; i++) {
    obstacles.add(new Obstacle());
    obstacles.get(i).x-=(400*(1-i));
  }
  obstacleSpawner=0;
}
//------------------------------------------------------
void checkLife() {
  int life=0;
  for (Bird bird : birds) {
    if (!bird.dead)
      life++;
  }
  if (life==0)
    allDead=true;
}
//---------------------------------------------------------------------
void regenerateBird() {
  generation++;

  ArrayList<Bird> matingPool=new ArrayList<Bird>();
  float sumTraveled=0;
  float maxTraveled=0;
  Bird best=new Bird();
  for (Bird bird : birds) {
    sumTraveled+=bird.traveled;
    if ( bird.traveled>maxTraveled) {
      maxTraveled=bird.traveled;
      best.brain=bird.brain.copy();
    }
  }

  for (Bird bird : birds) {
    float fitness=acceptReject(bird.traveled, maxTraveled)/sumTraveled;
    fitness*=100;
    for (int i=1; i<fitness; i++) {
      matingPool.add(bird);
    }
  }

  Collections.shuffle(matingPool);

  if (matingPool.size()>0) {
    for (int i=0; i<birds.size(); i++) {
      int index=floor(random(matingPool.size()));
      birds.get(i).brain=matingPool.get(index).brain.copy();
    }
  }

  for (Bird bird : birds) {
    bird.brain.mutate(mutationRate);
    bird.y=round(random(height/3, height*2/3));
    bird.dead=false;
    bird.traveled=0;
    bird.x=100;
  }

  bestAnimation=0;
  best.y=round(random(height/3, height*2/3));
  best.dead=false;
  best.traveled=0;
  best.x=100;
  birds.set(0, best);

  previousGenerationTraveled=generationTraveled;
  generationTraveled=0;


  // saveBrain();  //***Uncomment this to save your bird progress***
}
//---------------------------------------------------------------
float acceptReject(float fitness, float maxFitness) {
  float r=random(maxFitness);
  if (r<=fitness)
    return fitness;
  else
    return 0;
}
//---------------------------------------------------------------
void showBest() {
  Bird best=birds.get(0);
  float animationFrame=10*sin(PI*bestAnimation/20);
  if (!showBestOnly) {
    pushMatrix();
    translate(best.x, best.y);

    fill(#FFD700);
    for (int i=0; i<4; i++) {
      rotate(PI*i/2);
      beginShape();
      vertex(0, -40-animationFrame);
      vertex(-5, -50-animationFrame);
      vertex(5, -50-animationFrame);
      endShape();
    }
    popMatrix();
  } else {
    if (!birds.get(0).dead) {
      birds.get(0).show();
    } else {
      for (int i=0; i<birds.size(); i++) {
        if (!birds.get(i).dead) {
          birds.get(i).show();
          i=birds.size();
        }
      }
    }
  }
  bestAnimation++;
}
//--------------------------------------------------------------------------------------
void loadBrain() {
  brainData=loadJSONObject("brain.json");
  generation=brainData.getInt("generation");
  highestScore=brainData.getInt("highestScore");
  JSONArray brains=brainData.getJSONArray("brains");

  int iterator=brains.size();
  if (birdPopulation<brains.size())
    iterator=birdPopulation;

  for (int i=0; i<iterator; i++) {

    JSONObject brain=brains.getJSONObject(i);

    JSONArray weights=brain.getJSONArray("weights");
    JSONArray biases=brain.getJSONArray("biases");

    for (int j=0; j<weights.size(); j++) {
      JSONObject weight=weights.getJSONObject(j);

      int weightRow=weight.getInt("row");
      int weightColumn=weight.getInt("column");
      JSONArray weightArrayRow=weight.getJSONArray("array");
      for (int k=0; k<weightArrayRow.size(); k++) {
        JSONArray weightArrayColumn=weightArrayRow.getJSONArray(k);
        float [] arrayColumn=weightArrayColumn.getFloatArray();
        birds.get(i).brain.weights.get(j).array[k]=arrayColumn;
      }
      birds.get(i).brain.weights.get(j).row=weightRow;
      birds.get(i).brain.weights.get(j).column=weightColumn;


      JSONObject bias=biases.getJSONObject(j);
      int biasRow=bias.getInt("row");
      int biasColumn=bias.getInt("column");
      JSONArray biasArrayRow=bias.getJSONArray("array");
      for (int k=0; k<biasArrayRow.size(); k++) {
        JSONArray biasArrayColumn=biasArrayRow.getJSONArray(k);
        float [] arrayColumn=biasArrayColumn.getFloatArray();
        birds.get(i).brain.biases.get(j).array[k]=arrayColumn;
      }
      birds.get(i).brain.biases.get(j).row=biasRow;
      birds.get(i).brain.biases.get(j).column=biasColumn;
    }
  }
}
//----------------------------------------------------------------------------------------
void saveBrain() {
  int layer=birds.get(0).brain.weights.size();
  brainData=new JSONObject();
  JSONArray brains=new JSONArray();

  for (int i=0; i<birdPopulation; i++) {

    JSONObject brain=new JSONObject();

    JSONArray weights=new JSONArray();
    JSONArray biases=new JSONArray();

    for (int j=0; j<layer; j++) {
      JSONObject weight=new JSONObject();

      int weightRow=birds.get(i).brain.weights.get(j).row; 
      int weightColumn=birds.get(i).brain.weights.get(j).column; 

      JSONArray weightArrayRow=new JSONArray();
      for (int k=0; k<weightRow; k++) {
        JSONArray weightArrayColumn=new JSONArray();
        float [] arrayColumn;
        arrayColumn=birds.get(i).brain.weights.get(j).array[k];
        for (int l=0; l<weightColumn; l++ ) {
          weightArrayColumn.setFloat(l, arrayColumn[l]);
        }
        weightArrayRow.setJSONArray(k, weightArrayColumn);
      }

      weight.setInt("row", weightRow); 
      weight.setInt("column", weightColumn); 
      weight.setJSONArray("array", weightArrayRow); 


      JSONObject bias=new JSONObject(); 

      int biasRow=birds.get(i).brain.biases.get(j).row; 
      int biasColumn=birds.get(i).brain.biases.get(j).column; 

      JSONArray biasArrayRow=new JSONArray(); 
      for (int k=0; k<biasRow; k++) {
        JSONArray biasArrayColumn=new JSONArray();
        float [] arrayColumn;
        arrayColumn=birds.get(i).brain.biases.get(j).array[k];
        for (int l=0; l<biasColumn; l++ ) {
          biasArrayColumn.setFloat(l, arrayColumn[l]);
        }
        biasArrayRow.setJSONArray(k, biasArrayColumn);
      }

      bias.setInt("row", biasRow); 
      bias.setInt("column", biasColumn); 
      bias.setJSONArray("array", biasArrayRow); 

      biases.setJSONObject(j, bias); 

      weights.setJSONObject(j, weight);
    }
    brain.setJSONArray("weights", weights); 
    brain.setJSONArray("biases", biases); 

    brains.setJSONObject(i, brain);
  }
  brainData.setJSONArray("brains", brains); 

  brainData.setInt("generation", generation);
  brainData.setInt("highestScore", highestScore);
  saveJSONObject(brainData, "data/brain.json");
}

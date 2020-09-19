import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Trained_Flappy_Bird_Game_Neuroevolution extends PApplet {



JSONObject brainData;

ArrayList<Bird> birds=new ArrayList<Bird>();
ArrayList<Obstacle> obstacles=new ArrayList<Obstacle>();

PImage birdPic;
PImage pillar;

PFont font;

float mutationRate=0.008f;
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
public void setup() {
  
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
  // loadBrain();  //*** Comment this out to start from generation 1 ***
}
public void draw() {
  background(0xff87CEEB);

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

  fill(0xffE15C6D);
  text("Generation "+str(generation), width/2, height-30);
  text(str(score), width/2, 30);
  text(str(highestScore), width-40, 30);
  generationTraveled++;
}
//-------------------------------------------
public void keyPressed() {
  if ((key=='b')||(key=='B'))
    showBestOnly=!showBestOnly;
}
//----------------------------------------------
public void spawnObstacles() {

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
public void restartRound() {
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
public void checkLife() {
  int life=0;
  for (Bird bird : birds) {
    if (!bird.dead)
      life++;
  }
  if (life==0)
    allDead=true;
}
//---------------------------------------------------------------------
public void regenerateBird() {
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
public float acceptReject(float fitness, float maxFitness) {
  float r=random(maxFitness);
  if (r<=fitness)
    return fitness;
  else
    return 0;
}
//---------------------------------------------------------------
public void showBest() {
  Bird best=birds.get(0);
  float animationFrame=10*sin(PI*bestAnimation/20);
  if (!showBestOnly) {
    pushMatrix();
    translate(best.x, best.y);

    fill(0xffFFD700);
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
public void loadBrain() {
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
public void saveBrain() {
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
class Bird {
  NeuralNetwork brain;

  float x;
  float y;
  float vy;
  float g=0.2f;
  float size=33;
  float traveled=0;
  boolean dead=false;
  Bird() {
    brain=new NeuralNetwork(layer);
    x=100;
    y=height/3;
  }
  //----------------------------------------------------------
  public void move() {
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
  public void jump() {
    if (!dead)
      vy=-5;
  }
  //---------------------------------------------------------------------
  public void show() {    
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
  public void checkHit() {
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

  public void think() {
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
class Matrix {
  float [][] array;
  int row;
  int column;

  Matrix(int row, int column) {
    array=new float[row][column];
    this.row=row;
    this.column=column;
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=random(-1, 1);
      }
    }
  }

  Matrix(Matrix other) {
    row=other.row;
    column=other.column;
    array=new float[row][column];
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=other.array[i][j];
      }
    }
  }

  Matrix(float [] input) {
    row=1;
    column=input.length;
    array=new float[row][column];
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=input[j];
      }
    }
  }

  public float get(int i, int j) {
    return array[i][j];
  }

  public float [][] getArray() {
    return array;
  }

  public void set(float n) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=n;
      }
    }
  }

  public Matrix copy() {
    Matrix result=new Matrix(row, column);
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        result.array[i][j]=array[i][j];
      }
    }
    return result;
  }

  public void printMatrix() {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        print(array[i][j]+" ");
      }
      println();
    }
    println();
  }

  public void T() {
    Matrix result=new Matrix(column, row);
    for (int i=0; i<column; i++) {
      for (int j=0; j<row; j++) {
        result.array[i][j]=array[j][i];
      }
    }
    row=result.row;
    column=result.column;
    array=result.array;
  }

  public void add(float n) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=array[i][j]+n;
      }
    }
  } 

  public void mult(float n) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=array[i][j]*n;
      }
    }
  }

  public void mutate(float mutationRate) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        float random=random(1);
        if (random<=mutationRate) {
          float x=array[i][j]+=random(-7, 7);
          array[i][j]=1/(1+exp(-1*x));
        }
      }
    }
  }
}

class MatrixMath {
  public Matrix mult(Matrix a, Matrix b) {
    Matrix result=new Matrix(a.row, b.column);

    if (a.column==b.row) {
      for (int i=0; i<a.row; i++) {
        for (int j=0; j<b.column; j++) {
          result.array[i][j]=0;
          for (int k=0; k<b.row; k++) {
            result.array[i][j]+=a.array[i][k]*b.array[k][j];
          }
        }
      }
    } else {
      println("=========================================");
      println("this matrix column doesnt match other row");
      println("=========================================");
    }

    return result;
  }

  public Matrix add(Matrix a, Matrix b) {
    Matrix result=new Matrix(a.row, a.column); //not done error mismatch row cathcer
    if ((a.row==b.row)&&(a.column==b.column)) {
      for (int i=0; i<result.row; i++) {
        for (int j=0; j<result.column; j++) {
          result.array[i][j]=a.array[i][j]+b.array[i][j];
        }
      }
    } else {
      println("=========================================");
      println("this matrix column/row doesnt match other column/row");
      println("=========================================");
    }
    return result;
  }

  public Matrix sub(Matrix a, Matrix b) {
    Matrix result=new Matrix(a.row, a.column);
    if ((a.row==b.row)&&(a.column==b.column)) {
      for (int i=0; i<result.row; i++) {
        for (int j=0; j<result.column; j++) {
          result.array[i][j]=a.array[i][j]-b.array[i][j];
        }
      }
    } else {
      println("=========================================");
      println("this matrix column/row doesnt match other column/row");
      println("=========================================");
    }
    return result;
  }

  public Matrix getT(Matrix a) {
    Matrix result=new Matrix(a.column, a.row);
    for (int i=0; i<result.row; i++) {
      for (int j=0; j<result.column; j++) {
        result.array[i][j]=a.array[j][i];
      }
    }
    return result;
  }

  public Matrix hadamartProduct(Matrix a, Matrix b) {
    Matrix result=new Matrix(a.row, a.column); //not done error mismatch row cathcer
    if ((a.row==b.row)&&(a.column==b.column)) {
      for (int i=0; i<result.row; i++) {
        for (int j=0; j<result.column; j++) {
          result.array[i][j]=a.array[i][j]*b.array[i][j];
        }
      }
    } else {
      println("=========================================");
      println("this matrix column/row doesnt match other column/row");
      println("=========================================");
    }
    return result;
  }

  public Matrix sigmoid(Matrix a) {
    Matrix result=new Matrix(a);
    for (int i=0; i<a.row; i++) {
      for (int j=0; j<a.column; j++) {
        float x=result.array[i][j];
        result.array[i][j]=1/(1+exp(-1*x));
      }
    }
    return result;
  }
}

MatrixMath Matrix=new MatrixMath();
class NeuralNetwork {
  ArrayList<Matrix> weights=new ArrayList<Matrix>();
  ArrayList<Matrix> biases=new ArrayList<Matrix>();

  NeuralNetwork(int [] layers) {
    for (int i=0; i<layers.length-1; i++) {
      int row=layers[i+1];
      int column=layers[i];
      weights.add(new Matrix(row, column));

      row=layers[i+1];
      column=1;
      biases.add(new Matrix(row, column));
    }
  }

  public float [] feedForward(float [] input) {
    Matrix output=new Matrix(input);
    output.T();
    for (int i=0; i<weights.size(); i++) {

      output=Matrix.mult(weights.get(i), output);

      output=Matrix.add(output, biases.get(i));

      output=Matrix.sigmoid(output);
    }
    //output.printMatrix();
    output.T();
    return output.array[0];
  }

  public void mutate(float mutationRate) {
    for (Matrix weight : weights)
      weight.mutate(mutationRate);
    for (Matrix bias : biases)
      bias.mutate(mutationRate);
  }

  public NeuralNetwork crossover(NeuralNetwork other) {
    NeuralNetwork child=this.copy();
    child.weights.clear();
    child.biases.clear();
    int mid=round(random(weights.size()));
    for (int i=0; i<weights.size(); i++) {
      if (i<mid) {
        child.weights.add(weights.get(i).copy());
        child.biases.add(biases.get(i).copy());
      } else {
        child.weights.add(other.weights.get(i).copy());
        child.biases.add(other.biases.get(i).copy());
      }
    }
    return child;
  }

  public NeuralNetwork copy() {
    int [] parameter={0};
    NeuralNetwork clone=new NeuralNetwork(parameter);
    clone.weights.clear();
    clone.biases.clear();
    for (int i=0; i<weights.size(); i++) {
      clone.weights.add(weights.get(i).copy());
      clone.biases.add(biases.get(i).copy());
    }
    return clone;
  }

  public void train(float [] inputArray, float [] targetArray) {
    float learningRate=0.5f;
    ArrayList<Matrix> neurons=new ArrayList<Matrix>();
    ArrayList<Matrix> errors=new ArrayList<Matrix>();

    Matrix target=new Matrix(targetArray);
    target.T();
    Matrix output=new Matrix(inputArray);
    output.T();
    neurons.add(output.copy());

    output.printMatrix();
    for (int i=0; i<weights.size(); i++) {
      println("WEIGHT :");
      weights.get(i).printMatrix();

      println("WEIGHT X INPUT :");
      output=Matrix.mult(weights.get(i), output);
      output.printMatrix();

      println("BIAS :");
      output=Matrix.add(output, biases.get(i));
      biases.get(i).printMatrix();

      println("INPUT + BIAS :");
      output.printMatrix();

      println("SIGMOID :");
      output=Matrix.sigmoid(output);
      output.printMatrix();
      println();
      println();
      neurons.add(output.copy());
    }
    errors.add(Matrix.sub(target, output));
    println("ERROR :");
    errors.get(0).printMatrix();

    for (int i=weights.size()-1; i>0; i--) {
      Matrix transposedWeight=Matrix.getT(weights.get(i));
      println("TRANSPOSED WEIGHT :");
      transposedWeight.printMatrix();
      for (int j=0; j<transposedWeight.row; j++) {
        float sumOfRow=0;
        for (int k=0; k<transposedWeight.column; k++) {
          sumOfRow+=transposedWeight.array[j][k];
        }
        println("SUM OF ROW "+ str(j)+" :");
        println(sumOfRow);
        for (int k=0; k<transposedWeight.column; k++) {
          if (sumOfRow!=0)
            transposedWeight.array[j][k]*=(1/sumOfRow);
        }
        println("TRANSPOSED WEIGHT ROW "+ str(j)+" / "+"SUM OF ROW :");
        printArray(transposedWeight.array[j]);
      }

      Matrix error=Matrix.mult(transposedWeight, errors.get(0));
      println("ERROR LAYER "+str(i)+" :");
      error.printMatrix();
      errors.add(0, error);
    }

    for (int i=weights.size()-1; i>=0; i--) {

      Matrix derivatedSigmoid=neurons.get(i+1).copy();
      Matrix inverseMatrix=derivatedSigmoid.copy();
      inverseMatrix.set(1);
      inverseMatrix=Matrix.sub(inverseMatrix, derivatedSigmoid);
      derivatedSigmoid=Matrix.hadamartProduct(derivatedSigmoid, inverseMatrix);
      Matrix gradient=Matrix.hadamartProduct(errors.get(i), derivatedSigmoid);

      Matrix slope=Matrix.mult(gradient, Matrix.getT(neurons.get(i)));
      slope.mult(learningRate);

      Matrix weight=weights.get(i).copy();
      weights.remove(i);
      weights.add(i, Matrix.add(weight, slope));

      Matrix bias=biases.get(i).copy();
      biases.remove(i);
      biases.add(i, Matrix.add(bias, gradient));
      gradient.printMatrix();

      slope.printMatrix();
    }
  }
}
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
  public void show() {
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
  public void move() {
    x-=gameSpeed;
  }
}
class Population{

}
  public void settings() {  size(800, 600, P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Trained_Flappy_Bird_Game_Neuroevolution" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

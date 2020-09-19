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

  float [] feedForward(float [] input) {
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

  void mutate(float mutationRate) {
    for (Matrix weight : weights)
      weight.mutate(mutationRate);
    for (Matrix bias : biases)
      bias.mutate(mutationRate);
  }

  NeuralNetwork crossover(NeuralNetwork other) {
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

  NeuralNetwork copy() {
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

  void train(float [] inputArray, float [] targetArray) {
    float learningRate=0.5;
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

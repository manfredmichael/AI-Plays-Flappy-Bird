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

  float get(int i, int j) {
    return array[i][j];
  }

  float [][] getArray() {
    return array;
  }

  void set(float n) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=n;
      }
    }
  }

  Matrix copy() {
    Matrix result=new Matrix(row, column);
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        result.array[i][j]=array[i][j];
      }
    }
    return result;
  }

  void printMatrix() {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        print(array[i][j]+" ");
      }
      println();
    }
    println();
  }

  void T() {
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

  void add(float n) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=array[i][j]+n;
      }
    }
  } 

  void mult(float n) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=array[i][j]*n;
      }
    }
  }

  void mutate(float mutationRate) {
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
  Matrix mult(Matrix a, Matrix b) {
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

  Matrix add(Matrix a, Matrix b) {
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

  Matrix sub(Matrix a, Matrix b) {
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

  Matrix getT(Matrix a) {
    Matrix result=new Matrix(a.column, a.row);
    for (int i=0; i<result.row; i++) {
      for (int j=0; j<result.column; j++) {
        result.array[i][j]=a.array[j][i];
      }
    }
    return result;
  }

  Matrix hadamartProduct(Matrix a, Matrix b) {
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

  Matrix sigmoid(Matrix a) {
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

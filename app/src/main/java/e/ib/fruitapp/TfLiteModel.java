package e.ib.fruitapp;

import android.graphics.Bitmap;
import android.os.Trace;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.nio.MappedByteBuffer;

public class TfLiteModel {

    private Interpreter tflite;

    public TfLiteModel(MappedByteBuffer loadedModelFile) {
        this.tflite = new Interpreter(loadedModelFile, new Interpreter.Options());
    }

    public float[][] doInference(Float[][][] input) {
        byte[][][] inputt = toPrimitives(input);
        float[][][] output = createOutputArray();

        tflite.run(inputt, output);
        float[] classes = new float[10];
        float[] scores = new float[10];

        for(int i = 0; i < 10; i++){
            classes[i] = output[1][i][0];
            scores[i] = output[2][i][0];
        }
        return new float[][]{classes, scores};
    }

    private byte[][][] toPrimitives(Float[][][] input) {
        byte[][][] otp = new byte[300][300][3];
        for (int i = 0; i < otp.length; i++) {
            for(int j = 0; j < otp[0].length; j++){
                for(int k = 0; k < otp[0][0].length; k++){
                    otp[i][j][k] = input[i][j][k].byteValue();
                }
            }
        }
        return otp;
    }


    private float[][][] createOutputArray() {
        float[][] otp1 = new float[10][4];//10,4
        float[][] otp2 = new float[10][4];//10
        float[][] otp3 = new float[10][4];//10
        float[][] otp4 = new float[10][4];//1
        return new float[][][]{otp1, otp2, otp3, otp4};
    }


    public static void main(String[] args) {
        //Tensor t = new Tensor();
    }

}

package ru.emiren.snakeai.ai;

import java.awt.desktop.AppHiddenEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class AI {
    private int inputSize;
    private int hiddenSize;
    private int outputSize;

    private double score = 0;

    private ArrayList<ArrayList<Double>> input;
    private ArrayList<ArrayList<Double>> weights;

    private double LEARNING_RATE = 0.5;

    // private final File weightsFile = new File("...");
    // private final File GameLogs = new File("...");

    public AI(int inputSize, int hiddenSize, int outputSize){
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;

        for (int i = 0; i < inputSize; i++)
            input = new ArrayList<>(hiddenSize);
        for (int i = 0; i < hiddenSize; i++)
            weights = new ArrayList<>(outputSize);

        loadWeights();
    }

    public int getAction(ArrayList<Double> state) {
        ArrayList<Double> output = forward(state);

        int action = 0;
        double max = output.get(0);

        for (int i = 1; i < outputSize; i++)
            if (output.get(i) > max){
                max = output.get(i);
                action = i;
            }
        return action;
    }

    public void train(ArrayList<Double> state, int action, ArrayList<Double> nextState, double reward) {
        score += reward;
        ArrayList<Double> output = forward(state);
        ArrayList<Double> nextOutput = forward(nextState);

        ArrayList<Double> targetOutput = (ArrayList<Double>) output.clone();
        targetOutput.set(action, reward + getMaxQ(nextOutput));

        ArrayList<Double> houtput = new ArrayList<>(hiddenSize);
        for (int i = 0; i < hiddenSize; i++){
            double sum = 0;
            for (int j = 0; j < inputSize; j++)
                sum += state.get(j) * input.get(j).get(i);
            houtput.set(i, sigmoid(sum));
        }

        for (int i = 0; i < hiddenSize; i++)
            for (int j = 0; j < outputSize; j++)
                weights.get(i).set(j, weights.get(i).get(j) + LEARNING_RATE * houtput.get(i) * (targetOutput.get(j) - output.get(j)));

        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++){
                double sum = 0;
                for (int k = 0; k < outputSize; k++)
                    sum += (targetOutput.get(k) - output.get(k)) * weights.get(j).get(k);

                weights.get(i).set(j, weights.get(i).get(j) + LEARNING_RATE * state.get(i) * houtput.get(j) * (1 - houtput.get(j)) * sum);
            }
    }

    public ArrayList<Double> forward(ArrayList<Double> input) {
        ArrayList<Double> houtput = new ArrayList<>(hiddenSize);

        for (int i = 0; i < hiddenSize; i++){
            double sum = 0;
            for (int j = 0; j < inputSize; j++)
                sum += input.get(j) * this.input.get(j).get(i);
            houtput.set(i, sigmoid(sum));
        }

        ArrayList<Double> output = new ArrayList<>(outputSize);
        for (int i = 0; i < outputSize; i++){
            double sum = 0;
            for (int j = 0 ; j < hiddenSize; j++)
                sum += houtput.get(j) * weights.get(j).get(i);
            output.set(i, sum);
        }

        return output;
    }

    private double sigmoid(double x) { return 1 / 1 + Math.exp(-x); }
    private double getMaxQ(ArrayList<Double> output) {
        double max = output.get(0);

        for (int i = 1; i < output.size(); i++) {

            double num = output.get(i);

            if (num > max)
                max = num;
        }
        return max;
    }

    private void initializeWeights(ArrayList<ArrayList<Double>> weights) { }
    private void loadWeights() { }

    private void setWeights(ArrayList<ArrayList<Double>> weights) {
        inputSize =  weights.get(0).get(0).intValue();
        hiddenSize = weights.get(0).get(1).intValue();
        outputSize = weights.get(0).get(2).intValue();

        input = new ArrayList<>();
        weights = new ArrayList<>();
        for (int i = 0; i < inputSize; i++) {
            input.add(new ArrayList<>(hiddenSize));
            input.set(i, (ArrayList<Double>) weights.get(i + 1).clone());

        }
        for (int i = 0; i < hiddenSize; i++) {
            weights.add(new ArrayList<>(outputSize));
            this.weights.set(i, (ArrayList<Double>) weights.get(inputSize + i + 1).clone());
        }
    }

    public ArrayList<ArrayList<Double>> getWeights() {
        ArrayList<ArrayList<Double>> weights = new ArrayList<ArrayList<Double>>(inputSize + hiddenSize + 2);

        ArrayList<Double> weight = new ArrayList<>(new ArrayList<Double>(Arrays.<Double>asList((double) inputSize, (double) hiddenSize, (double) outputSize)));
        weights.set(0, weight);

        for (int i = 0; i < inputSize; i++)
            weights.set(i + 1, (ArrayList<Double>) input.get(i).clone());

        for (int i = 0; i < hiddenSize; i++)
            weights.set(inputSize + i + 1, (ArrayList<Double>) this.weights.get(i).clone());

        System.out.println("Probability of error");
        return weights;

    }


    public void saveScore(long time, int applesEaten) { }

    private void createTrainingFile(boolean fileType) { }
}

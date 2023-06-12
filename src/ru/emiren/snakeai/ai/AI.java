package ru.emiren.snakeai.ai;

import ru.emiren.snakeai.engine.Game;

import java.awt.desktop.AppHiddenEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AI {
    private int inputSize;
    private int hiddenSize;
    private int outputSize;

    private double score = 0;

    private ArrayList<ArrayList<Double>> input;
    private ArrayList<ArrayList<Double>> weights;

    private double LEARNING_RATE = 0.5;

     private final String weightsFilePath = "Data/weights.txt";
     private final String gameLogsPath = "Data/gameLogs.txt";
     private File weightsFile = new File(weightsFilePath);
     private File gameLogs = new File(gameLogsPath);

     private static Random random = new Random();

    public AI(int inputSize, int hiddenSize, int outputSize){
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;

        input = new ArrayList<>();
        for (int i = 0; i < inputSize; i++) {
            ArrayList<Double> row = new ArrayList<>(hiddenSize);
            for (int j = 0; j < hiddenSize; j++)
                row.add(0.0);
            input.add(row);
        }

        weights = new ArrayList<>();
        for (int i = 0; i < hiddenSize + 1; i++) {
            ArrayList<Double> row = new ArrayList<>();
            for (int j = 0; j < outputSize + 1; j++)
                row.add(0.0);
            weights.add(row);
        }

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

        ArrayList<Double> targetOutput = new ArrayList<>(output);
        targetOutput.set(action, reward + getMaxQ(nextOutput));

        ArrayList<Double> houtput = new ArrayList<>(hiddenSize);
        for (int i = 0; i < hiddenSize; i++)
            houtput.add(0.0);

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
            for (int j = 0; j < hiddenSize; j++) {
                double sum = 0;
                for (int k = 0; k < outputSize; k++)
                    sum += (targetOutput.get(k) - output.get(k)) * weights.get(j).get(k);

                double currentWeight = input.get(i).get(j);
                double updatedWeight = currentWeight + LEARNING_RATE * state.get(i) * houtput.get(j) * (1 - houtput.get(j)) * sum;
                input.get(i).set(j, updatedWeight);
            }
    }

    public ArrayList<Double> forward(ArrayList<Double> argInput) {

        ArrayList<Double> hiddenOutput = new ArrayList<>(hiddenSize);

        System.out.println("Forward 1");

        System.out.println(inputSize + " " + hiddenSize + " " + outputSize);

        for (int i = 0; i < hiddenSize; i++) {
            hiddenOutput.add(0.0);
        }



        for (int i = 0; i < hiddenSize; i++) {
            double sum = 0;
            for (int j = 0; j < inputSize; j++) {
                sum += argInput.get(j) * input.get(j).get(i);
            }
            hiddenOutput.set(i, sigmoid(sum));
        }

        for (int i = 0; i < hiddenSize; i++) {
            System.out.println(hiddenOutput.get(i));
        }

        System.out.println("Forward 2");
        ArrayList<Double> output = new ArrayList<>(outputSize);
        for (int i = 0; i < outputSize; i++) {
            output.add(0.0);
        }
        System.out.println();
        for (int i = 0; i < outputSize; i++) {
            System.out.println(output.get(i));
        }

        System.out.println("Forward 3");
        for (int i = 0; i < outputSize; i++) {
            double sum = 0;
            for (int j = 0; j < hiddenSize; j++) {
                sum += hiddenOutput.get(j) * weights.get(j).get(i);
            }
            output.set(i, sum);
        }

        System.out.println("Forward end");
        System.out.println("\n");
        for (Double arg : output) {
            System.out.println(arg);
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

    private void setWeights(ArrayList<ArrayList<Double>> weights) {
        inputSize =  weights.get(0).get(0).intValue();
        hiddenSize = weights.get(0).get(1).intValue();
        outputSize = weights.get(0).get(2).intValue();

        input = new ArrayList<>();
        weights = new ArrayList<>();
        for (int i = 0; i < inputSize; i++) {
            input.add(new ArrayList<>(hiddenSize));
            input.get(i).addAll(weights.get(i + 1));

        }
        for (int i = 0; i < hiddenSize; i++) {
            weights.add(new ArrayList<>(outputSize));
            this.weights.get(i).addAll( weights.get(inputSize + i + 1));
        }
    }

    public ArrayList<ArrayList<Double>> getWeights() {
        ArrayList<ArrayList<Double>> weight = new ArrayList<ArrayList<Double>>();

        for (int i = 0; i < inputSize + hiddenSize + 2; i++)  {
            weight.add(new ArrayList<>());
        }

        weight.get(0).add((double) inputSize);
        weight.get(0).add((double) hiddenSize);
        weight.get(0).add((double) outputSize);

        for (int i = 0; i < inputSize; i++)
            weight.get(i + 1).addAll (input.get(i));

        for (int i = 0; i < hiddenSize; i++)
            weight.get(inputSize + i + 1).addAll(weights.get(i));

        return weight;

    }

    private void initializeWeights(ArrayList<ArrayList<Double>> weight) {
        for (int i = 0; i < weight.size(); i++)
            for (int j = 0; j < weight.get(i).size(); j++)
                weight.get(i).set(j, random.nextDouble() * 2 - 1);
    }

    private void loadWeights() {
        ArrayList<ArrayList<Double>> weightsFromFile = new ArrayList<>();

        System.out.println(weightsFile.length());
        if (weightsFile.exists() && weightsFile.length() > 0) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(weightsFilePath));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] weightStrings = line.trim().split(" ");
                    ArrayList<Double> row = new ArrayList<>();
                    for (String weightString : weightStrings) {
                        row.add(Double.parseDouble(weightString));
                    }
                    weightsFromFile.add(row);
                }
                setWeights(weightsFromFile);
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            createTrainingFile(true);
            initializeWeights(input);
            initializeWeights(weights);
            saveWeights();
        }


    }

    public void saveWeights() {
        try {
            FileWriter writer = new FileWriter(weightsFile);
            ArrayList<ArrayList<Double>> weightsToFile = getWeights();
            for (ArrayList<Double> row : weights) {
                for (Double weight : row)
                    writer.write(weight.toString() + " ");
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            saveWeights();
            e.printStackTrace();
        }
    }
//        try {
//            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(weightsFile));
//            out.writeObject(getWeights());
//            out.close();
//        } catch (IOException e) {
//            saveWeights();
//        }


    public void saveScore(long time, int applesEaten) {
        try (FileWriter writer = new FileWriter(gameLogs, true)) {
            writer.append("Time played: ").append(String.valueOf((System.currentTimeMillis() - time) / Game.DELAY)).append(" | Apples eaten: ").append(String.valueOf(applesEaten)).append(" | Score: ").append(String.valueOf(score)).append("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            createTrainingFile(true);
        }
    }

    private void createTrainingFile(boolean fileType){
        // True for weights file, false for game logs.
        File file = (fileType) ? weightsFile : gameLogs;
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package ru.emiren.snakeai.engine;

import ru.emiren.snakeai.ai.AI;
import ru.emiren.snakeai.components.Apple;

import javax.swing.*;
import java.util.ArrayList;

public class Frame {
    //
    private static JFrame frame;
    private static Game game = new Game();

    public Frame() {
        frame = new JFrame("Snake-AI");
        frame.setResizable(false);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

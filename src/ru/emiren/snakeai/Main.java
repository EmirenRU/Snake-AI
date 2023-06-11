package ru.emiren.snakeai;

import ru.emiren.snakeai.engine.Frame;

public class Main {
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Frame();
            }
        }).start();
    }
}
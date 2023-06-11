package ru.emiren.snakeai.components;

import ru.emiren.snakeai.engine.Game;

import java.awt.*;

public class Apple {
    Node pos;

    public Apple(int x, int y){
        pos = new Node(x, y);
    }

    public void draw(Graphics g){
        g.setColor(Color.RED);
        g.fillOval(pos.getX(), pos.getY(), Game.DOT_SIZE, Game.DOT_SIZE);
    }

    public Node getPos() {
        return pos;
    }
}

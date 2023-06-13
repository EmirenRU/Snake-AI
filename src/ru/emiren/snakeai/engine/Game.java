package ru.emiren.snakeai.engine;

import ru.emiren.snakeai.ai.AI;
import ru.emiren.snakeai.components.Apple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Game extends JPanel implements ActionListener {

    // Settings
    private final int WIDTH  = 720;
    private final int HEIGHT = 720;
    public static final int DOT_SIZE = 10;
    private final int ROW_DOTS = WIDTH / DOT_SIZE;
    private final int COL_DOTS = HEIGHT / DOT_SIZE;
    private final int ALL_DOTS = (WIDTH * HEIGHT) / (DOT_SIZE * DOT_SIZE);
    public static final int DELAY = 33;

    private ArrayList<Integer> x;
    private ArrayList<Integer> y;

    private boolean inGame = true;

    private int SNAKE_SIZE = 3;

    private ArrayList<Boolean> dir;


    private Random random;

    private Timer timer;
    private Apple apple;
    private AI ai;

    private long gameTime;
    private int applesEaten;
    private int recentWallAttempts;

    public Game(){ initGame(); }

    private void initGame() {
        x = new ArrayList<Integer>();
        y = new ArrayList<Integer>();

        random = new Random();

        for (int i = 0; i < ROW_DOTS; i++)
            x.add(0);

        for (int i = 0; i < COL_DOTS; i++)
            y.add(0);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);

        ai = new AI( 12, 8, 4);

        this.apple = generateApple();
        applesEaten = 0;
        initGameParams();
        timer = new Timer(DELAY, this);
        gameTime = System.currentTimeMillis();
        timer.start();
    }

    public void restart() {
        inGame = true;
        initGame();
    }

    private void initGameParams() {
        SNAKE_SIZE = 3;

        int x0 = new Random().nextInt(WIDTH);
        int y0 = new Random().nextInt(HEIGHT);

        x.set(0, 300);
        y.set(0, 300);

        for (int i = 0; i < SNAKE_SIZE; i++) {
            x.set(i, x0 - i * DOT_SIZE);
            y.set(i, y0);
        }

        dir = new ArrayList<>(Arrays.asList(true, false, false, false));
        recentWallAttempts = 0;
    }

    private Apple generateApple() {
        int x = random.nextInt(ROW_DOTS) * DOT_SIZE;
        int y = random.nextInt(COL_DOTS) * DOT_SIZE;

        if ( x >= ROW_DOTS - 30 )
            x -= DOT_SIZE * 3;
        if (x <= 30)
            x += DOT_SIZE * 3;
        if ( y >= COL_DOTS)
            y -= DOT_SIZE * 3;
        if (y <= 30)
            y += DOT_SIZE * 3;

        return new Apple(x, y);
    }

    private void move() {
        ArrayList<Double> currentState = getInput();

        int dx = apple.getPos().getX() - x.get(0);
        int dy = apple.getPos().getY() - y.get(0);

        double foodDistance = Math.sqrt(dx * dx + dy * dy);

        int action = ai.getAction(currentState);

        System.out.println("Action = " + action);

        if (action == 0)
            moveLeft();
        else if (action == 1)
            moveRight();
        else if (action == 2)
            moveUp();
        else if (action == 3)
            moveDown();

        moveSnake();
        checkCollision();

        giveReward(currentState, action, foodDistance);

    }

    // abs(apple.x - x[0]) < DOT_SIZE

    private void giveReward(ArrayList<Double> currentState, int action, double foodDistances) {


        if ((apple.getPos().getX() + DOT_SIZE > x.get(0))
                && (x.get(0) >= apple.getPos().getX())
                && (apple.getPos().getY() + DOT_SIZE > y.get(0))
                && (y.get(0) >= apple.getPos().getY())
        ){
            SNAKE_SIZE++;
            applesEaten++;

            double reward = calculateReward(foodDistances, true, action);
            ai.train(currentState, action, getInput(), reward);
            apple = generateApple();
        } else
            ai.train(currentState,action,getInput(),calculateReward(foodDistances, false, action));

        if (!inGame)
            ai.train(currentState, action, getInput(), -10.0);
    }

    private double calculateReward(double foodDistances, boolean ateApple, int action) {
        double distToWall = Math.min(y.get(0),
                Math.min(WIDTH - x.get(0),
                        Math.min(HEIGHT - y.get(0), x.get(0))
                )
        );

        if (distToWall <= DOT_SIZE) {
            recentWallAttempts++;
            return -10.0;
        }

        recentWallAttempts = 0;

        if (ateApple)
            return 10.0;

        // Carrot and Stick
        if (y.get(0) == apple.getPos().getY()){
            if (apple.getPos().getX() > x.get(0)){
                if (action == 1)
                    return 0.5;
                else
                    return -0.5;
            }
            else if (apple.getPos().getX() < x.get(0)) {
                if (action == 0)
                    return 0.5;
                else
                    return -0.5;
            }
        }

        if (x.get(0) == apple.getPos().getX()){
            if (apple.getPos().getY() > y.get(0)){
                if (action == 3)
                    return 0.5;
                else
                    return -0.5;
             }
            else if (apple.getPos().getY() < y.get(0)){
                if (action == 2)
                    return 0.5;
                else
                    return -0.5;
            }
        }

        int dx = apple.getPos().getX() - x.get(0);
        int dy = apple.getPos().getY() - y.get(0);

        double newFoodDistance = Math.sqrt(dx*dx + dy*dy);

        if (newFoodDistance < foodDistances) {
            System.out.println("Moving toward the Apple.");
            return 0.01;
        }

        System.out.println("Moving the opposite side of the Apple");
        return -0.1;
    }

    private void moveLeft() {
        dir.set(0, true);
        dir.set(1, false);
        dir.set(2, false);
        dir.set(3, false);
    }

    private void moveRight() {
        dir.set(0, false);
        dir.set(1, true);
        dir.set(2, false);
        dir.set(3, false);
    }
    private void moveUp() {
        dir.set(0, false);
        dir.set(1, false);
        dir.set(2, true);
        dir.set(3, false);
    }

    private void moveDown() {
        dir.set(0, false);
        dir.set(1, false);
        dir.set(2, false);
        dir.set(3, true);
    }

    private void moveSnake() {
        x.set(0, (dir.get(0)) ? x.get(0) - DOT_SIZE : x.get(0));
        x.set(0, (dir.get(1)) ? x.get(0) + DOT_SIZE : x.get(0));
        y.set(0, (dir.get(2)) ? y.get(0) - DOT_SIZE : y.get(0));
        y.set(0, (dir.get(3)) ? y.get(0) + DOT_SIZE : y.get(0));

        for (int i = SNAKE_SIZE; i > 0; i--){
            x.set(i, x.get(i - 1));
            y.set(i, y.get(i - 1));
        }
    }

    private ArrayList<Double> getInput() {
        ArrayList<Double> input = new ArrayList<>(12);

        System.out.println("GetInputStart");

        System.out.println("Adding");
        for (int i = 0; i < 12; i++)
            input.add(0.0);
        /*
        0 - Snake x
        1 - Snake y
        2 - Apple x
        3 - Apple y
        4 - Direction left-right
        5 - Direction up-down
        6 - Distance to left wall
        7 - Distance to right wall
        8 - Distance to top wall
        9 - Distance to bottom wall
        10 - Distance from Snake to Apple by x
        11 - Snake dots amount
         */

        input.set(0, (double) (x.get(0)));
        input.set(1, (double) (y.get(0)));

        input.set(2, (double) apple.getPos().getX());
        input.set(3, (double) apple.getPos().getY());

        if (dir.get(0))
            input.set(4, 1.0);
        else if (dir.get(1))
            input.set(4, -1.0);
        if (dir.get(2))
            input.set(5, 1.0);
        else if (dir.get(3))
            input.set(5, -1.0);

        input.set(6, (double) (x.get(0)));
        input.set(7, (double) (WIDTH - x.get(0)));
        input.set(8, (double) (y.get(0)));
        input.set(9, (double) (HEIGHT - y.get(0)));

        int dx = apple.getPos().getX() - x.get(0);
        int dy = apple.getPos().getY() - y.get(0);

        input.set(10, Math.sqrt((dx * dx + dy * dy)));
        input.set(11, (double) SNAKE_SIZE);

        for (Double arg : input)
            System.out.println(arg);

        return input;
    }

    private void checkCollision() {
        if (y.get(0) >= HEIGHT || y.get(0) < 0 || x.get(0) >= WIDTH || x.get(0) < 0 )
            inGame = false;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (inGame) {
            apple.draw(g);

            for (int i = 0; i < SNAKE_SIZE; i++){
                if ( i == 0 )
                    g.setColor(Color.GREEN);
                else
                    g.setColor(Color.WHITE);

                g.fillRect(x.get(i), y.get(i), DOT_SIZE, DOT_SIZE);
            }

            Toolkit.getDefaultToolkit().sync();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame)
            move();
        else{
            ai.saveWeights();
            ai.saveScore(gameTime, applesEaten);
            timer.stop();
            if (applesEaten > 9){
                System.out.println("Stopped training, Ai ate 10 apples in one turn.");
            } else{
                restart();
            }

            long currentGameTime = System.currentTimeMillis();
            if (currentGameTime - gameTime > 5){
                gameTime = currentGameTime;
                restart();
            }
            else
                restart();
        }
        repaint();
    }


    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public int getROW_DOTS() {
        return ROW_DOTS;
    }

    public int getCOL_DOTS() {
        return COL_DOTS;
    }

    public int getALL_DOTS() {
        return ALL_DOTS;
    }

    public int getSNAKE_SIZE() {
        return SNAKE_SIZE;
    }

    public void setSNAKE_SIZE(int SNAKE_SIZE) {
        this.SNAKE_SIZE = SNAKE_SIZE;
    }

    public ArrayList<Boolean> getDir() {
        return dir;
    }

    public void setDir(ArrayList<Boolean> dir) {
        this.dir = dir;
    }

    public AI getAi() {
        return ai;
    }

    public void setAi(AI ai) {
        this.ai = ai;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        gameTime = gameTime;
    }

    public Apple getApple() {
        return apple;
    }

    public void setApple(Apple apple) {
        this.apple = apple;
    }

    public int getApplesEaten() {
        return applesEaten;
    }

    public void setApplesEaten(int applesEaten) {
        applesEaten = applesEaten;
    }

    public int getRecentWallAttempts() {
        return recentWallAttempts;
    }

    public void setRecentWallAttempts(int recentWallAttempts) {
        recentWallAttempts = recentWallAttempts;
    }
}

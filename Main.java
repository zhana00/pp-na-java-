package com.example.sn;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends Application {
    // variable
    static int speed = 5;
    static int foodcolor = 0;
    static int width = 20;
    static int height = 20;
    static int foodX = 0;
    static int foodY = 0;
    static int cornersize = 25;
    static List<Corner> snake = new ArrayList<>(); //квадратчетата, в които се състои змията
    static Dir direction = Dir.left;
    static boolean gameOver = false;
    static Random rand = new Random();

    // enum защото посоките са константи
    public enum Dir {
        left, right, up, down
    }

    public static class Corner {
        int x;
        int y;

        public Corner(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }

    public void start(Stage primaryStage) {
        try {
            newFood();
            // VBox позиционира всички свои дъщерни възли (компоненти) във вертикална колона - един върху друг.
            VBox root = new VBox();
            // Canvas e празна правоъгълна област на екрана, върху която приложението може да рисува
            // или от която приложението може да улавя входни събития от потребителя.
            Canvas c = new Canvas(width * cornersize, height * cornersize);
            //GraphicsContext се използва за издаване на повиквания за рисуване към Canvas с помощта на буфер.
            // Всяко повикване слага необходимите параметри в буфера, където те по-късно ще бъдат изобразени.
            GraphicsContext gc = c.getGraphicsContext2D();
            root.getChildren().add(c);

            // флип книга
            new AnimationTimer() {
                long lastTick = 0;

                public void handle(long now) {
                    if (lastTick == 0) {
                        lastTick = now;
                        tick(gc);
                        return;
                    }
                    //увеличаване скоростта на змията
                    if (now - lastTick > 1000000000 / speed) {
                        lastTick = now;
                        tick(gc);
                    }
                }

            }.start();

            Scene scene = new Scene(root, width * cornersize, height * cornersize);

            // control
            // задаване на бутоните за контрол на змията
            scene.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
                if (key.getCode() == KeyCode.UP) {
                    direction = Dir.up;
                }
                if (key.getCode() == KeyCode.LEFT) {
                    direction = Dir.left;
                }
                if (key.getCode() == KeyCode.DOWN) {
                    direction = Dir.down;
                }
                if (key.getCode() == KeyCode.RIGHT) {
                    direction = Dir.right;
                }

            });

            // add start snake parts
            //задаваме начално състояние на змията 3 квадратчета
            snake.add(new Corner(width / 2, height / 2));
            snake.add(new Corner(width / 2, height / 2));
            snake.add(new Corner(width / 2, height / 2));

            primaryStage.setScene(scene);
            primaryStage.setTitle("SNAKE GAME");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tick
    public static void tick(GraphicsContext gc) {
        if (gameOver) {
            gc.setFill(Color.RED);
            gc.setFont(new Font("", 50));
            gc.fillText("GAME OVER", 100, 250);
            return;
        }

        for (int i = snake.size() - 1; i >= 1; i--) {
            snake.get(i).x = snake.get(i - 1).x;
            snake.get(i).y = snake.get(i - 1).y;
        }
        //змията умира ако докосне границите
        switch (direction) {
            case up -> {
                snake.get(0).y--;
                if (snake.get(0).y < 0) {
                    gameOver = true;
                }
            }
            case down -> {
                snake.get(0).y++;
                if (snake.get(0).y > height) {
                    gameOver = true;
                }
            }
            case left -> {
                snake.get(0).x--;
                if (snake.get(0).x < 0) {
                    gameOver = true;
                }
            }
            case right -> {
                snake.get(0).x++;
                if (snake.get(0).x > width) {
                    gameOver = true;
                }
            }
        }

        // eat food
        if (foodX == snake.get(0).x && foodY == snake.get(0).y) {
            snake.add(new Corner(-1, -1)); //змията расте като изяде храна
            newFood();
        }

        // self destroy
        //умира ако удари себе си
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(0).x == snake.get(i).x && snake.get(0).y == snake.get(i).y) {
                gameOver = true;
                break;
            }
        }

        // fill
        // background
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, width * cornersize, height * cornersize);

        // score
        gc.setFill(Color.DARKCYAN);
        gc.setFont(new Font("", 30));
        gc.fillText("Score: " + (speed - 6), 10, 30);

        // random foodcolor
        Color cc = switch (foodcolor) {
            case 0 -> Color.PURPLE;
            case 1 -> Color.BLUE;
            case 2 -> Color.RED;
            case 3 -> Color.BROWN;
            case 4 -> Color.ORANGE;
            default -> Color.WHITE;
        };

        gc.setFill(cc);
        gc.fillOval(foodX * cornersize, foodY * cornersize, cornersize, cornersize);

        // snake
        for (Corner c : snake) {
            gc.setFill(Color.BLUE); // сянка
            gc.fillRect(c.x * cornersize, c.y * cornersize, cornersize - 1, cornersize - 1);
            gc.setFill(Color.BLUEVIOLET); // основен цвят
            gc.fillRect(c.x * cornersize, c.y * cornersize, cornersize - 2, cornersize - 2);

        }

    }

    // food
    //случайна позиция на храна, там където няма змия
    //случаен цвят на новата храна и увеличаване на скоростта
    public static void newFood() {
        start:
        while (true) {
            foodX = rand.nextInt(width);
            foodY = rand.nextInt(height);

            for (Corner c : snake) {
                if (c.x == foodX && c.y == foodY) {
                    continue start;
                }
            }
            foodcolor = rand.nextInt(5);
            speed++;
            break;

        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

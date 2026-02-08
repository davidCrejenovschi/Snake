package snakeproject.utils;

import java.util.LinkedList;

public class Snake {

    private LinkedList<Coordinate2D<Integer>> body = new LinkedList<>();
    private String direction = "left";

    public void init(Coordinate2D<Integer> startPosition) {
        body.clear();
        body.addFirst(startPosition);
    }

    public void move(Coordinate2D<Integer> nextPosition) {
        body.addFirst(nextPosition);
        body.removeLast();
    }
    
    public void grow(Coordinate2D<Integer> foodPosition) {
        body.addFirst(foodPosition);
    }

    public Coordinate2D<Integer> getHead() {
        return body.getFirst();
    }

    public LinkedList<Coordinate2D<Integer>> getBody(){
        return body;
    }

    public Coordinate2D<Integer> getTail(){
        return body.getLast();
    }

    public boolean isBodyPart(Coordinate2D<Integer> pos){
       
        return body.contains(pos);
    }

    public void setDirection(String newDirection){
        direction = newDirection;
    }

    public String getDirection(){
        return direction;
    }
}
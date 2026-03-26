package org.example.utils;

import java.util.HashSet;
import java.util.LinkedList;

public class Snake {

    private LinkedList<Coordinate2D<Integer>> body = new LinkedList<>();
    private final HashSet<Coordinate2D<Integer>> bodySet = new HashSet<>();
    private String direction = "left";

    public void init(Coordinate2D<Integer> head, Coordinate2D<Integer> tail) {
        body.clear();
        bodySet.clear();
        body.add(head);
        body.add(tail);
        bodySet.add(head);
        bodySet.add(tail);
    }

    public void move(Coordinate2D<Integer> nextPosition) {

        Coordinate2D<Integer> removedTail = body.removeLast();
        bodySet.remove(removedTail);

        body.addFirst(nextPosition);
        bodySet.add(nextPosition);
    }

    public void grow(Coordinate2D<Integer> foodPosition) {
        body.addFirst(foodPosition);
        bodySet.add(foodPosition);
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
        return bodySet.contains(pos);
    }

    public void setDirection(String newDirection){
        direction = newDirection;
    }

    public String getDirection(){
        return direction;
    }
}
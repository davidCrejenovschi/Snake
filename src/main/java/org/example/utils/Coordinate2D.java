package org.example.utils;

public class Coordinate2D<T> {
    
    private T x;
    private T y;

    public Coordinate2D(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public void setX(T x) {
        this.x = x;
    }

    public T getY() {
        return y;
    }

    public void setY(T y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate2D<?> that = (Coordinate2D<?>) o;
        return java.util.Objects.equals(x, that.x) && 
               java.util.Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}
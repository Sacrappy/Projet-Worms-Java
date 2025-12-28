package Backend;

public class Pair {

    public final int x;
    public final int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public boolean equals(Pair p){
        return x == p.x && y == p.y;
    }

}

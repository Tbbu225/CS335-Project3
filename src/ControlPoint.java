import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class ControlPoint {

    public Point location;
    private HashSet<ControlPoint> adjacentPoints;

    public ControlPoint(int x, int y) {
        location = new Point(x,y);
        adjacentPoints = new HashSet<>(8);
    }

    public void addAdjacentPoint(ControlPoint controlPoint) {
        if (adjacentPoints.size() == 8)
            return;

        adjacentPoints.add(controlPoint);
    }

    public ArrayList<ControlPoint> getAdjacencySet() {
        ArrayList<ControlPoint> adjacent = new ArrayList<>(adjacentPoints.size());
        adjacent.addAll(adjacentPoints);

        return adjacent;
    }

}

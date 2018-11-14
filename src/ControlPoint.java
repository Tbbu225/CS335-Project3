import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class ControlPoint {

    public Point location;
    public boolean isSelected, isVisible;
    private HashSet<ControlPoint> adjacentPoints;

    public ControlPoint(int x, int y) {
        location = new Point(x,y);
        isSelected = isVisible = false;
        adjacentPoints = new HashSet<>(8);
    }

    public void addAdjacentPoint(ControlPoint controlPoint) {
        if (adjacentPoints.size() == 8)
            return;

        adjacentPoints.add(controlPoint);
        controlPoint.adjacentPoints.add(this);
    }

    public ArrayList<ControlPoint> getAdjacentControlPoints() {
        ArrayList<ControlPoint> adjacent = new ArrayList<>(adjacentPoints.size());
        adjacent.addAll(adjacentPoints);
        return adjacent;
    }

}

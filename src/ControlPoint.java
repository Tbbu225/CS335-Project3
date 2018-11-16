import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class ControlPoint extends Point {

    public boolean isSelected, isVisible;
    private int adjacenyMaximum;
    private HashSet<ControlPoint> adjacentPoints;

    public ControlPoint(int x, int y, int maxAdjacent) {
        super(x,y);
        isSelected = isVisible = false;
        adjacenyMaximum = maxAdjacent;
        adjacentPoints = new HashSet<>(adjacenyMaximum);
    }

    public void addAdjacentPoint(ControlPoint controlPoint) {
        if (adjacentPoints.size() == adjacenyMaximum)
            return;

        if(!this.adjacentPoints.contains(controlPoint))
            adjacentPoints.add(controlPoint);
        if(!controlPoint.adjacentPoints.contains(this))
            controlPoint.adjacentPoints.add(this);
    }

    public ArrayList<ControlPoint> getAdjacentControlPoints() {
        ArrayList<ControlPoint> adjacent = new ArrayList<>(adjacentPoints.size());
        adjacent.addAll(adjacentPoints);
        return adjacent;
    }

    public int getAdjacenyMaximum() {
        return adjacenyMaximum;
    }

}

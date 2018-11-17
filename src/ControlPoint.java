import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

/* ControlPoint
 *
 * This class is an extension of the Point class, and holds extra
 * information necessary to use in establishing a warp grid
 *
 * Authors:
 * Tyler Burkett
 * John Dickens
 */
public class ControlPoint extends Point {

    //Private Members

    //Public flags used for determining if the point
    //is selected and visible, respectively
    public boolean isSelected, isVisible;

    //The max number of points that can be adjacent to this point
    private int adjacencyMaximum;

    //The set of points adjacent to this point
    private HashSet<ControlPoint> adjacentPoints;

    //Constructor
    public ControlPoint(int x, int y, int maxAdjacent) {
        //Call the constructor for Point
        super(x,y);

        //Set the flags initially to false
        //and set the max number of possible adjacent points
        isSelected = isVisible = false;
        adjacencyMaximum = maxAdjacent;

        //Create the set for adjacent control points
        adjacentPoints = new HashSet<>(adjacencyMaximum);
    }

    //Add a point to the set off adjacent control points.
    //Doesn't do anything if the control points are already
    //adjacent or if the calling control point is adjacent to
    //the max number of possible control points
    public void addAdjacentPoint(ControlPoint controlPoint) {
        if (adjacentPoints.size() == adjacencyMaximum)
            return;

            adjacentPoints.add(controlPoint);
            controlPoint.adjacentPoints.add(this);
    }

    //Get a list of control points that are adjacent to this current one
    public ArrayList<ControlPoint> getAdjacentControlPoints() {
        ArrayList<ControlPoint> adjacent = new ArrayList<>(adjacentPoints.size());
        adjacent.addAll(adjacentPoints);
        return adjacent;
    }

    //Get the maximum number of adjacent points this point can have
    public int getAdjacencyMaximum() {
        return adjacencyMaximum;
    }

}

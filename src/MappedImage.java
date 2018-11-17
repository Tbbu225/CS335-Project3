import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
/* MappedImage
 *
 * This class maintains both a buffered image and a set of control points
 * that can used to warp an image.
 *
 * Authors:
 * Tyler Burkett
 * John Dickens
 */
public class MappedImage extends JPanel implements MouseListener, MouseMotionListener{

    //Private members

    //The actual image that is to be displayed under the
    //control point grid
    private BufferedImage image;

    //Associated images; this is used for pairing one images
    //control points with another's
    private ArrayList<MappedImage> associatedImages;

    //The control points on the image
    private ControlPoint[][] mappingPoints;

    //A reference to the location of the currently
    //moving control point and other control points
    //associated with it
    private Point clickedPoint;

    //The number of control points in one column/row
    //of the control grid. This grid is always square
    private int gridSize;

    //The radius of each control point circle
    private int pointSize;

    //The size of the mappedImage
    private int imageLength, imageHeight;


    //Constructor
    public MappedImage(int size, int length, int height, int radius) {
        //Call the JPanel constructor and set the background to be white
        //in case no image is loaded
        super();
        setBackground(Color.WHITE);

        //Set the image to null and allocate the
        //list of associated images
        image = null;
        associatedImages = new ArrayList<>();

        //Set sizes of points, the grid, and the MappedImage
        //itself based on given paramters
        gridSize = size;
        pointSize = radius;
        imageLength = length;
        imageHeight = height;

        //Allocate the grid to be the given size + 2 squared;
        //This is done to have invisible control points that exist
        //on the edge of the panel
        mappingPoints = new ControlPoint[gridSize + 2][gridSize + 2];

        //Set the clicked point to null
        clickedPoint = null;

        //Calculate the distance between points vertically and horizontally
        int widthInc = imageLength / (gridSize + 1);
        int heightInc = imageHeight / (gridSize + 1);

        //Set the appropriate x and y values for the points on the grid
        for (int row = 0; row < mappingPoints.length; row++)
            for (int column = 0; column < mappingPoints[row].length; column++) {

                //Determine the x and y value of the control point.
                //If the point is the last one in the column or row,
                //make sure it goes to the very edge of the image
                int rowVal = heightInc * row;
                int columnVal = widthInc * column;
                if (row == heightInc * (gridSize + 1))
                    rowVal = imageHeight;
                if (column == widthInc * (gridSize + 1))
                    columnVal = imageLength;

                //Create a new control point
                ControlPoint newPoint = new ControlPoint(columnVal, rowVal, 6);

                //If the control point is not on the very edge of the image,
                //make it visible
                if (column != 0 && column != mappingPoints[row].length-1
                   && row != 0 && row != mappingPoints.length-1)
                    newPoint.isVisible = true;

                //Add the new control point to the appropriate part of the list
                mappingPoints[row][column] = newPoint;
            }

        //Connect the control points appropriately
        formGrid();

        //Draw the result
        repaint();

        //Add listeners for mouse commands so that the points can be moved
        addMouseListener(this);
        addMouseMotionListener(this);

    }

    //Copy Constructor
    public MappedImage(MappedImage mappedImage, boolean keepImage) {
        super();
        setBackground(Color.WHITE);

        this.image = null;
        this.gridSize = mappedImage.gridSize;
        this.pointSize = mappedImage.pointSize;
        this.imageLength = mappedImage.imageLength;
        this.imageHeight = mappedImage.imageHeight;
        this.clickedPoint = mappedImage.clickedPoint;

        this.associatedImages = new ArrayList<>(mappedImage.associatedImages.size());
        this.associatedImages.addAll(mappedImage.associatedImages);

        if(keepImage && mappedImage.image != null) {
            this.image = new BufferedImage(mappedImage.image.getWidth(),
                    mappedImage.image.getHeight(),
                    mappedImage.image.getType());
            this.image.getGraphics().drawImage(mappedImage.image, 0, 0, null);
        }

        this.mappingPoints = new ControlPoint[gridSize+2][gridSize+2];

        for (int row = 0; row < mappingPoints.length; row++) {
            for (int column = 0; column < mappingPoints[row].length; column++) {
                ControlPoint oldPoint = mappedImage.mappingPoints[row][column];
                ControlPoint newPoint = new ControlPoint(oldPoint.x, oldPoint.y, oldPoint.getAdjacenyMaximum());
                this.mappingPoints[row][column] = newPoint;
                if (oldPoint.isVisible)
                    newPoint.isVisible = true;
            }
        }

        formGrid();


    }

    public void getImage(String imagePath) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            image = null;
            return;
        }

        Image scaledImage = originalImage.getScaledInstance(imageLength, imageHeight, Image.SCALE_SMOOTH);

        image = new BufferedImage(imageLength, imageHeight, originalImage.getType());

        Graphics2D bufferGraphics = image.createGraphics();
        bufferGraphics.drawImage(scaledImage, 0, 0, null);
    }

    public ControlPoint[][] getMappingPoints() {
        return mappingPoints;
    }

    public void setMappingPoints(ControlPoint[][] mappingPoints) {
        this.mappingPoints = mappingPoints;
    }

    public void setAssociatedImage(MappedImage image) {
        if(this.associatedImages.contains(image) || image.associatedImages.contains(this))
            return;
        associatedImages.add(image);
        image.associatedImages.add(this);
    }

    public int getGridSize() {
        return gridSize;
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.drawImage(image, 0, 0, null);
        for (int row = 0; row < mappingPoints.length; row++)
            for (int column = 0; column < mappingPoints[row].length; column++) {
                ControlPoint controlPoint = mappingPoints[row][column];

                for (ControlPoint adjacentControlPoint : controlPoint.getAdjacentControlPoints()) {
                    g2d.setColor(Color.BLACK);
                    g2d.drawLine(controlPoint.x, controlPoint.y,
                            adjacentControlPoint.x, adjacentControlPoint.y);
                }
            }

        for (int row = 0; row < mappingPoints.length; row++)
            for (int column = 0; column < mappingPoints[row].length; column++) {
                ControlPoint controlPoint = mappingPoints[row][column];
                if (controlPoint.isVisible) {
                    if (controlPoint.isSelected)
                        g2d.setColor(Color.RED);
                    else
                        g2d.setColor(Color.BLACK);
                    g2d.fillOval(controlPoint.x - (pointSize / 2), controlPoint.y - (pointSize / 2), pointSize, pointSize);
                }
            }


    }


    @Override
    public Dimension getPreferredSize() {
        if (image != null)
            return new Dimension(image.getWidth(), image.getHeight());
        else
            return new Dimension(this.imageLength, this.imageHeight);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        Point mouseEventPoint = mouseEvent.getPoint();
        if(clickedPoint == null)
            for (int row = 0; row < mappingPoints.length; row++)
                for (int column = 0; column < mappingPoints[row].length; column++) {
                    ControlPoint controlPoint = mappingPoints[row][column];
                    if (isClose(mouseEventPoint, controlPoint) && controlPoint.isVisible) {
                        clickedPoint = new Point(row, column);
                        mappingPoints[clickedPoint.x][clickedPoint.y].isSelected = true;
                        for(MappedImage mappedImage : associatedImages) {
                            mappedImage.mappingPoints[clickedPoint.x][clickedPoint.y].isSelected = true;
                            mappedImage.repaint();
                        }
                    }
                }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if(clickedPoint != null) {
            mappingPoints[clickedPoint.x][clickedPoint.y].isSelected = false;
            for(MappedImage mappedImage : associatedImages) {
                mappedImage.mappingPoints[clickedPoint.x][clickedPoint.y].isSelected = false;
                mappedImage.repaint();
            }
            clickedPoint = null;
            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if(clickedPoint != null) {
            mappingPoints[clickedPoint.x][clickedPoint.y].x = mouseEvent.getX();
            mappingPoints[clickedPoint.x][clickedPoint.y].y = mouseEvent.getY();
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}

    private boolean isClose(Point location, ControlPoint controlPoint) {
        return Math.sqrt((location.x - controlPoint.x) * (location.x - controlPoint.x)
                + (location.y - controlPoint.y) * (location.y - controlPoint.y)) <= pointSize/2;
    }

    private void formGrid() {

        for(int row = 0; row < mappingPoints.length; row++) {
            for(int column = 0; column < mappingPoints[row].length; column++) {
                int right = column + 1;
                int up = row - 1;

                boolean rightConnects = right < mappingPoints[row].length;
                boolean upConnects = up >= 0;

                if(rightConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[row][right]);
                if(upConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[up][column]);
                if(rightConnects && upConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[up][right]);
            }
        }
    }
}

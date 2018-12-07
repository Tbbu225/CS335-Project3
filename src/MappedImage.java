import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.util.ArrayList;

/* MappedImage
 *
 * This class maintains both a buffered image and a set of control points
 * that can be used to warp an image.
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
    private BufferedImage initialImage;

    //The pathname of the loaded image
    private String path;

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
    private int gridLength, gridHeight;

    //The radius of each control point circle
    private int pointSize;

    //Colors used for drawing control points
    private Color deselected, selected;

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
        initialImage = null;
        path = null;
        associatedImages = new ArrayList<>();

        deselected = Color.BLACK;
        selected = Color.RED;

        //Set sizes of points, the grid, and the MappedImage
        //itself based on given parameters. +2 is added to dimensions
        //for points at the edge of the image
        gridLength =  gridHeight = size + 2;
        pointSize = radius;
        imageLength = length;
        imageHeight = height;

        //Set the clicked point to null
        clickedPoint = null;

        //Make points for grid
        makePoints();

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
        //Call JPanel constructor and set the background
        super();
        setBackground(Color.WHITE);

        //Set image to null
        this.image = null;
        this.initialImage = null;
        this.path = null;

        //Copy members from provided MappedImage
        this.gridHeight = mappedImage.gridHeight;
        this.gridLength = mappedImage.gridLength;
        this.pointSize = mappedImage.pointSize;
        this.deselected = mappedImage.deselected;
        this.selected = mappedImage.selected;
        this.imageLength = mappedImage.imageLength;
        this.imageHeight = mappedImage.imageHeight;
        this.clickedPoint = mappedImage.clickedPoint;

        //Copy the associatedImages list from the provided MappedImage
        this.associatedImages = new ArrayList<>(mappedImage.associatedImages.size());
        this.associatedImages.addAll(mappedImage.associatedImages);

        //If the image is wanted to be copied as well,
        //Take the original image from the provided MappedImage and
        //Draw it onto a new image in this one
        if(keepImage && mappedImage.image != null) {
            this.path = mappedImage.path;
            this.image = new BufferedImage(mappedImage.image.getWidth(),
                    mappedImage.image.getHeight(),
                    mappedImage.image.getType());
            this.initialImage = new BufferedImage(mappedImage.image.getWidth(),
                    mappedImage.image.getHeight(),
                    mappedImage.image.getType());
            this.image.getGraphics().drawImage(mappedImage.image, 0, 0, null);
            this.initialImage.getGraphics().drawImage(mappedImage.image, 0, 0, null);
        }

        //Allocate list of control points
        this.mappingPoints = new ControlPoint[gridHeight][gridLength];

        //Create new control points based on the old control points in the provided MappedImage
        for (int row = 0; row < mappingPoints.length; row++) {
            for (int column = 0; column < mappingPoints[row].length; column++) {
                ControlPoint oldPoint = mappedImage.mappingPoints[row][column];
                ControlPoint newPoint = new ControlPoint(oldPoint.x, oldPoint.y, oldPoint.getAdjacencyMaximum());
                this.mappingPoints[row][column] = newPoint;
                if (oldPoint.isVisible)
                    newPoint.isVisible = true;
            }
        }

        //Connect the new points together
        formGrid();
    }

    //Method to get images for the class
    public void getImage(String imagePath) {
        path = imagePath;
        //Try to read in an image from the provide path.
        //If that fails, set the image to null and return
        try {

            //Read in image into a temporary image
            BufferedImage originalImage = ImageIO.read(new File(path));

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            //Resize the image to fit inside the dimension of the mapped image while keeping
            //the aspect ratio the same
            Image scaledImage;
            if (width >= height) {
                int newHeight = height * imageLength / width;
                scaledImage = originalImage.getScaledInstance(imageLength, newHeight, Image.SCALE_SMOOTH);
            }
            else {
                int newWidth = width * imageHeight / height;
                scaledImage = originalImage.getScaledInstance(newWidth, imageHeight, Image.SCALE_SMOOTH);
            }

            //Copy the scaled image into actual image displayed by the MappedImage
            image = new BufferedImage(imageLength, imageHeight, originalImage.getType());
            initialImage = new BufferedImage(imageLength, imageHeight, originalImage.getType());
            Graphics2D bufferGraphics = image.createGraphics();
            Graphics2D initialBufferGraphics = initialImage.createGraphics();
            bufferGraphics.drawImage(scaledImage, 0, 0, null);
            initialBufferGraphics.drawImage(scaledImage, 0, 0, null);

        } catch (Exception e) {
            image = null;
            initialImage = null;
        }

    }

    //Form list of three points which will form a complete list of all
    //triangles in the grid.
    public ControlPoint[][] getTrianglePoints() {
        ControlPoint[][] pointTriangles = new ControlPoint[(gridLength-1)*2*(gridHeight-1)][3];

        int section = 0;

        for (int row = 0; row < gridLength; row++) {
            for (int column = 0; row < gridHeight; row++) {
                int up = row - 1;
                int down = row + 1;
                int right = column + 1;

                //Determine if the prior connection values would be in range
                boolean upConnects = up >= 0;
                boolean downConnects = down < mappingPoints.length;
                boolean rightConnects = right < mappingPoints[row].length;

                if (row % 2 == 0) {
                    if (rightConnects && downConnects) {
                        pointTriangles[section][1] = mappingPoints[row][column];
                        pointTriangles[section][2] = mappingPoints[row][right];
                        pointTriangles[section][3] = mappingPoints[down][column];
                        section++;
                    }
                }
                else {
                    if(rightConnects && upConnects) {
                        pointTriangles[section][1] = mappingPoints[row][column];
                        pointTriangles[section][2] = mappingPoints[row][right];
                        pointTriangles[section][3] = mappingPoints[up][right];
                        section++;
                    }
                }
            }
        }

        return pointTriangles;
    }

    //Brighten the image
    public void brighten(float scale) {

        //Set rescale to change brightness
        RescaleOp brighten = new RescaleOp(scale, 0.0f, null);

        //Create temporary image of initial image (unprocessed image)
        BufferedImage tempImage = new BufferedImage(initialImage.getWidth(), initialImage.getHeight(),
                                    initialImage.getType());
        tempImage.getGraphics().drawImage(initialImage, 0, 0, null);

        //Brighten image and set as the new image
        brighten.filter(tempImage, image);
    }

    //Get buffered image
    public BufferedImage getBufferedImage() { return image; }

    //Get control points list
    public ControlPoint[][] getMappingPoints() {
        return mappingPoints;
    }

    //Get the grid length of this MappedImage
    public int getGridLength() {
        return gridLength;
    }

    //Get the grid height of this MappedImage
    public int getGridHeight() { return gridHeight; }
    
    //Associate a MappedImage with this one
    public void setAssociatedImage(MappedImage image) {
        if(this.associatedImages.contains(image) || image.associatedImages.contains(this))
            return;
        associatedImages.add(image);
        image.associatedImages.add(this);
    }

    //Change colors used for control points
    public void setColors(Color selected, Color deselected) {
        this.selected = selected;
        this.deselected = deselected;
    }

    //Change size of control point grid
    public void setGridDimensions(int x, int y) {

        //Do nothing if negative numbers are given
        if ( x < 0 || y < 0)
            return;

        //Change the grid length and height
        gridLength = x;
        gridHeight = y;

        //Make the new list of points
        makePoints();

        //Connect the control points together
        formGrid();

        //paint the new grid on the image
        repaint();
    }

    public void setVisibleGridDimensions(int x, int y) {

        //Do nothing if negative numbers are given
        if ( x < 0 || y < 0)
            return;

        //Change the grid length and height
        gridLength = x + 2;
        gridHeight = y + 2;

        //Make the new list of points
        makePoints();

        //Connect the control points together
        formGrid();

        //paint the new grid on the image
        repaint();
    }

    public void setFrameDimensions(int x, int y) {

        //Do nothing if negative numbers are given
        if ( x < 0 || y < 0)
            return;

        //Change the grid length and height
        imageLength = x;
        imageHeight = y;

        //Re-get the image to scale to the new frame
        getImage(path);

        //Make the new list of points
        makePoints();

        //Connect the control points together
        formGrid();

        //paint the new grid on the image
        repaint();

        imageLength = x;
        imageHeight = y;



    }
    //Overwritten methods

    @Override
    public void paint(Graphics graphics) {
        //Call the orignal paint method for JPanel
        super.paint(graphics);

        //Get the graphics context of this MappedImage
        Graphics2D g2d = (Graphics2D) graphics;

        //Draw the actual image onto the MappedImage
        g2d.drawImage(image, 0, 0, null);

        //Draw the lines between adjacent control points
        for (ControlPoint[] mappingPoint : mappingPoints)
            for (ControlPoint controlPoint : mappingPoint) {
                for (ControlPoint adjacentControlPoint : controlPoint.getAdjacentControlPoints()) {
                    g2d.setColor(deselected);
                    g2d.drawLine(controlPoint.x, controlPoint.y,
                            adjacentControlPoint.x, adjacentControlPoint.y);
                }
            }

        //Draw the actual dot of the control image.
        for (ControlPoint[] mappingPoint : mappingPoints)
            for (ControlPoint controlPoint : mappingPoint) {
                if (controlPoint.isVisible) {
                    if (controlPoint.isSelected)
                        g2d.setColor(selected);
                    else
                        g2d.setColor(deselected);
                    g2d.fillOval(controlPoint.x - (pointSize / 2), controlPoint.y - (pointSize / 2), pointSize, pointSize);
                }
            }


    }


    @Override
    public Dimension getPreferredSize() {
            return new Dimension(this.imageLength, this.imageHeight);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        //get the mouse press location
        Point mouseEventPoint = mouseEvent.getPoint();

        //Only select a new point if is not already selected
        if(clickedPoint == null)

            //Check every control point
            for (int row = 0; row < mappingPoints.length; row++)
                for (int column = 0; column < mappingPoints[row].length; column++) {

                    //If the control point is close enough to the mouse press location and
                    //is visible, make it the clicked control point and set both it and all
                    //associated points in other images as selected
                    ControlPoint controlPoint = mappingPoints[row][column];
                    if (isClose(mouseEventPoint, controlPoint) && controlPoint.isVisible) {
                        clickedPoint = new Point(row, column);
                        mappingPoints[clickedPoint.x][clickedPoint.y].isSelected = true;
                        for(MappedImage mappedImage : associatedImages) {
                            mappedImage.mappingPoints[clickedPoint.x][clickedPoint.y].isSelected = true;
                            mappedImage.repaint();
                        }
                        return;
                    }
                }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        //Only do this if a point is selected
        if(clickedPoint != null) {

            //Set the clicked control point and all associated control points in other
            //images as not selected
            mappingPoints[clickedPoint.x][clickedPoint.y].isSelected = false;
            for(MappedImage mappedImage : associatedImages) {
                mappedImage.mappingPoints[clickedPoint.x][clickedPoint.y].isSelected = false;
                mappedImage.repaint();
            }

            //Remove the control point as the clicked point
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
        //Only do this if a control point has been selected
        if(clickedPoint != null) {

            //Update the clicked control point with the current mouse position
            mappingPoints[clickedPoint.x][clickedPoint.y].moveInBoundary(mouseEvent.getX(), mouseEvent.getY());
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}

    //Private helper methods

    //Determine if a point is "close enough" to a point by finding the distance between it and the point
    //and comparing it to the size of the point
    private boolean isClose(Point location, ControlPoint controlPoint) {
        return Math.sqrt((location.x - controlPoint.x) * (location.x - controlPoint.x)
                + (location.y - controlPoint.y) * (location.y - controlPoint.y)) <= pointSize/2;
    }

    //Connect the control points in a particular fashion
    private void formGrid() {

        //For every point in the control point list
        for(int row = 0; row < mappingPoints.length; row++) {
            for(int column = 0; column < mappingPoints[row].length; column++) {

                //Calculate values used to get surrounding points that should
                //be considered adjacent.
                /*
                Visual:
                    1    2    3
                         |   /

                    4 -- 5 -- 6

                     /   |
                    7    8    9
                */

                int up = row - 1;
                int down = row + 1;
                int left = column - 1;
                int right = column + 1;

                //Determine if the prior connection values would be in range
                boolean upConnects = up >= 0;
                boolean downConnects = down < mappingPoints.length;
                boolean leftConnects = left >= 0;
                boolean rightConnects = right < mappingPoints[row].length;

                //If a point exists that should be connected to this point, connect them
                if(upConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[up][column]);
                if(rightConnects && upConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[up][right]);
                if(rightConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[row][right]);
                if(downConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[down][column]);
                if(downConnects && leftConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[down][left]);
                if(leftConnects)
                    mappingPoints[row][column].addAdjacentPoint(mappingPoints[row][left]);

            }
        }
    }

    private void makePoints() {
        //Allocate the grid to be the given size + 2 squared;
        //This is done to have invisible control points that exist
        //on the edge of the panel
        mappingPoints = new ControlPoint[gridHeight][gridLength];

        //Calculate the distance between points vertically and horizontally
        int widthInc = imageLength / (gridLength - 1);
        int heightInc = imageHeight / (gridHeight - 1);

        //Set the appropriate x and y values for the points on the grid
        for (int row = 0; row < mappingPoints.length; row++)
            for (int column = 0; column < mappingPoints[row].length; column++) {

                //Determine the x and y value of the control point.
                //If the point is the last one in the column or row,
                //make sure it goes to the very edge of the image
                int rowVal = heightInc * row;
                int columnVal = widthInc * column;
                if (row == heightInc * (gridHeight - 1))
                    rowVal = imageHeight;
                if (column == widthInc * (gridLength - 1))
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

    }


}

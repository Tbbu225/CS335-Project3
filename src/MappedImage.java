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

public class MappedImage extends JPanel implements MouseListener, MouseMotionListener{
    private BufferedImage image;
    private ArrayList<ControlPoint> mappingPoints;
    private int gridSize;
    private int pointSize;
    private int imageLength, imageHeight;
    private ControlPoint clickedPoint;

    public MappedImage(int size, int length, int height, int radius) {
        super();
        setBackground(Color.WHITE);

        image = null;
        gridSize = size;
        pointSize = radius;
        imageLength = length;
        imageHeight = height;

        mappingPoints = new ArrayList<>((gridSize + 2) * (gridSize + 2));

        clickedPoint = null;

        int widthInc = imageLength / (gridSize + 1);
        int heightInc = imageHeight / (gridSize + 1);

        for (int y = 0; y <= imageHeight; y += heightInc)
            for (int x = 0; x <= imageLength; x += widthInc) {

                ControlPoint newPoint = new ControlPoint(x, y, 6);

                int xVal = newPoint.x;
                int yVal = newPoint.y;

                if (x != 0 && x != widthInc * (gridSize + 1)
                        && y != 0 && y != heightInc * (gridSize + 1))
                    newPoint.isVisible = true;

                mappingPoints.add(newPoint);
            }

        formGrid();

        repaint();

        addMouseListener(this);
        addMouseMotionListener(this);

    }

    public MappedImage(MappedImage mappedImage, boolean keepImage) {
        super();
        setBackground(Color.WHITE);

        this.image = null;
        this.gridSize = mappedImage.gridSize;
        this.pointSize = mappedImage.pointSize;
        this.imageLength = mappedImage.imageLength;
        this.imageHeight = mappedImage.imageHeight;
        this.clickedPoint = mappedImage.clickedPoint;

        if(keepImage && mappedImage.image != null) {
            this.image = new BufferedImage(mappedImage.image.getWidth(),
                    mappedImage.image.getHeight(),
                    mappedImage.image.getType());
            this.image.getGraphics().drawImage(mappedImage.image, 0, 0, null);
        }

        this.mappingPoints = new ArrayList<>(mappedImage.mappingPoints.size());

        for (int i = 0; i < mappedImage.mappingPoints.size(); i++) {
            ControlPoint oldPoint = mappedImage.mappingPoints.get(i);
            ControlPoint newPoint = new ControlPoint(oldPoint.x, oldPoint.y, oldPoint.getAdjacenyMaximum());
            this.mappingPoints.add(newPoint);
            if(oldPoint.isVisible)
                newPoint.isVisible = true;
        }
    }

    public void getImage(String imagePath) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            image = null;
            return;
        }

        int height = originalImage.getHeight();
        int width = originalImage.getWidth();

        Image scaledImage = originalImage.getScaledInstance(imageLength, imageHeight, Image.SCALE_SMOOTH);

        image = new BufferedImage(imageLength, imageHeight, originalImage.getType());

        Graphics2D bufferGraphics = image.createGraphics();
        bufferGraphics.drawImage(scaledImage, 0, 0, null);
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.drawImage(image, 0, 0, null);
        for (ControlPoint controlPoint : mappingPoints) {
            if (controlPoint.isVisible) {
                if (controlPoint.isSelected)
                    g2d.setColor(Color.RED);
                else
                    g2d.setColor(Color.BLACK);
                g2d.fillOval(controlPoint.x - (pointSize / 2), controlPoint.y - (pointSize / 2), pointSize, pointSize);
            }
        }

        for (ControlPoint controlPoint : mappingPoints)
            for (ControlPoint adjacentControlPoint : controlPoint.getAdjacentControlPoints()) {
                g2d.setColor(Color.BLACK);
                g2d.drawLine(controlPoint.x, controlPoint.y,
                        adjacentControlPoint.x, adjacentControlPoint.y);
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
            for (ControlPoint controlPoint : mappingPoints)
                if (isClose(mouseEventPoint, controlPoint)) {
                    clickedPoint = controlPoint;
                    clickedPoint.isSelected = true;
                }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if(clickedPoint != null) {
            clickedPoint.isSelected = false;
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
            clickedPoint.x = mouseEvent.getX();
            clickedPoint.y = mouseEvent.getY();
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
        int widthInc = imageLength / (gridSize + 1);
        int heightInc = imageHeight / (gridSize + 1);

        for (ControlPoint controlPoint : mappingPoints)
            for (ControlPoint otherControlPoint : mappingPoints) {
                int distanceX = controlPoint.x - otherControlPoint.x;
                int distanceY = controlPoint.y - otherControlPoint.y;
                if (Math.abs(distanceX) <= widthInc && Math.abs(distanceY) <= heightInc
                        && !(distanceX < 0 && distanceY < 0) && !(distanceX > 0 && distanceY > 0)
                        && controlPoint != otherControlPoint)
                    controlPoint.addAdjacentPoint(otherControlPoint);
            }
    }

}

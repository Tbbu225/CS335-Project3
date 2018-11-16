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
    private int gridLength, gridHeight;
    private ControlPoint clickedPoint;

    public MappedImage(String imagePath, int gridSize, int length, int height, int pointSize) {
        super();
        this.gridSize = gridSize;
        this.pointSize = pointSize;
        gridLength = length;
        gridHeight = height;

        mappingPoints = new ArrayList<>((gridSize + 2) * (gridSize + 2));

        clickedPoint = null;

        getImage(imagePath);

        int widthInc = image.getWidth() / (gridSize + 1);
        int heightInc = image.getHeight() / (gridSize + 1);

        for (int y = 0; y <= image.getHeight(); y += heightInc)
            for (int x = 0; x <= image.getWidth(); x += widthInc) {

                ControlPoint newPoint = new ControlPoint(x, y);

                int xVal = newPoint.x;
                int yVal = newPoint.y;

                if (x != 0 && x != widthInc * (gridSize + 1)
                        && y != 0 && y != heightInc * (gridSize + 1))
                    newPoint.isVisible = true;

                mappingPoints.add(newPoint);
            }

        for (ControlPoint controlPoint : mappingPoints)
            for (ControlPoint otherControlPoint : mappingPoints)
                if (Math.abs(controlPoint.x - otherControlPoint.x) <= widthInc
                        && Math.abs(controlPoint.y - otherControlPoint.y) <= heightInc
                        && controlPoint != otherControlPoint)
                    controlPoint.addAdjacentPoint(otherControlPoint);

        repaint();

        addMouseListener(this);
        addMouseMotionListener(this);

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

        Image scaledImage = originalImage.getScaledInstance(gridLength, gridHeight, Image.SCALE_SMOOTH);

        image = new BufferedImage(gridLength, gridHeight, originalImage.getType());

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
        return new Dimension(image.getWidth(), image.getHeight());
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


}

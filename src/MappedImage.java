import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MappedImage extends JPanel{
    private BufferedImage image;
    private ArrayList<ControlPoint> mappingPoints;
    private int gridSize;
    private int gridLength, gridHeight;

    public MappedImage(String imagePath, int size, int length, int height) {
        super();
        gridSize = size;
        gridLength = length;
        gridHeight = height;

        mappingPoints = new ArrayList<>((gridSize+2)*(gridSize+2));

        getImage(imagePath);

        int widthInc = image.getWidth() / (gridSize+1);
        int heightInc  = image.getHeight() / (gridSize+1);

        for (int y = 0; y <= image.getHeight(); y += heightInc)
            for (int x = 0; x <= image.getWidth(); x += widthInc) {

                ControlPoint newPoint = new ControlPoint(x, y);

                int xVal = newPoint.location.x;
                int yVal = newPoint.location.y;

                if (x != 0 && x != widthInc * (gridSize+1)
                 && y != 0 && y != heightInc * (gridSize+1))
                    newPoint.isVisible = true;

                mappingPoints.add(newPoint);
            }

        for(ControlPoint controlPoint : mappingPoints)
            for(ControlPoint otherControlPoint : mappingPoints)
                if (Math.abs(controlPoint.location.x - otherControlPoint.location.x) <= widthInc
                 && Math.abs(controlPoint.location.y - otherControlPoint.location.y) <= heightInc
                 && controlPoint != otherControlPoint)
                    controlPoint.addAdjacentPoint(otherControlPoint);

        repaint();

    }

    public void getImage(String imagePath) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(new File(imagePath));
        }
        catch (IOException e) {
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
        g2d.drawImage(image,0,0,null);
        for(ControlPoint controlPoint: mappingPoints) {
            if (controlPoint.isVisible) {
                if (controlPoint.isSelected)
                    g2d.setColor(Color.RED);
                else
                    g2d.setColor(Color.BLACK);
                g2d.fillOval(controlPoint.location.x-10, controlPoint.location.y-10, 20, 20);
            }
        }

        for(ControlPoint controlPoint : mappingPoints)
            for(ControlPoint adjacentControlPoint : controlPoint.getAdjacentControlPoints()) {
                g2d.setColor(Color.BLACK);
                g2d.drawLine(controlPoint.location.x, controlPoint.location.y,
                        adjacentControlPoint.location.x, adjacentControlPoint.location.y);
            }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }
}

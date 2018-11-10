import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ControlPoint {

    public static void main(String[] args) {

        ControlPointView v = new ControlPointView();
        v.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });

    }
}
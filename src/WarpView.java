import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/* Warpview
 *
 * This Class acts as a GUI for allowing a user to create a warp of two images
 * using the MappedImage class.
 *
 * Authors:
 * Tyler Burkett
 * John Dickens
 */
public class WarpView extends JFrame {

    //Jpanel to hold buttons, and images
    private JPanel button_panel, image_panel;

    //JFrame for pop up menu on settings, then one for preview morph
    private JFrame settings_panel, preview_frame, morph_frame;

    //views of menu
    private JMenuBar main_menu;
    private JMenuItem morph_save, settings_menu_item, quit_menu_item;

    //buttons for settings menu, then buttons for main control
    private JButton settings_okay, settings_cancel, left_image_button, right_image_button, preview_morph_button, morph_button;

    //labels and inputs for frames and seconds and grid resolution
    private JLabel frames_sec_label, seconds_label, grid_height_label, grid_length_label, brightness_label_left, brightness_label_right, frame_length_label, frame_height_label;
    private TextField frames_sec_input, seconds_input, frame_length_input, frame_height_input;
    private JSlider grid_height_input, grid_length_input, brightness_input_left, brightness_input_right;
    private int frames_sec, seconds;

    //filechooser to pick files
    private JFileChooser file_choose;

    //flag for which image to load
    private Boolean load_left_flag;

    private Container c;

    //mapped image objects
    private MappedImage orig_img, dest_img, morphing_img;

    //Timer to control time of morph
    private Timer preview_timer, morph_timer, settings_timer;

    private int timer_counter;
    //array to hold increment amounts
    private double[][] inc_x_array, inc_y_array;
    private double[][] r_inc, g_inc, b_inc, a_inc;

    private ControlPoint[][] orig_points, end_points, morph_points;

    private ArrayList<Triangle> src_triangles, dest_triangles;

    private ArrayList<MappedImage> frames;

    //constructor
    public WarpView()
    {
        //set initial values
        frames_sec = 10;
        seconds = 2;

        //constructor to load mapped image
        orig_img = new MappedImage( 10, 450,600, 10);
        dest_img = new MappedImage( 10, 450, 600, 10);
        orig_img.setAssociatedImage(dest_img);

        src_triangles = new ArrayList<>();
        dest_triangles = new ArrayList<>();
        frames = new ArrayList<>();

        inc_x_array = null;
        inc_y_array = null;
        r_inc = null;
        g_inc = null;
        b_inc = null;

        initialize_GUI();

    }

    //method where all the GUI initialization actually happens
    public void initialize_GUI()
    {

        //setting up main menu
        main_menu = new JMenuBar();

        morph_save = new JMenuItem("Morph Save");
        main_menu.add(morph_save);  //Reserved for when we need it
        morph_save.addActionListener(new morph_save());

        settings_menu_item = new JMenuItem("Settings");
        main_menu.add(settings_menu_item);
        settings_menu_item.addActionListener(new settings_menu());

        quit_menu_item = new JMenuItem("Quit");
        main_menu.add(quit_menu_item);
        quit_menu_item.addActionListener(new quit());

        //setting up controls at botton
        button_panel = new JPanel();

        left_image_button = new JButton("Load Left Image");
        button_panel.add(left_image_button);
        left_image_button.addActionListener(new left_image());

        right_image_button = new JButton("Load Right Image");
        button_panel.add(right_image_button);
        right_image_button.addActionListener(new right_image());

        preview_morph_button = new JButton("Preview Morph");
        button_panel.add(preview_morph_button);
        preview_morph_button.addActionListener(new preview_morph());

        morph_button = new JButton("Morph");
        button_panel.add(morph_button);
        morph_button.addActionListener(new image_morph());

        //image panel- holds images
        image_panel = new JPanel();
        image_panel.setLayout(new FlowLayout());

        image_panel.add(orig_img);
        image_panel.add(dest_img);

        //initializing container
        c = getContentPane();

        c.add(main_menu, BorderLayout.NORTH);
        c.add(image_panel, BorderLayout.CENTER);
        c.add(button_panel, BorderLayout.SOUTH);

        setTitle("Control Point");
        setSize(1100, 800);
        setVisible(true);
    }

    //method that loads image
    public void load_image()
    {
        inc_x_array = null;
        inc_y_array = null;

        file_choose = new JFileChooser();
        int file_return = file_choose.showOpenDialog(file_choose);
        if(file_return == JFileChooser.APPROVE_OPTION)
        {
            File file = file_choose.getSelectedFile();

            //loads left image
            if(load_left_flag) {
                orig_img.getImage(file.getAbsolutePath());
                orig_img.setVisible(true);
                repaint();
            }
            else//loads right image
            {
                dest_img.getImage((file.getAbsolutePath()));
                dest_img.setVisible(true);
                repaint();
            }
        }
    }

    class morph_save implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            //frames
            //save control point
            //JFileChooser chooser = new JFileChooser();
            
            for(int i = 0; i < frames.size(); i++)
            {
                File output = new File("output"+i);
                try {
                    ImageIO.write(frames.get(i).getBufferedImage(), "jpg", output);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    //pop up menu for settings
    class settings_menu implements ActionListener{
        public void actionPerformed(ActionEvent e){

            //Jframe to hold it
            settings_panel = new JFrame("  Settings");
            settings_panel.setLayout(new GridLayout(9,2,30,40));

            //labels for button
            frames_sec_label = new JLabel("  Frames per Second");
            settings_panel.add(frames_sec_label);

            //text field to hold how many frames per second
            frames_sec_input = new TextField(10);
            settings_panel.add(frames_sec_input);
            frames_sec_input.addActionListener(this);

            seconds_label = new JLabel("  Seconds per morph");
            settings_panel.add(seconds_label);

            //text field to hold how many seconds per morph
            seconds_input = new TextField(10);
            settings_panel.add(seconds_input);
            seconds_input.addActionListener(this);

            frame_length_label = new JLabel("  Frame Length:" );
            settings_panel.add(frame_length_label);

            frame_length_input = new TextField(10);
            settings_panel.add(frame_length_input);
            frame_length_input.addActionListener(this);

            frame_height_label = new JLabel("  Frame Height:" );
            settings_panel.add(frame_height_label);

            frame_height_input = new TextField(10);
            settings_panel.add(frame_height_input);
            frame_height_input.addActionListener(this);

            grid_height_label = new JLabel( "  Grid Height:  ");
            settings_panel.add(grid_height_label);

            grid_height_input = new JSlider();
            grid_height_input.setMinimum(1);
            grid_height_input.setMaximum(20);
            grid_height_input.setValue(10);
            settings_panel.add(grid_height_input);


            grid_length_label = new JLabel( "  Grid Length:  ");
            settings_panel.add(grid_length_label);

            grid_length_input = new JSlider();
            grid_length_input.setMinimum(1);
            grid_length_input.setMaximum(20);
            grid_length_input.setValue(10);
            settings_panel.add(grid_length_input);

            brightness_label_left = new JLabel("  Brightness(left image):  ");
            settings_panel.add(brightness_label_left);
            brightness_input_left = new JSlider();
            brightness_input_left.setMinimum(1); //values will be divided by 10 when passed into mapped images
            brightness_input_left.setMaximum(20);
            brightness_input_left.setValue(10);

            settings_panel.add(brightness_input_left);

            brightness_label_right = new JLabel("  Brightness(right image):  ");
            settings_panel.add(brightness_label_right);
            brightness_input_right = new JSlider();
            brightness_input_right.setMinimum(1); //values will be divided by 10 when passed into mapped images
            brightness_input_right.setMaximum(20);
            brightness_input_right.setValue(10);

            settings_panel.add(brightness_input_right);


            settings_timer = new Timer(10, actionEvent -> {
                brightness_label_left.setText("  Brightness(left image):  " + brightness_input_left.getValue());
                brightness_label_right.setText("  Brightness(right image):  " + brightness_input_right.getValue());
                grid_height_label.setText("  Grid Height:  " + grid_height_input.getValue());
                grid_length_label.setText("  Grid Length:  "+ grid_length_input.getValue());
            }
            );

            settings_timer.start();

            //confirms text field entries
            settings_okay = new JButton("OK");
            settings_panel.add(settings_okay);
            settings_okay.addActionListener((new set_ok()));

            //cancels
            settings_cancel = new JButton("Cancel");
            settings_panel.add(settings_cancel);
            settings_cancel.addActionListener((new set_cancel()));

            //sets the pop up window visible
            settings_panel.setSize(500, 500);
            settings_panel.setVisible(true);
        }
    }

    //quits program
    class quit implements ActionListener{
        public void actionPerformed(ActionEvent e){
            System.exit(0);
        }
    }

    //buttons operated by settings pop up menu
    //this one sets the values of seconds and frames per second
    class set_ok implements ActionListener{
        public void actionPerformed(ActionEvent e){

            //sets grid size
            orig_img.setVisibleGridDimensions(orig_img.getGridLength()-2,grid_height_input.getValue());
            dest_img.setVisibleGridDimensions(dest_img.getGridLength()-2, grid_height_input.getValue());

            orig_img.setVisibleGridDimensions(grid_length_input.getValue(), orig_img.getGridHeight()-2);
            dest_img.setVisibleGridDimensions(grid_length_input.getValue(), orig_img.getGridHeight()-2);

            //changes brightness with error checking
            if(orig_img.getBufferedImage() != null)
                orig_img.brighten(((float)brightness_input_left.getValue()) / 10);
            if(dest_img.getBufferedImage() != null)
                dest_img.brighten(((float)brightness_input_right.getValue()) / 10);

            try {
                orig_img.setFrameDimensions(Integer.parseInt(frame_length_input.getText()), Integer.parseInt(frame_height_input.getText()));

            } catch(Exception x){}

            try {
                dest_img.setFrameDimensions(Integer.parseInt(frame_length_input.getText()), Integer.parseInt(frame_height_input.getText()));
            } catch(Exception x){}

            //need to add something in here to make sure the two have default values
            try {
            seconds = Integer.parseInt(seconds_input.getText());
            if(seconds < 0) throw new IllegalArgumentException();
            } catch (Exception x) {
                seconds = 2;
            }

            try {
                frames_sec = Integer.parseInt(frames_sec_input.getText());
                if(frames_sec < 0) throw new IllegalArgumentException();
            } catch (Exception x) {
                frames_sec = 10;
            }

            settings_panel.dispatchEvent(new WindowEvent(settings_panel, WindowEvent.WINDOW_CLOSING));
        }
    }

    //closes settings window, does not confirm
    class set_cancel implements ActionListener{
        public void actionPerformed(ActionEvent e){
            settings_panel.dispatchEvent(new WindowEvent(settings_panel, WindowEvent.WINDOW_CLOSING));
        }
    }

    //buttons for main controls
    //loads left image
    class left_image implements ActionListener{
        public void actionPerformed(ActionEvent e){
            load_left_flag = true;
            load_image();
        }
    }

    //loads right image
    class right_image implements ActionListener{
        public void actionPerformed(ActionEvent e){
            load_left_flag = false;
            load_image();        }
    }

    //previews morph
    class preview_morph implements ActionListener{
        public void actionPerformed(ActionEvent e){

            morphing_img = new MappedImage(orig_img,true);

            //finds distances in between points
            find_increments();

            preview_frame = new JFrame("Preview Morph");

            timer_counter = 0;

            //timer actives every frame
            preview_timer = new Timer((1000*seconds)/frames_sec, actionEvent -> {
                preview_old_to_new();
                morphing_img.repaint();

                //counter so that preview_timer will stop after going through all the frames
                timer_counter++;
                if(timer_counter == (seconds*frames_sec)-1) {
                    preview_timer.stop();
                    finalize_morph();
                }
            });

            preview_timer.start();

            preview_frame.add(morphing_img);

            preview_frame.setSize(orig_img.getWidth()+40,orig_img.getHeight()+40);
            preview_frame.setVisible(true);
        }
    }
    //actually morphs image
    class image_morph implements ActionListener{
        public void actionPerformed(ActionEvent e){
            if(orig_img.getBufferedImage() == null || dest_img.getBufferedImage() == null)
            {
                return;
            }
            morph_frame = new JFrame("Morph");

            make_betweens();

            timer_counter = 0;

            //timer actives every frame
            morph_timer = new Timer((1000*seconds)/frames_sec, actionEvent -> {

                morph(dest_img);
                //morph_image();

                //blend
                //morph triangles

                //counter so that preview_timer will stop after going through all the frames
                timer_counter++;
                if(timer_counter == (seconds*frames_sec)-1) {
                    morph_timer.stop();
//                    finalize_morph();
                }
            });

            morph_timer.start();

//            morph_frame.add(morphing_img);

            morph_frame.setSize(orig_img.getHeight()+40,orig_img.getWidth()+40);
            morph_frame.setVisible(true);
        }
    }

    public void morph(MappedImage dest_img)
    {
        for(int i = 1; i < frames.size()-1; i++)
        {
               MorphTools.warpTriangle(orig_img.getBufferedImage(), dest_img.getBufferedImage(), src_triangles.get(i), dest_triangles.get(i), null, null);

        }

    }

    public void preview_old_to_new()
    {
        //morph points to feed back into morph)ima
        morph_points = morphing_img.getMappingPoints();

        for(int i = 0; i<morphing_img.getGridLength(); i++)
        {
            for(int j = 0; j < morphing_img.getGridHeight(); j++)
            {
                morph_points[i][j].setLocation(orig_points[i][j].getX() +timer_counter*inc_x_array[i][j],orig_points[i][j].getY()+timer_counter*inc_y_array[i][j]);
            }
        }
    }

    public void finalize_morph()
    {
        for(int i = 0; i<morphing_img.getGridLength(); i++)
        {
            for(int j = 0; j < morphing_img.getGridHeight(); j++)
            {
                morph_points[i][j].setLocation(end_points[i][j].getX(),end_points[i][j].getY());
            }
        }
        morphing_img.repaint();
    }

    public void find_increments()
    {
        //gets grid of increments
        int num_increments = seconds*frames_sec;



        orig_points = orig_img.getMappingPoints();
        end_points = dest_img.getMappingPoints();

        inc_x_array = new double[morphing_img.getGridLength()][morphing_img.getGridHeight()];
        inc_y_array = new double[morphing_img.getGridLength()][morphing_img.getGridHeight()];

        //for loop to get increments
        for(int i = 0; i<morphing_img.getGridLength(); i++)
        {
            for(int j = 0; j < morphing_img.getGridHeight(); j++)
            {
                inc_x_array[i][j] =  (end_points[i][j].getX() - orig_points[i][j].getX())/num_increments;
                inc_y_array[i][j] =  (end_points[i][j].getY() - orig_points[i][j].getY())/num_increments;
            }
        }
    }

    public void apply_increment(MappedImage dest, int step)
    {
        //morph points to feed back into morph)ima
        ControlPoint[][] dest_points = dest.getMappingPoints();

        for(int i = 0; i<dest_points.length; i++)
        {
            for(int j = 0; j < dest_points[i].length; j++)
            {
                dest_points[i][j].setLocation(orig_points[i][j].getX() +step * inc_x_array[i][j],orig_points[i][j].getY()+step * inc_y_array[i][j]);
            }
        }

    }

    public void make_triangles(MappedImage dest)
    {
        ControlPoint[][] src_tri = orig_img.getTrianglePoints();
        ControlPoint[][] dest_tri = dest.getTrianglePoints();

        src_triangles.clear();
        dest_triangles.clear();

        //makes a whole bunch of triangle objects from source
        for(int i = 0; i < src_tri.length; i++)
        {
            src_triangles.add(new Triangle(src_tri[i][1].x,src_tri[i][1].y,src_tri[i][2].x,src_tri[i][2].y,src_tri[i][3].x,src_tri[i][3].y ));
            dest_triangles.add(new Triangle(dest_tri[i][1].x,dest_tri[i][1].y,dest_tri[i][2].x,dest_tri[i][2].y,dest_tri[i][3].x,dest_tri[i][3].y ));
        }

    }

    public void blend(MappedImage dest_img, int step) {

        int width = orig_img.getBufferedImage().getWidth();
        int height = orig_img.getBufferedImage().getHeight();

        if(r_inc == null || g_inc == null || b_inc == null)
        {
            r_inc = new double[height][width];
            g_inc = new double[height][width];
            b_inc = new double[height][width];
            a_inc = new double[height][width];

            for (int i = 0; i < orig_img.getBufferedImage().getHeight(); i++)
            {
                for(int j = 0; j < orig_img.getBufferedImage().getWidth(); j++)
                {
                    Color origRGB = new Color(orig_img.getBufferedImage().getRGB(i,j));
                    Color destRGB = new Color(dest_img.getBufferedImage().getRGB(i,j));

                    int rdiff = destRGB.getRed() - origRGB.getRed();
                    int gdiff = destRGB.getGreen() - origRGB.getGreen();
                    int bdiff = destRGB.getBlue() - origRGB.getBlue();
                    int adiff = destRGB.getAlpha() - origRGB.getAlpha();

                    r_inc[i][j] = ((double) rdiff) / (frames_sec * seconds);
                    g_inc[i][j] = ((double) gdiff) / (frames_sec * seconds);
                    b_inc[i][j] = ((double) bdiff) / (frames_sec * seconds);
                    a_inc[i][j] = ((double) adiff) / (frames_sec * seconds);
                }
            }
        }

        BufferedImage temp = dest_img.getBufferedImage();

        for (int j = 0; j < temp.getHeight(); j++)
        {
            for (int k = 0; k < temp.getWidth(); k++)
            {
                Color pixelColor = new Color(temp.getRGB(j,k));

                int rnew = (int) Math.floor(pixelColor.getRed() + r_inc[j][k] * step + 0.5);
                int gnew = (int) Math.floor(pixelColor.getGreen() + g_inc[j][k] * step + 0.5);
                int bnew = (int) Math.floor(pixelColor.getBlue() + (int) b_inc[j][k] * step + 0.5);
                int anew = (int) Math.floor(pixelColor.getAlpha() + (int) a_inc[j][k] * step + 0.5);

                Color newPixelColor = new Color(rnew, gnew, bnew, anew);

                temp.setRGB(j, k, newPixelColor.getRGB());
            }
        }


    }

    public void make_betweens() {

        //finds distances in between points
        if(inc_x_array == null || inc_y_array == null)
            find_increments();

        frames.add(orig_img);

        for (int i = 1; i < frames_sec * seconds - 2; i++ ) {
            MappedImage frame = new MappedImage(orig_img, false);
            frames.add(frame);
        }

        frames.add(dest_img);

        for(int i = 1; i < frames.size() - 1; i++) {
            MappedImage frame = frames.get(i);
            apply_increment(frame, i);
            make_triangles(frame);
            morph(frame);
            blend(frame, i);
        }

    }

}
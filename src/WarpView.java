import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private JMenuItem file_menu_item, settings_menu_item, quit_menu_item;

    //buttons for settings menu, then buttons for main control
    private JButton settings_okay, settings_cancel, left_image_button, right_image_button, preview_morph_button, morph_button;

    //labels and inputs for frames and seconds and grid resolution
    private JLabel frames_sec_label, seconds_label, grid_height_label, grid_length_label, brightness_label;
    private TextField frames_sec_input, seconds_input;
    private JSlider grid_height_input, grid_length_input, brightness_input;
    private int frames_sec, seconds, grid_height, grid_length, brightness;

    //filechooser to pick files
    private JFileChooser file_choose;

    //flag for which image to load
    private Boolean load_left_flag;

    private Container c;

    //mapped image objects
    private MappedImage orig_img, dest_img, morphing_img;

    //Timer to control time of morph
    private Timer preview_timer, morph_timer;

    private int timer_counter;
    //array to hold increment amounts
    private double[][] inc_x_array, inc_y_array;

    private ControlPoint[][] orig_points, end_points, morph_points;

    private ArrayList<Triangle> src_triangles, dest_triangles;

    private ArrayList<MappedImage> frames;

    private BufferedImage temp_buff_img;

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

        initialize_GUI();

    }

    //method where all the GUI initialization actually happens
    public void initialize_GUI()
    {

        //setting up main menu
        main_menu = new JMenuBar();

        file_menu_item = new JMenuItem("File");
//        main_menu.add(file_menu_item);  //Reserved for when we need it
        file_menu_item.addActionListener(new file_menu());

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
        image_panel.setLayout(new GridLayout(1,2,10,10));

        image_panel.add(orig_img);
        image_panel.add(dest_img);

        //initializing container
        c = getContentPane();

        c.add(main_menu, BorderLayout.NORTH);
        c.add(image_panel, BorderLayout.CENTER);
        c.add(button_panel, BorderLayout.SOUTH);

        setTitle("Control Point");
        setSize(900, 700);
        setVisible(true);
    }

    //method that loads image
    public void load_image()
    {

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


    class file_menu implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //Reserved for saving/loading morphs if/when we reach that point in the project.
        }
    }

    //pop up menu for settings
    class settings_menu implements ActionListener{
        public void actionPerformed(ActionEvent e){

            //Jframe to hold it
            settings_panel = new JFrame("  Settings");
            settings_panel.setLayout(new GridLayout(7,2,30,40));

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

            grid_height_label = new JLabel( "  Grid Height");
            settings_panel.add(grid_height_label);

            grid_height_input = new JSlider();
            grid_height_input.setMinimum(1);
            grid_height_input.setMaximum(20);
            grid_height_input.setValue(10);
            grid_height_input.setPaintTicks(true);
            settings_panel.add(grid_height_input);


            grid_length_label = new JLabel( "  Grid Length");
            settings_panel.add(grid_length_label);

            grid_length_input = new JSlider();
            grid_length_input.setMinimum(1);
            grid_length_input.setMaximum(20);
            grid_length_input.setValue(10);
            grid_length_input.setPaintTicks(true);

            settings_panel.add(grid_length_input);


/*
    private JLabel frames_sec_label, seconds_label, grid_height_label, grid_length_label, brightness_label;
    private TextField frames_sec_input, seconds_input;
    private JSlider grid_height_input, grid_length_input, brightness_input;
    private int frames_sec, seconds, grid_height, grid_length, brightness;

            grid_height_input = new TextField(10);
            settings_panel.add(grid_height_input);
            grid_height_input.addActionListener(this);
*/

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

                            orig_img.setVisibleGridDimensions(orig_img.getGridLength()-2,grid_height_input.getValue());
                            dest_img.setVisibleGridDimensions(dest_img.getGridLength()-2, grid_height_input.getValue());

                            orig_img.setVisibleGridDimensions(grid_length_input.getValue(), orig_img.getGridHeight()-2);
                            dest_img.setVisibleGridDimensions(grid_length_input.getValue(), orig_img.getGridHeight()-2);


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
            morph_frame = new JFrame("Morph");

            //make_triangles(); // This creates two arrays of triangles, one for source image and destination image
            find_increments();//finds increments between the points

            timer_counter = 0;

            //timer actives every frame
            morph_timer = new Timer((1000*seconds)/frames_sec, actionEvent -> {

                //morph_image();

                //              blend
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

    public void morph_image(MappedImage src, MappedImage dest)
    {

        MorphTools morphy = new MorphTools();

        for(int i = 0; i<morphing_img.getGridLength()-1; i++)
        {
               morphy.warpTriangle(src.getBufferedImage(), dest.getBufferedImage(), src_triangles.get(i), dest_triangles.get(i), null, null);

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
                morphing_img.setMappingPoints(morph_points);
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
                morphing_img.setMappingPoints(morph_points);
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

    public void apply_increment(MappedImage src, MappedImage dest, int step)
    {
        //morph points to feed back into morph)ima
        ControlPoint[][] src_points = src.getMappingPoints();
        ControlPoint[][] dest_points = dest.getMappingPoints();

        for(int i = 0; i<src_points.length; i++)
        {
            for(int j = 0; j < src_points[i].length; j++)
            {
                dest_points[i][j].setLocation(src_points[i][j].getX() +step * inc_x_array[i][j],src_points[i][j].getY()+step * inc_y_array[i][j]);
                dest.setMappingPoints(dest_points);
            }
        }

    }

    public void make_triangles(MappedImage src, MappedImage dest)
    {
        ControlPoint[][] src_tri = src.getTrianglePoints();
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

    public void blend(int step) {

    }

    public void make_betweens() {
        MappedImage frame = new MappedImage(orig_img,true);

        //finds distances in between points
        if(inc_x_array == null || inc_y_array == null)
            find_increments();




    }

}
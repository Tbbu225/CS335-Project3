//package PACKAGE_NAME;
//Keeping on working

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    //labels and inputs for frames and seconds
    private JLabel frames_sec_label, seconds_label;
    private TextField frames_sec_input, seconds_input;
    private int frames_sec, seconds;

    //filechooser to pick files
    private JFileChooser file_choose;

    //sets up images
    private BufferedImage left_image, right_image, temp_image;
    private JLabel left_image_label, right_image_label;

    private Boolean load_left_flag;

    private Container c;

    //constructor
    public WarpView()
    {
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

        //taking care of image labels
        left_image_label = new JLabel();
        right_image_label = new JLabel();
        left_image_label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        right_image_label.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        image_panel = new JPanel();
        image_panel.setLayout(new GridLayout(1,2));

        image_panel.add(left_image_label);
        image_panel.add(right_image_label);

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

            if(load_left_flag == true) {
                try {
                    left_image = ImageIO.read(file);
                } catch (IOException e) {}

            }
            else
            {
                try {
                    right_image = ImageIO.read(file);
                } catch (IOException e) {}
            }
        }
    }




    class file_menu implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //Reserved for loading morphs when we reach that point in the project.
        }
    }

    //pop up menu for settings
    class settings_menu implements ActionListener{
        public void actionPerformed(ActionEvent e){

            //Jframe to hold it
            settings_panel = new JFrame("Settings");
            settings_panel.setLayout(new GridLayout(3,2,40,40));

            //labels for button
            frames_sec_label = new JLabel("Frames per Second");
            settings_panel.add(frames_sec_label);

            //text field to hold how many frames per second
            frames_sec_input = new TextField(10);
            settings_panel.add(frames_sec_input);
            frames_sec_input.addActionListener(this);

            seconds_label = new JLabel("Seconds per morph");
            settings_panel.add(seconds_label);

            //text field to hold how many seconds per morph
            seconds_input = new TextField(10);
            settings_panel.add(seconds_input);
            seconds_input.addActionListener(this);

            //confirms text field entries
            settings_okay = new JButton("OK");
            settings_panel.add(settings_okay);
            settings_okay.addActionListener((new set_ok()));

            //cancels
            settings_cancel = new JButton("Cancel");
            settings_panel.add(settings_cancel);
            settings_cancel.addActionListener((new set_cancel()));

            //sets the pop up window visible
            settings_panel.setSize(400, 300);
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

            //need to add something in here to make sure the two have default values
            try {
            seconds = Integer.parseInt(seconds_input.getText());
            } catch (Exception x) {
                x.printStackTrace();
            }

            try {
            frames_sec = Integer.parseInt(frames_sec_input.getText());
            } catch (Exception x) {
                x.printStackTrace();
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

            preview_frame = new JFrame("Preview Morph");

            //add call for preview here

            preview_frame.setSize(600,700);
            preview_frame.setVisible(true);
        }
    }
    //actually morphs image
    class image_morph implements ActionListener{
        public void actionPerformed(ActionEvent e){
            morph_frame = new JFrame("Morph");

            //add call for morph here

            morph_frame.setSize(600,700);
            morph_frame.setVisible(true);
        }
    }
}
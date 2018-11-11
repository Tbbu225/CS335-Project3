//package PACKAGE_NAME;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

public class WarpView extends JFrame {

    //Jpanels to hold gui
    private JPanel button_panel;

    //JFrame for pop up menu on settings
    private JFrame settings_panel;

    //views of menu
    private JMenuBar main_menu;
    private JMenuItem file_menu_item, settings_menu_item, quit_menu_item;

    //buttons for settings menu
    private JButton settings_okay, settings_cancel;

    private JLabel frames_sec_label, seconds_label;

    private TextField frames_sec_input, seconds_input;

    private int frames_sec, seconds;

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
        main_menu.add(file_menu_item);
        file_menu_item.addActionListener(new file_menu());

        settings_menu_item = new JMenuItem("Settings");
        main_menu.add(settings_menu_item);
        settings_menu_item.addActionListener(new settings_menu());

        quit_menu_item = new JMenuItem("Quit");
        main_menu.add(quit_menu_item);
        quit_menu_item.addActionListener(new quit());

        //setting up controls at botton
        button_panel = new JPanel();

        //initializing container
        Container c = getContentPane();

        c.add(main_menu, BorderLayout.NORTH);
        c.add(button_panel, BorderLayout.SOUTH);


        setTitle("Control Point");
        setSize(900, 700);
        setVisible(true);

    }

    class file_menu implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //Action goes here. I don't even know what to put in the file menu.
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
            seconds = Integer.parseInt(seconds_input.getText());
            frames_sec = Integer.parseInt(frames_sec_input.getText());
            settings_panel.dispatchEvent(new WindowEvent(settings_panel, WindowEvent.WINDOW_CLOSING));
        }
    }

    //closes settings window, does not confirm
    class set_cancel implements ActionListener{
        public void actionPerformed(ActionEvent e){
            settings_panel.dispatchEvent(new WindowEvent(settings_panel, WindowEvent.WINDOW_CLOSING));
        }
    }
}
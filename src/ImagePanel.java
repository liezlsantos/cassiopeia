import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * This class was used to set a particular image file as the background of the JFrame. 
 */
@SuppressWarnings("serial")
public class ImagePanel extends JPanel{
    
	private Image image;
   
    public ImagePanel(Image image){
        this.image = image;
        Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }

    public void paintComponent(Graphics g){
        g.drawImage(image, 0, 0, null);
    }
}
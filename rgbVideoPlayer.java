import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import javax.sound.sampled.*;

public class rgbVideoPlayer
{  
    public static void main(String[] args) 
    {
        String rgbFileName = args[0];
        String wavFileName = args[1];

        int frameWidth = 240;
        int frameHeight = 180;
        int numFrames = 150;

        BufferedImage img = new BufferedImage( frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);

        ImageIcon icon = new ImageIcon( img ); // wrap the image 
        JLabel label = new JLabel( icon );
        JPanel outputPanel = new JPanel ();
        outputPanel.add( label );

        JFrame outputFrame = new JFrame("Output image");
        
        outputFrame.getContentPane().add( outputPanel );
        outputFrame.pack();
        outputFrame.setVisible(true);
        outputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try 
        {
            File rgbFile = new File(rgbFileName);
            InputStream is = new FileInputStream(rgbFile);

            long rgbFileLength = rgbFile.length();
            byte[] rgbBytes = new byte[(int)rgbFileLength];

            int offset = 0;
            int numRead = 0;
            while (offset < rgbBytes.length && (numRead=is.read(rgbBytes, offset, rgbBytes.length-offset)) >= 0) {
                offset += numRead;
            }

            File wavFile = new File( wavFileName );
            Clip clip = AudioSystem.getClip();
            clip.open( AudioSystem.getAudioInputStream( new File(wavFileName) ) );
            clip.start();

            int f = 0;
            int ind = 0;
            while ( clip.isRunning() && f < 150 )
            {
                if ( clip.getMicrosecondPosition() > f * 100 * 1000 )
                {
                    for ( int y = 0; y < frameHeight; ++y )
                    {
                        for ( int x = 0; x < frameWidth; ++x )
                        {
                            byte a = 0;
                            byte r = rgbBytes[ind];
                            byte g = rgbBytes[ind+frameHeight*frameWidth];
                            byte b = rgbBytes[ind+frameHeight*frameWidth*2]; 
                            
                            int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                            img.setRGB(x,y,pix);
                            ind++;
                        }
                    }

                    ind += frameHeight*frameWidth*2;

                    ++f;

                    label.repaint();
                }
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        } 
    }
}

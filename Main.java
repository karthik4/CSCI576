package com.csci576.mmdb;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        DataStore ds = new DataStoreImpl();

        try {
            System.out.println( "Generating motion descriptor" );
            int[] motionDescriptor = ds.generateMotionDescriptor
                    (args[0]);
            System.out.println( "Generating audio intensity descriptor" );
            int[] audioDescriptor = ds.generateAudioDescriptor
                    (args[1]);
            System.out.println( "Generating color histogram descriptor" );
            int[] colorHistogramDescriptor = ds
                    .generateColorHistogramDescriptor(args[0]);

            if (args.length > 2 && args[2].equals("-t")) {
                System.out.println( "Writing motion descriptor to " + args[0] + ".desc1" );
                writeDescriptorData(motionDescriptor, args[0] + ".desc1");
                System.out.println( "Writing audio intensity descriptor to " + args[1] + ".desc2" );
                writeDescriptorData(audioDescriptor, args[1] + ".desc2");
                System.out.println( "Writing color histogram descriptor to " + args[0] + ".desc3" );
                writeDescriptorData(colorHistogramDescriptor, args[0] + ".desc3");
            }

            displayVideoWithDescriptors(args[0], motionDescriptor,
                    colorHistogramDescriptor, audioDescriptor);

            final String videoNameMotionDescriptorBased =
                    getBestMatchingVideoBasedOnDescriptor(motionDescriptor, "desc1");

            final String videoNameAudioDescriptorBased =
                    getBestMatchingVideoBasedOnDescriptor(audioDescriptor,
                            "desc2");

            final String videoNameColorDescriptorBased =
                    getBestMatchingVideoBasedOnDescriptor
                            (colorHistogramDescriptor, "desc3");

            System.out.println(videoNameMotionDescriptorBased);
            System.out.println(videoNameAudioDescriptorBased);
            System.out.println(videoNameColorDescriptorBased);

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private static String getBestMatchingVideoBasedOnDescriptor(final int[] descriptor, final String desc1) {
        File[] files = new File(".").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(desc1);
            }
        });

        float closestDelta = Integer.MAX_VALUE;
        String closestMatchingFile = "";
        for (File file : files) {
            float delta = computeDistance(descriptor, loadDescriptorData(file
                    .getPath()));
            if (delta < closestDelta) {
                closestDelta = delta;
                closestMatchingFile = file.getName();
            }
        }

        return closestMatchingFile;
    }

    private static void displayVideoWithDescriptors(final String arg, final
    int[] motionDescriptor, final int[] colorHistogramDescriptor, final int[]
                                                            audioDescriptor) {
        final File videoFile = new File(arg);
        int[][] frame = null;

        try {
            InputStream is = new FileInputStream(videoFile);

            long frameLen = videoFile.length();
            byte[] bytes = new byte[(int) frameLen];

            int offset = 0, numRead;

            while (offset < frameLen && (numRead = is.read(bytes, offset,
                    (int) frameLen - offset)) >= 0) {
                offset += numRead;
            }

            int index = 0;
            frame = new int[180][240];

            for (int i = 0; i < 180; ++i) {
                for (int j = 0; j < 240; ++j) {
                    byte r = bytes[index];
                    byte g = bytes[240 * 180 + index];
                    byte b = bytes[240 * 180 * 2 + index];

                    int pixel = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff)
                            << 8) | ((b & 0xff));
                    frame[i][j] = pixel;
                    index++;
                }
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }

        BufferedImage motionDescriptorImage = new BufferedImage
                (2 * motionDescriptor.length, 50,
                BufferedImage.TYPE_INT_RGB);
        BufferedImage colorHistogramDescriptorImage = new BufferedImage
                (2 * colorHistogramDescriptor.length,
                50, BufferedImage.TYPE_INT_RGB);
        BufferedImage audioDescriptorImage = new BufferedImage
                (2 * audioDescriptor.length,
                50, BufferedImage.TYPE_INT_RGB);
        BufferedImage firstFrame = new BufferedImage(240, 180, BufferedImage.TYPE_INT_RGB);

        int range = Integer.MIN_VALUE;
        for (int i = 0; i < motionDescriptor.length; ++i) {
            if (motionDescriptor[i] > range) {
                range = motionDescriptor[i];
            }
        }

        double qInterval = range / 255.0;

        for (int i = 0; i < 50; ++i) {
            for (int j = 0; j < 2 * motionDescriptor.length; ++j) {
                int mdj = (int) Math.round(motionDescriptor[j / 2] / qInterval);
                int motion_pixel = 0xff000000 |
                        ((mdj & 0xff) << 16) |
                        ((mdj & 0xff) << 8) |
                        ((mdj & 0xff));
                motionDescriptorImage.setRGB(j, i, motion_pixel);
            }
            for (int j = 0; j < 2 * colorHistogramDescriptor.length; ++j) {
                int color_pixel = 0xff000000 |
                        ((colorHistogramDescriptor[j / 2] & 0xff) << 16) |
                        ((colorHistogramDescriptor[j / 2] & 0xff) << 8) |
                        ((colorHistogramDescriptor[j / 2] & 0xff));
                colorHistogramDescriptorImage.setRGB(j, i, color_pixel);
            }
            for (int j = 0; j < 2 * audioDescriptor.length; ++j) {
                int audio_pixel = 0xff000000 |
                        ((audioDescriptor[j / 2] & 0xff) << 16) |
                        ((audioDescriptor[j / 2] & 0xff) << 8) |
                        ((audioDescriptor[j / 2] & 0xff));
                audioDescriptorImage.setRGB(j, i, audio_pixel);
            }
        }

        if (frame != null) {
            for (int i = 0; i < 180; ++i) {
                for (int j = 0; j < 240; ++j) {
                    firstFrame.setRGB(j, i, frame[i][j]);
                }
            }
        }

        JPanel superContainer = new JPanel();
        superContainer.setLayout(new BoxLayout(superContainer, BoxLayout.Y_AXIS));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel panel0 = new JPanel();
        panel0.setLayout(new BoxLayout(panel0, BoxLayout.X_AXIS));
        panel0.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel label = new JLabel(new ImageIcon(firstFrame));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        panel0.add(label);
        panel0.add(new JLabel("Video"));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Motion Descriptor"));
        panel.add(new JLabel(new ImageIcon(motionDescriptorImage)));

        JPanel panel1 = new JPanel();
        panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));

        panel1.add(new JLabel("Color Descriptor"));
        panel1.add(new JLabel(new ImageIcon(colorHistogramDescriptorImage)));

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
        panel2.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel2.add(new JLabel("Audio Descriptor"));
        panel2.add(new JLabel(new ImageIcon(audioDescriptorImage)));

        superContainer.add(panel0);
        container.add(panel);
        container.add(panel1);
        container.add(panel2);
        superContainer.add(container);

        JFrame jFrame = new JFrame("Displaying Descriptors for the video");
        jFrame.getContentPane().add(superContainer);

        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    static void writeDescriptorData(int[] descriptor, String filename)
    {
        try
        {
            DataOutputStream os = new DataOutputStream( new FileOutputStream( filename ) );
            for (int aDescriptor : descriptor) os.writeInt(aDescriptor);
            os.close();
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    static int[] loadDescriptorData( String filename )
    {
        ArrayList<Integer> descriptorList = new ArrayList<>(0);
        try 
        {
            DataInputStream is = new DataInputStream( new FileInputStream( filename ) );
            while ( is.available() > 0 )
            {
                descriptorList.add( is.readInt() );
            }
            is.close();
        } catch ( IOException e )
        {
            e.printStackTrace();
        }

        // convert array list to array
        int[] descriptors = new int[descriptorList.size()];
        for ( int i = 0; i < descriptors.length; ++i )
            descriptors[i] = descriptorList.get(i);
        return descriptors;
    }

    // compute L-2 distance between two descriptors
    static float computeDistance(int[] descriptor1, int[] descriptor2)
    {
        // assuming the descriptors have the same length
        assert descriptor1.length == descriptor2.length;

        float squaredDistance = 0.f;
        for ( int i = 0; i < descriptor1.length; ++i )
        {
            float diff = (float) (descriptor1[i] - descriptor2[i]);
            squaredDistance += diff * diff;
        }

        return (float) Math.sqrt( squaredDistance );
    }
}

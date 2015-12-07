package com.csci576.mmdb;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) {
        DataStore ds = new DataStoreImpl();

        try {
            int[] motionDescriptor = ds.generateMotionDescriptor
                    (args[0]);
            int[] audioDescriptor = ds.generateAudioDescriptor
                    (args[1]);
            int[] colorHistogramDescriptor = ds
                    .generateColorHistogramDescriptor(args[0]);

            displayVideoWithDescriptors(args[0], motionDescriptor,
                    colorHistogramDescriptor, audioDescriptor);

        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
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

        BufferedImage motionDescriptorImage = new BufferedImage(300, 50,
                BufferedImage.TYPE_INT_RGB);
        BufferedImage colorHistogramDescriptorImage = new BufferedImage(300,
                50, BufferedImage.TYPE_INT_RGB);
        BufferedImage audioDescriptorImage = new BufferedImage(300,
                50, BufferedImage.TYPE_INT_RGB);
        BufferedImage firstFrame = new BufferedImage(240, 180, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < 50; ++i) {
            for (int j = 0; j < 300; ++j) {
                int motion_pixel = 0xff000000 |
                        ((motionDescriptor[j / 2] & 0xff) << 16) |
                        ((motionDescriptor[j / 2] & 0xff) << 8) |
                        ((motionDescriptor[j / 2] & 0xff));

                int color_pixel = 0xff000000 |
                        ((colorHistogramDescriptor[j / 2] & 0xff) << 16) |
                        ((colorHistogramDescriptor[j / 2] & 0xff) << 8) |
                        ((colorHistogramDescriptor[j / 2] & 0xff));

                int audio_pixel = 0xff000000 |
                        ((audioDescriptor[j / 2] & 0xff) << 16) |
                        ((audioDescriptor[j / 2] & 0xff) << 8) |
                        ((audioDescriptor[j / 2] & 0xff));

                motionDescriptorImage.setRGB(j, i, motion_pixel);
                colorHistogramDescriptorImage.setRGB(j, i, color_pixel);
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
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));

        JPanel panel0 = new JPanel();
        panel0.setLayout(new BoxLayout(panel0, BoxLayout.Y_AXIS));
        panel0.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel label = new JLabel(new ImageIcon(firstFrame));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        panel0.add(label);
        panel0.add(new JLabel("Video"));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(new ImageIcon(motionDescriptorImage)));
        panel.add(new JLabel("Motion Descriptor!"));

        JPanel panel1 = new JPanel();
        panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        panel1.add(new JLabel(new ImageIcon(colorHistogramDescriptorImage)));
        panel1.add(new JLabel("Color Descriptor"));

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        panel2.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel2.add(new JLabel(new ImageIcon(audioDescriptorImage)));
        panel2.add(new JLabel("Audio Descriptor!"));

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
}

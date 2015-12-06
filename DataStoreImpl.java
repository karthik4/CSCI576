package com.csci576.mmdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of the data store.
 * <p>
 * Created by karthikkumarguru on 12/2/15.
 */
public class DataStoreImpl implements DataStore {
    /**
     * 16 x 12 macroblocks
     */
    private static final int MB_SIZE_X = 16;

    private static final int MB_SIZE_Y = 12;

    /**
     * Searches 2 macro blocks in either direction
     */
    private static final int SEARCH_SPACE = 2;
    @Override
    public void generateAudioDescriptor(final String pathToFile) {
        // TODO: pei-lun
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    @Override
    public void generateMotionDescriptor(final String pathToFile) throws FileNotFoundException {
        // Here lies a O(n**6) algorithm.
        final File videoFile = new File(pathToFile);

        if (!videoFile.exists()) {
            throw new FileNotFoundException("File not found at " + pathToFile);
        }

        InputStream is = new FileInputStream(videoFile);

        long frameLen = videoFile.length();
        byte[] bytes = new byte[(int) frameLen];

        int offset = 0, numRead;

        try {
            while (offset < frameLen && (numRead = is.read(bytes, offset,
                    (int) frameLen - offset)) >= 0) {
                offset += numRead;
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        int [] descriptor_x = new int[149];
        int [] descriptor_y = new int[149];

        int[] massiveDescriptor = new int[149];

        for (int j = 1; j < 150; ++j) {
            descriptor_x[j-1] = 0; // holds the total horizontal motion
            descriptor_y[j-1] = 0; // holds the total vertical motion
            for (int i = 0; i < 225; ++i) {
                int[] motionVector = getMotionVectorForMacroBlock(i, getFrame
                        (j - 1,
                        bytes), getFrame(j, bytes));

                descriptor_x[j-1] += Math.abs(motionVector[0]);
                descriptor_y[j-1] += Math.abs(motionVector[1]);
//                System.out.println("Motion vector for frame " + (j-1) + " & " +
//                        j + " block " + i + ": " +
//                        motionVector[0] + ", " + motionVector[1]);
            }

            massiveDescriptor[j - 1] = descriptor_x[j - 1] + descriptor_y[j - 1];
        }

        for (int desc : massiveDescriptor) {
            System.out.println(desc);
        }
    }

    private int[] getMotionVectorForMacroBlock(final int mbIndex, final int[][]
            refFrame, final int[][] targetFrame) {
        int ul_mb_x = (mbIndex % 15) * 16;
        int ul_mb_y = (mbIndex / 15) * 12;

        int mb_x = 16;
        int mb_y = 12;

        int ss_x = 16 * 5;
        int ss_y = 12 * 5;

        int ss_ul_x = ul_mb_x - 2 * 16;
        int ss_ul_y = ul_mb_y - 2 * 12;

        int min_mad = Integer.MAX_VALUE;

        int[] retVector = new int[2];
        retVector[0] = 0;
        retVector[1] = 0;
        for (int i = ss_ul_y; i + mb_y < ss_ul_y + ss_y; ++i) {
            if (i >= 0 && i + mb_y < refFrame.length) {
                for (int j = ss_ul_x; j + mb_x < ss_ul_x + ss_x; ++j) {
                    if (j >=0 && j + mb_x < refFrame[0].length) {

                        // Compute MAD for the macroblock
                        int summer = 0;
                        for (int i1 = 0; i1 < mb_y; ++i1) {
                            for (int j1 = 0; j1 < mb_x; ++j1) {
                                summer += Math.abs(targetFrame[ul_mb_y + i1][ul_mb_x
                                        + j1] - refFrame[i + i1][j + j1]);
                            }
                        }

                        summer = summer / (mb_x * mb_y);
                        if (summer < min_mad) {
                            retVector[0] = ul_mb_x - j;
                            retVector[1] = ul_mb_y - i;
                            min_mad = summer;
                        }
                    }
                }
            }
        }

        return retVector;
    }

    private void printMacroBlocks(final int[][] frame) {

        for (int i = 0; i < 225; ++i) {
            System.out.println("Macroblock " + i + ": " + (i % 15) * 16 + ", " +
                    (i / 15) * 12 + ", " + ((i % 15) * 16 + 15) + ", " + ((i /
                    15) * 12 + 11));
        }
    }
//
//    private void displayFrame(final int[][] frame) {
//        BufferedImage image = new BufferedImage(240, 180, BufferedImage.TYPE_INT_RGB);
//        for (int i = 0; i < 180 && i < frame.length; ++i) {
//            for (int j = 0; j < 240 && j < frame[i].length; ++j) {
//                image.setRGB(j, i, frame[i][j]);
//            }
//        }
//
//        JPanel panel = new JPanel();
//        panel.add(new JLabel(new ImageIcon(image)));
//
//        JFrame jFrame = new JFrame("Displaying frame 1 and 120");
//        jFrame.getContentPane().add(panel);
//
//        jFrame.pack();
//        jFrame.setVisible(true);
//        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//    }

    private int[][] getFrame(final int frameNumber, final byte[] bytes) {
        int index = 3 * 180 * 240 * frameNumber;
        int[][] frame = new int[180][240];

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

        return frame;
    }

    @Override
    public void generateUnknownDescriptor(final String pathToFile) {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
}

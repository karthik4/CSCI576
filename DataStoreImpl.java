package com.csci576.mmdb;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
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
    public int[] generateAudioDescriptor(final String pathToFile) throws IOException, UnsupportedAudioFileException {
        // TODO: pei-lun
        // read the file
        File wavFile = new File( pathToFile );

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
        AudioFormat audioFormat = audioInputStream.getFormat();

        // get audio information
        int bytesPerFrame = audioFormat.getFrameSize();
        int frameLength = (int) audioInputStream.getFrameLength();

        // set windows size to 1024
        int windowSize = 1024;
        int numBytes = 1024 * bytesPerFrame;
        byte[] audioBytes = new byte[numBytes];
        int numBytesRead = 0;
        int numFramesRead = 0;

        // stores the output audio descriptor
        float[] intensityDescriptor = new float[ frameLength/windowSize + 1 ];

        int windowNumber = 0;
        while ( ( numBytesRead = audioInputStream.read( audioBytes ) ) != -1 )
        {
            // calculate the number of frames actually read.
            numFramesRead = numBytesRead / bytesPerFrame;

            // iterate though the window
            float totalIntensity = 0.f;
            for ( int i = 0; i < numFramesRead; ++i )
            {
                // convert 2 byte values to one float sample value
                float sample = ( (audioBytes[i*2 + 0] & 0xFF) | (audioBytes[i*2 + 1] << 8) ) / 32768.0F;

                // accumulate the intensity
                totalIntensity += Math.abs( sample );
            }

            // compute the average intensity
            totalIntensity /= numFramesRead;

            // put the average in the intensity vector
            intensityDescriptor[windowNumber++] = totalIntensity;
        }

        int[] outputDescriptor = new int[intensityDescriptor.length];

        for ( int i = 0; i < outputDescriptor.length; ++i )
        {
            outputDescriptor[i] = (int) (intensityDescriptor[i] * 255);
        }

        return outputDescriptor;
    }

    @Override
    public int[] generateMotionDescriptor(final String pathToFile) throws
            FileNotFoundException {
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
        int range = 0;

        for (int j = 1; j < 150; ++j) {
            descriptor_x[j-1] = 0; // holds the total horizontal motion
            descriptor_y[j-1] = 0; // holds the total vertical motion
            for (int i = 0; i < 225; ++i) {
                int[] motionVector = getMotionVectorForMacroBlock(i, getFrame
                        (j - 1,
                        bytes), getFrame(j, bytes));

                descriptor_x[j-1] += Math.abs(motionVector[0]);
                descriptor_y[j-1] += Math.abs(motionVector[1]);
            }

            massiveDescriptor[j - 1] = descriptor_x[j - 1] + descriptor_y[j - 1];

            if (massiveDescriptor[j - 1] > range) {
                range = massiveDescriptor[j - 1];
            }
        }

        double qInterval = range / 255.00;
        int[] quantizedMotionDescriptor = new int[150];

        quantizedMotionDescriptor[0] = 0;

        for (int i = 1; i < 150; ++i) {
            quantizedMotionDescriptor[i] = (int) Math.round(massiveDescriptor[i - 1] / qInterval);
        }

        return massiveDescriptor;
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
    public int[] generateColorHistogramDescriptor(final String pathToFile) throws FileNotFoundException {

        int frameWidth = 240;
        int frameHeight = 180;
        int numFrames = 150;

        File rgbFile = new File( pathToFile );
        if ( !rgbFile.exists() ) {
            throw new FileNotFoundException("File not found at " + pathToFile);
        }
        InputStream is = new FileInputStream( rgbFile );

        long rgbFileLength = rgbFile.length();
        byte[] rgbBytes = new byte[(int)rgbFileLength];

        int offset = 0;
        int numRead = 0;
        try {
            while (offset < rgbBytes.length && (numRead=is.read(rgbBytes, offset, rgbBytes.length-offset)) >= 0) {
                offset += numRead;
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        int numHueBins = 9;
        float[] colorHistogramDescriptor = new float[ 30 * numHueBins ];

        int ind = 0;
        // get one descriptor for every 5 frames
        for ( int s = 0; s < 30; ++s )
        {
            int totalNumPixels = 0;
            // accumulate color histogram for 5 frames
            for ( int f = 0; f < 5; ++f )
            {
                for ( int y = 0; y < frameHeight; ++y )
                {
                    for ( int x = 0; x < frameWidth; ++x )
                    {
                        float r = byte2float( rgbBytes[ind] );
                        float g = byte2float( rgbBytes[ind+frameHeight*frameWidth] );
                        float b = byte2float( rgbBytes[ind+frameHeight*frameWidth*2] );

                        // transform rgb color space to get hue
                        float h = rgb2h( r, g, b );

                        // quantize hue to 0 to 8
                        h = (float) Math.floor( h / 360 * numHueBins );
                        if (h == numHueBins) b = 8;

                        int colorIndex = (int) h;

                        colorHistogramDescriptor[ s*9 + colorIndex ]++;
                        totalNumPixels++;

                        ind++;
                    }
                }

                ind += frameHeight*frameWidth*2;
            }

            for ( int c = 0; c < 9; ++c )
                colorHistogramDescriptor[s*9+c] /= totalNumPixels;
        }

        int[] outputDescriptor = new int[colorHistogramDescriptor.length];

        for ( int i = 0; i < outputDescriptor.length; ++i )
        {
            outputDescriptor[i] = (int) (colorHistogramDescriptor[i] * 255);
        }

        return outputDescriptor;
    }

    static float byte2float( byte b )
    {
        return ( ((float)b) % 256) + ( ((float) b) < 0 ? 256 : 0 );
    }

    static float rgb2h( float r, float g, float b )
    {
        float h, s, v;
        float M = Math.max(r, Math.max(g, b));
        float m = Math.min(r, Math.min(g, b));
        v = M;
        if( M == 0 )
            s = 0;
        else
            s = (M-m)/M;
        if( M == m ) h = 0; /* It is achromatic color */
        else if( M == r && g >= b )
            h = (float) (60*(g-b)/(M-m));
        else if( M == r && g < b )
            h = (float) (360 + 60*(g-b)/(M-m));
        else if( g == M )
            h = (float) (60*(2.0 + (b-r)/(M-m)));
        else
            h = (float) (60*(4.0 + (r-g)/(M-m)));
        return h;
    }
}

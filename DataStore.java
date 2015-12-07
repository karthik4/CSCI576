package com.csci576.mmdb;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A simple interface for the data store.
 *
 * Created by karthikkumarguru on 12/1/15.
 */
public interface DataStore {
    /**
     * Generate descriptor based on audio content in the file.
     * The generated descriptor is written to disk for reuse (location?)
     *
     * @param pathToFile path to the audio file for generating descriptor.
     */
    int[] generateAudioDescriptor(final String pathToFile) throws
            UnsupportedAudioFileException, IOException;

    /**
     * Generate descriptor based on degree of motion in the video file.
     * The generated descriptor is written to disk for resuse(location?)
     *
     * @param pathToFile the video file used for generating descriptor.
     */
    int[] generateMotionDescriptor(final String pathToFile) throws
            FileNotFoundException;

    /**
     * Generate descriptor based on the third parameter(which is?).
     * The generated descriptor is written to disk for reuse(location?)
     *
     * @param pathToFile the file used to generate descriptor.
     */
    int[] generateColorHistogramDescriptor(final String pathToFile) throws
            FileNotFoundException;
}

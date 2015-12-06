package com.csci576.mmdb;

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
    void generateAudioDescriptor(final String pathToFile);

    /**
     * Generate descriptor based on degree of motion in the video file.
     * The generated descriptor is written to disk for resuse(location?)
     *
     * @param pathToFile the video file used for generating descriptor.
     */
    void generateMotionDescriptor(final String pathToFile);

    /**
     * Generate descriptor based on the third parameter(which is?).
     * The generated descriptor is written to disk for reuse(location?)
     *
     * @param pathToFile the file used to generate descriptor.
     */
    void generateUnknownDescriptor(final String pathToFile);
}
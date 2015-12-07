package com.csci576.mmdb;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {
        DataStore ds = new DataStoreImpl();

        try {
            int [] motionDescriptorImage = ds.generateMotionDescriptor
                    ("commercial1.v576.rgb");
            int [] audioDescriptorImage = ds.generateAudioDescriptor
                    ("commercial1.v576.rgb");
            int [] colorHistogramDescriptorImage = ds.generateColorHistogramDescriptor
                    ("commercial1.v576.rgb");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

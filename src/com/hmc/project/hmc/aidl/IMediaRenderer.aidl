package com.hmc.project.hmc.aidl;

interface IMediaRenderer {
    boolean play(String path);
    boolean stop();
//    boolean next();
//    boolean prev();
    boolean close();
    boolean pause();
}
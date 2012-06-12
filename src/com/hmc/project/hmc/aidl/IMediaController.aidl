package com.hmc.project.hmc.aidl;

interface IMediaController {
    boolean play(String path);
    boolean stop();
//    boolean next();
//    boolean prev();
    boolean pause();
}
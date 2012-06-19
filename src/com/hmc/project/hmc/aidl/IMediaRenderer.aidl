package com.hmc.project.hmc.aidl;

interface IMediaRenderer {
    boolean play(String path);
    boolean stop();
    boolean seekFordard();
    boolean seekBackward();
    boolean close();
    boolean pause();
    boolean playFromPosition(int pos, String path);

}
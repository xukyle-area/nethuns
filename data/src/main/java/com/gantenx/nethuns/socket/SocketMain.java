package com.gantenx.nethuns.socket;


import com.gantenx.nethuns.socket.base.SocketTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketMain {

    public static void main(String[] args) {
        SocketTask.startSocketJob();
    }
}

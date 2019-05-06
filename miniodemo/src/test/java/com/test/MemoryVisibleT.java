package com.test;

import org.junit.Test;

public class MemoryVisibleT {

    public static void main(String[] args) {
            Demo d = new Demo();
            new Thread(d).start();
        while (true) {
            if (d.flag){
                    System.out.println("flag"+d.flag);
                }
            }
    }
    static class Demo implements Runnable {
        private boolean flag = false;
        @Override
        public void run() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            flag = true;
            System.out.println(flag);
        }
    }
}

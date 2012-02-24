package com.addepar.www.scrivener;

import java.util.Scanner;

public class S4JDemo extends S4JTests {

    boolean halted = true;

    public static void main(String[] args) {
        final S4JDemo demo = new S4JDemo();

        new Thread() {
            @Override
            public void run() {
                for (Scanner in = new Scanner(System.in); ; ) {
                    String s = in.nextLine();
                    if (s.equals("halt")) {
                        demo.halted = true;
                    } else if (s.equals("resume")) {
                        demo.halted = false;
                    } else if (s.equals("die")) {
                        System.out.println("Finished demo!");
                        System.exit(0);
                    }
                    synchronized (demo) {
                        demo.notifyAll();
                    }
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (demo.halted) {
                            synchronized (demo) {
                                demo.wait();
                            }
                        }
                        Thread.sleep(1000);
                        System.out.println("T2!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}

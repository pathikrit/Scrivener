package com.pathikrit.www.scrivener.client;

import static com.pathikrit.www.scrivener.client.S4J.*;

public class S4JTester {

    public static void main(String[] args) {
        S4J.config("adp-rick-test", "http://localhost:8124");
        warn("test", new RuntimeException());
        foo();
        print(3, "ok");
    }

    public static void foo() {
        debug("test2");
        error("test3", new IllegalArgumentException("damn"));
        new Thread() {
            @Override
            public void run() {
                debug(new Exception("damn5!"));
            }
        }.start();
    }
}

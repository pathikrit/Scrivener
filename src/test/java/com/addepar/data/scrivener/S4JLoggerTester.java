package com.addepar.data.scrivener;

import static com.addepar.data.scrivener.S4JLogger.*;

public class S4JLoggerTester {

    public static void main(String[] args) {
        config("adp-rick-test", "localhost");
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

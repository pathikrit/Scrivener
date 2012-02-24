package com.addepar.www.scrivener;

import static com.addepar.www.scrivener.S4J.debug;
import static com.addepar.www.scrivener.S4J.error;

public class S4JExample {

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        S4J.config("adp-rick-test", "http://localhost:8124", "http://github.com/pathikrit/Scrivener/tree/master/src/main/java");

        error("Greenbaum is here", new NullPointerException());

//        warn("hello");
//        warn(UUID.randomUUID().toString());
//        stat("foo", Math.random());
//        stat("foo", Math.random());
//
//        stat("call", 1);
//
//        warn("test", new RuntimeException());
//        foo();
//        print(3, "ok");
//        stat("time", System.currentTimeMillis() - time);
        S4J.stop();
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
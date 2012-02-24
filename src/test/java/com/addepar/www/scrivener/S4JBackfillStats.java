package com.addepar.www.scrivener;

import org.joda.time.DateTime;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;
import java.net.URLEncoder;

public class S4JBackfillStats extends S4JTests {

    public static void main(String[] args) throws IOException {
        final String[] statKeys = {"blackbox-api-calls", "blackbox-latency", "amp-server-cache-misses", "amp-server-cache-hits", "amp-client-bttn-click"};

        for(DateTime i = new DateTime().withDate(2011, 5, 12), today = DateTime.now(); i.isBefore(today); i = i.plusDays(1)) {
            double value = Math.random();
            for(int j = 0; j < statKeys.length; j++) {
                hit(statKeys[j], j+value + 0.1*Math.random(), i);
            }
            System.out.println("Generated data for " + i);
        }
        S4J.stop();
    }

    public static void hit(String key, Number value, DateTime date) throws IOException {
        String url = "http://localhost:8124/stat?appid=adp-rick-test&entry=";
        final String entryFormat = "{\"key\":\"%s\",\"timestamp\":%d,\"value\":%f}";
        final String entry = String.format(entryFormat, key, date.getMillis(), value);
        url += URLEncoder.encode(entry, "utf-8");
        HttpConnection.connect(url).ignoreContentType(true).ignoreHttpErrors(true).post();
    }
}

package com.addepar.www.scrivener;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.HttpConnection;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseStackTrace;

public final class S4J {

    private static final S4J instance = new S4J();

    private static final Gson gson = new Gson();
    private static final String urlFormat = "%s/%s?appid=%s&entry=%s";

    private final BlockingQueue<Entry> logBuffer = new LinkedBlockingDeque<Entry>(), statBuffer = new LinkedBlockingDeque<Entry>();

    private final String user = System.getProperty("user.name"), host;

    private String appId = "NULL", server = "http://localhost:8124";

    private S4J() {
        String _host;
        try {
            _host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            _host = System.getProperty("os.name");
        }
        host = _host;

        new Thread() { @Override public void run() {  consume("log", instance. logBuffer); }}.start();
        new Thread() { @Override public void run() { consume("stat", instance.statBuffer); }}.start();
    }

    public static void config(String appId, String server) {
        instance.appId = appId;
        instance.server = StringUtils.removeEnd(server, "/");
        debug("Using scrivener server=%s and appId=%s", appId, server);
    }

    public static void debug(Object msg) { debug(msg, null); }
    public static void  warn(Object msg) {  warn(msg, null); }
    public static void error(Object msg) { error(msg, null); }

    public static void debug(String format, Object... args) { debug(String.format(format, args)); }
    public static void  warn(String format, Object... args) {  warn(String.format(format, args)); }
    public static void error(String format, Object... args) { error(String.format(format, args)); }

    public static void debug(Throwable error) { debug(null, error); }
    public static void  warn(Throwable error) {  warn(null, error); }
    public static void error(Throwable error) { error(null, error); }

    public static void debug(Object msg, Throwable error) { log("DEBUG", msg, error); }
    public static void  warn(Object msg, Throwable error) { log( "WARN", msg, error); }
    public static void error(Object msg, Throwable error) { log("ERROR", msg, error); }

    public static void print(Object... objs) { debug(Arrays.deepToString(objs)); }

    public static void stat(Object key, Number val) { put(instance.statBuffer, new StatEntry(key, val));  }

    public static void log(String type, Object msg, Throwable error) { put(instance.logBuffer, new LogEntry(type, msg == null ? "" : msg.toString(), error)); }

    private static void put(BlockingQueue<Entry> queue, Entry entry) {
        try {
            queue.put(entry);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void consume(String method, BlockingQueue<Entry> queue) {
        try {
            for(Entry entry; (entry = queue.take()).timestamp > 0; ) {
                try {
                    if (entry instanceof LogEntry) {
                        entry = new FullLogEntry((LogEntry) entry);
                        System.out.println(entry);
                    }
                    final String url = String.format(urlFormat, instance.server, method, instance.appId, URLEncoder.encode(gson.toJson(entry), "UTF-8"));
                    HttpConnection.connect(url).ignoreContentType(true).ignoreHttpErrors(true).post();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        final Entry poision = new Entry(0){};
        instance.logBuffer.add(poision);
        instance.statBuffer.add(poision);
    }

    private static abstract class Entry {
        protected final long timestamp;

        protected Entry(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    private static class StatEntry extends Entry {
        private final String key;
        private final Number value;

        private StatEntry(Object key, Number value) {
            super(System.currentTimeMillis());
            this.key = key == null ? "" : key.toString();
            this.value = value;
        }
    }

    private static class LogEntry extends Entry {
        private static final String secret = UUID.randomUUID().toString();

        private final String type, message, thread = Thread.currentThread().getName();
        private final Throwable error;

        private LogEntry(String type, String message, Throwable error) {
            super(System.currentTimeMillis());
            this.type = type;
            this.message = message;
            this.error = error == null ? new Exception(secret) : error;
        }
    }

    private static class FullLogEntry extends Entry {
        private static final Set<String> ignore = new HashSet<String>() {{
            add(Thread.class.getName());
            add(S4J.class.getName());
            add(LogEntry.class.getName());
        }};

        private static final String logFormat = "%s@%s> (%td-%3$tb-%3$ty %3$tH:%3$tM:%3$tS) %s in thread=%s at %s: %s";

        private final String user, host, type, thread, message, caller, stacktrace[];

        private FullLogEntry(LogEntry logEntry) {
            super(logEntry.timestamp);
            this.user = instance.user;
            this.host = instance.host;
            this.type = logEntry.type;
            this.thread = logEntry.thread;
            this.message = logEntry.message;
            final Throwable error = logEntry.error;
            this.caller = getCaller(error.getStackTrace());
            this.stacktrace = LogEntry.secret.equals(error.getMessage()) ? null : getRootCauseStackTrace(error);
        }

        private String getCaller(StackTraceElement[] stackTraceElements) {
            for (final StackTraceElement stackTraceElement : stackTraceElements)
                if (!ignore.contains(stackTraceElement.getClassName()))
                    return stackTraceElement.toString();
            return null;
        }

        @Override
        public String toString() {
            final StringBuilder log = new StringBuilder(String.format(logFormat, user, host, timestamp, type, thread, caller, message));
            if (stacktrace != null) { log.append("\n\t").append(StringUtils.join(stacktrace, '\n')); }
            return log.toString();
        }
    }
}

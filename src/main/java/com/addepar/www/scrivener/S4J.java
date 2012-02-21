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

    private final BlockingQueue<LogEntry> logBuffer = new LinkedBlockingDeque<LogEntry>();
    private final BlockingQueue<StatEntry> statBuffer = new LinkedBlockingDeque<StatEntry>();

    private final String user = System.getProperty("user.name");
    private final String host;
    private String appId = "NULL";
    private String server = "http://localhost:8124";

    private S4J() {
        String _host;
        try {
            _host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            _host = System.getProperty("os.name");
        }
        host = _host;

        new Thread() {
            @Override public void run() {
                while (true) {
                    try {
                        final FullLogEntry logEntry = new FullLogEntry(instance.logBuffer.take());
                        send(logEntry);
                        System.out.println(logEntry);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }.start();

        new Thread() {
            @Override public void run() {
                while (true) {
                    try {
                        send(instance.statBuffer.take());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static void config(String appId, String server) {
        instance.appId = appId;
        instance.server = StringUtils.removeEnd(server, "/");
        debug("Using scrivener server=%s and appId=%s", appId, server);
    }

    private static enum Type {DEBUG, WARN, ERROR}

    public static void debug(Object msg) { debug(msg, null); }
    public static void  warn(Object msg) {  warn(msg, null); }
    public static void error(Object msg) { error(msg, null); }

    public static void debug(String format, Object... args) { debug(String.format(format, args)); }
    public static void  warn(String format, Object... args) {  warn(String.format(format, args)); }
    public static void error(String format, Object... args) { error(String.format(format, args)); }

    public static void debug(Throwable error) { debug(null, error); }
    public static void  warn(Throwable error) {  warn(null, error); }
    public static void error(Throwable error) { error(null, error); }

    public static void debug(Object msg, Throwable error) { log(Type.DEBUG, msg, error); }
    public static void  warn(Object msg, Throwable error) { log( Type.WARN, msg, error); }
    public static void error(Object msg, Throwable error) { log(Type.ERROR, msg, error); }

    public static void print(Object... objs) { debug(Arrays.deepToString(objs)); }

    public static void stat(Object key, Number val) { put(instance.statBuffer, new StatEntry(key, val));  }

    private static void log(Type type, Object msg, Throwable error) { put(instance.logBuffer, new LogEntry(type, msg == null ? "" : msg.toString(), error)); }

    private static <T> void put(BlockingQueue<T> queue, T obj) {
        try {
            queue.put(obj);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void send(Object entry) {
        final String method;
        if (entry instanceof FullLogEntry)   method =  "log";
        else if (entry instanceof StatEntry) method = "stat";
        else throw new IllegalArgumentException("Unknown entry type: " + entry.getClass());
        try {
            final String url = String.format(urlFormat, instance.server, method, instance.appId, URLEncoder.encode(gson.toJson(entry), "UTF-8"));
            HttpConnection.connect(url).ignoreContentType(true).ignoreHttpErrors(true).post();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class StatEntry {
        private final String key;
        private final long timestamp = System.currentTimeMillis();
        private final Number value;

        private StatEntry(Object key, Number value) {
            this.key = key == null ? "" : key.toString();
            this.value = value;
        }
    }

    private static class LogEntry {
        private static final String secret = UUID.randomUUID().toString();

        private final Type type;
        private final String message;
        private final String thread = Thread.currentThread().getName();
        private final Throwable error;
        private final long timestamp = System.currentTimeMillis();

        private LogEntry(Type type, String message, Throwable error) {
            this.type = type;
            this.message = message;
            this.error = error == null ? new Exception(secret) : error;
        }
    }

    private static class FullLogEntry {
        private static final Set<String> ignore = new HashSet<String>() {{
            add(Thread.class.getName());
            add(S4J.class.getName());
            add(LogEntry.class.getName());
        }};

        private static final String logFormat = "%s@%s> (%td-%3$tb-%3$ty %3$tH:%3$tM:%3$tS) %s in thread=%s at %s: %s";

        private final String user;
        private final String host;
        private final long timestamp;
        private final String type;
        private final String thread;
        private final String caller;
        private final String message;
        private final String stackTrace;

        private FullLogEntry(LogEntry logEntry) {
            this.user = instance.user;
            this.host = instance.host;
            this.timestamp = logEntry.timestamp;
            this.type = logEntry.type.name();
            this.thread = logEntry.thread;
            final Throwable error = logEntry.error;
            this.caller = getCaller(error.getStackTrace());
            this.message = logEntry.message;
            this.stackTrace = LogEntry.secret.equals(error.getMessage()) ? null : StringUtils.join(getRootCauseStackTrace(error), '\n');
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
            if (stackTrace != null) { log.append("\n\t").append(stackTrace); }
            return log.toString();
        }
    }
}

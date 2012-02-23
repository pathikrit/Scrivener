**Scrivener** is a simple log/stat aggregation framework inspired by Facebook's Scribe project:

- **Asynchronous Logging**: Messages are put in a light-weight blocking-queue on the client-side and a separate consumer-thread pulls them out from the queue and sends to server. Blazingly fast logging.
- **Simple Interface**: More info logs (line-numbering) without overheads. No more Logger.getLogger(classname). Simply static import the public methods of S4J.java. Stacktracking/caller-filename-inference are all done in a separate thread using reflection.
- **Distributed Logging**: A single server sees all log/stat messages. Makes co-relation easier for a distributed platform.
- **Stat Framework**: Easy stat framework. A single static method stat(key,num). If you want to keep counters, simply call stat(key,1).
- **Integrated Viewer**: A web based log/statistics viewer. No need to ssh into bunch of different servers.

---

**Java Client**:
Simple Java client - S4J.java has 4 essential public static methods:

- **config**(appId, server): Call at the entry point of your app with appId and Scrivener server. Group of related programs should have same appId.
- **debug/warn/error/log**: Simple static methods that uses reflection (in separate thread) to figure out filename and line number etc
- **stat**: Takes in key and a number. Visualizations are auto generated on server side.
- **stop**: Optionally call this at the exit point of your app to make sure all messages are flushed.

---

**Server**:
A simple nodejs server backed by couchdb (sharded by appIds) that accepts log/stat messages. Available API methods:

-   POST   /log?appid=xxx&entry=json
-    GET   /log?appid=xxx&since=timestamp
- DELETE   /log?appid=xxx
-    GET  /stat?appid=xxx&since=timestamp
- DELETE  /stat?appid=xxx
-   POST  /stat?appid=xxx&entry=json

---

**Viewer**:
The same nodejs server serves a single page web app for log/stat visualization at the root-level:

-    GET  /

The client side visualization uses d3/Google Charts API with jquery/underscore.js sugar

---

**Usage**:

- Use in Java? Look at S4JExample.java
- Install and start the server - First run setup.sh which should install everything necessary and then run start_server.sh

---

**TODO**:

- Websockets and since parameter to push updates
- shellinabox
- Different color of log levels (debug-green, error-red, default yellow)
- Figure out github repo automatically
- Stat framework can send extended info like memory, cpu
- Add freeze update button, repopulate fields from sql query on update
- If timestamp does not exist, add it on server side


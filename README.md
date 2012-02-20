**Scrivener** is a simple log/stat aggregation framework inspired from Facebook's Scribe project:

- **Asynchronous Logging**: Messages are put in a lightweight blockingqueue on the client-side and a separate consumer-thread pulls them out from the queue and sends to server. Blazingly fast logging.
- **Simple Interface**: More info logs (line-numbering) without overheads. No more Logger.getLogger(classname). Simply static import the public methods of S4J.java. Stacktracking/caller-filename-inference are all done in a separate thread using reflection.
- **Distributed Logging**: A single server sees all log/stat messages. Makes corelation easier for a distributed platform
- **Stat Framework**: Easy stat framework. A single static method stat(key,num). If you want to keep counters, simply call stat(key,1).
- **Integrated Viewer**: A web based log/statistics viewer. No need to ssh into bunch of different servers

**Java Client**:
Simple Java client - S4J.java has 5 essential public static methods:

- **config**(appId, server): Call at the entry point of your app with appId and Scrivener server
- **debug/warn/error**: Simple static methods that uses reflection (in separate thread) to figure out filename and line number etc
- **stat**: Takes in key and a number. Visualizations are auto generated on server side

**Server**:
A simple nodejs server backed by couchdb that accepts log/stat messages

**Viewer**:
The same nodejs server serves a single page web app for log/stat visualization


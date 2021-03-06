# Simple Event Facade for Java ... is to Event what flat log message is to Slf4J

*This library is helpful for instrumenting code with an event api, and enrich "Applicative Thread Call Stack".*
Event not only produces logs, but they can also be used by other type of listeners,
for example to update performance metrics statistics.

## Build status

[![Build Status](https://api.travis-ci.org/lauvigne/sef4j.png)](https://travis-ci.org/lauvigne/sef4j)

##

This library is helpful for instrumenting code with an event api, and enrich "Applicative Thread Call Stack".
Event not only produces logs, but they can also be used by other type of listeners, 
for example to update performance metrics statistics.

For the developer, instrumenting code is as simple as wrapping some method by push()/pop() aspect:
```java
   public void foo() {
     StackPropper toPop = LocalCallStack.push("foo");
     try {
     	// do foo...
     	bar(123);
     } finally {
     	toPop.close();
     }
   }

   public void bar(int param1) {
     StackPropper toPop = LocalCallStack.meth("bar")
     	.withLogger(LOG, LogLevel.OFF, LogLevel.INFO, 200) // <= log on pop() only if time exceed 200ms
     	.withParam("param1", param1) // <= enrich current call stack elt with param
     	.withInheritableProp("prop", 123) // <= enrich all child call stack elt with heritable prop
     	.push()
     try {
     	// do bar...
     } finally {
     	toPop.close();
     }
   }
```

This will enrich an "applicative call stack", and produce push event and pop events, 
that can be captured by listeners.


A Typical usage is to produce 4 default real-time source of information when instrumenting code:
- Logs ... but not always !! To many logs may render your system slow, and be unusable
- Real-time load monitoring: 
  counters of pending running tasks (number of concurrent execution for a given code).
  to give system admin feedback like: 
     "currently running 5 tasks on X/Y/Z, with cumulated time=30 seconds"
- Real-time progress for a task:
  to give user feedback message for a specific running task, like 
     "processing 3 / 100 ... estimated time remaining= 30 seconds"
- Delayed performance measurement statistics:
  to give developers feedback like:
     "stats for X/Y/Z: executed 127 times, cumulated time=26 seconds, histogram: [0-0 ms]: 13 times, [1-32 ms]: 45 x avg=16ms, [33-64 ms]: 30 x 40ms ..."

Developers are completely free to add more handlers on call stack events, to do custom statistics,
for example to aggregate counters per category, using some provided method parameter value.

     
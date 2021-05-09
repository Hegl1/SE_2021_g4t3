# "A fatal error has been detected by the Java Runtime Environment"
Houston, we have a problem...

## When does it occur? Is it reproducable?

After freshly running the `installScript.sh`, the `buildScript.sh` and the `executeScript.sh` on the raspberry, there is a high chance that this error occurs at some point after the executed program established a connection to the TimeFlip device and read needed information from files.

```
...
Connection established
backend URL file exists
backend URL successfully read from file
URL of backend: http://192.168.0.220:8080
ID file exists
ID successfully read from file
#
# A fatal error has been detected by the Java Runtime Environment:
#
#  SIGSEGV (0xb) at pc=0xb51f5d30, pid=6791, tid=0xa0213460
#
# JRE version: OpenJDK Runtime Environment (8.0_212-b01) (build 1.8.0_212-8u212-b01-1+rpi1-b01)
# Java VM: OpenJDK Client VM (25.212-b01 mixed mode linux-aarch32 )
# Problematic frame:
# C  [libstdc++.so.6+0x83d30]  std::__detail::_List_node_base::_M_unhook()+0x8
#
# Core dump written. 
...
```

As for reproducability, after freshly running the afore-mentioned scripts the error is most likely to occur but it doesn't *always* occur under those conditions.

## What is going wrong?

It seems like there is a problem with an invalid memory access since we got the signal `SIGSEGV`. In the error report file `hs_err_pid1259.log` we can further find the signal code `SEGV_MAPERR`. 

As [this answer on stackoverflow](https://stackoverflow.com/a/28116503) suggests: "A page was accessed that is not even mapped into the address space of the application at all. This will often result from dereferencing a null pointer or a pointer that was corrupted with a small integer value. This is reported as SEGV_MAPERR."

Now the question is, which code/file/whatever causes this?


I found [this acticle](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/crashes001.html) which states: "If the fatal error log indicates the problematic frame to be a native library, there might be a bug in native code or the Java Native Interface (JNI) library code."

Our error report contains following snippet:
```
# Problematic frame:
# C  [libstdc++.so.6+0x83d30]  std::__detail::_List_node_base::_M_unhook()+0x8
```
The "C" indicates that the crash occured because of some native library, in our case the `SIGSEGV` occured with a thread executing in the library `libstdc++.so`.

## Can it be solved? 

[This acticle](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/crashes001.html) suggests that we should just "file a bug report".

Also, we were not the first people to encounter bugs with the afore-mentioned library. [(example)](https://bugs.openjdk.java.net/browse/JDK-8188798) 

[Here](https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4694590) it is claimed that "there are multiple issues surrounding libstdc++.so". Suggested solutions/workarounds from this source are to "statically link libstdc++.so" or to "install libstdc++ [...] manually". The problem is supposed to be following:

"The binary interface of libstdc++ has been constantly changing in the past
[...] years, if user JNI code is written in C++ and not compiled by the same
compiler we used to build JDK (i.e. egcs-2.91), at runtime, there will be
two incompatible libstdc++.so being loaded into memory at the same time.

That isn't healthy, in particular, when two libraries have common symbols
(yes, there are many common symbols between different libstdc++.so), symbols
from the first loaded library will be used to resolve all references. This can
lead to crashes or aborts."

**TODO ask PS teacher how to further proceed**

Observation: The error message says `libstdc++.so.6+0x83d30` and after Marcel suggested that might be connected to the version of g++ which is installed on the raspberry, I looked at the version: it's `g++ (Raspbian 8.3.0-6+rpi1) 8.3.0`. 
Are the "+0x83d30" from the `libstdc++.so` in the error message and the "8.3.0" from the g++ version connected? Should we install a different (older/newer) version of g++ to try to solve the problem? If yes, how do we find the right version? Or should we "statically link" or "manually install" `something somewhere somehow???

## Our workaround solution

Since the error only seems to occur on the first execution of our Java program, our suggested workaround solution is: 
- just ignore the fatal error
- change the battery of the TimeFlip device (The program didn't stop properly so the dice had no chance to disconnect. Battery change is the universal fix for this.) 
- wait a few seconds after the battery change
- re-run the program

It usually just magically works fine for any further executions.

package pw.yumc.Yum.runnables;

import java.lang.Thread.State;
import java.util.TimerTask;
import java.util.logging.Level;

import cn.citycraft.PluginHelper.kit.PKit;

/**
 *
 * @since 2016年6月22日 下午4:57:32
 * @author 喵♂呜
 */
public class MainThreadCheckTask extends TimerTask {

    private final Thread mainThread;

    public MainThreadCheckTask(final Thread mainThread) {
        this.mainThread = mainThread;
    }

    @Override
    public void run() {
        // According to this post the thread is still in Runnable although it's waiting for
        // file/http ressources
        // https://stackoverflow.com/questions/20795295/why-jstack-out-says-thread-state-is-runnable-while-socketread
        if (mainThread.getState() == State.RUNNABLE) {
            // Based on this post we have to check the top element of the stack
            // https://stackoverflow.com/questions/20891386/how-to-detect-thread-being-blocked-by-io
            final StackTraceElement[] stackTrace = mainThread.getStackTrace();
            final StackTraceElement topElement = stackTrace[0];
            if (topElement.isNativeMethod()) {
                // Socket/SQL (connect) - java.net.DualStackPlainSocketImpl.connect0
                // Socket/SQL (read) - java.net.SocketInputStream.socketRead0
                // Socket/SQL (write) - java.net.SocketOutputStream.socketWrite0
                if (isElementEqual(topElement, "java.net.DualStackPlainSocketImpl", "connect0")
                        || isElementEqual(topElement, "java.net.SocketInputStream", "socketRead0")
                        || isElementEqual(topElement, "java.net.SocketOutputStream", "socketWrite0")) {
                    PKit.i().getLogger().log(Level.WARNING, "主线程存在网络操作 服务器处于停止状态...", stackTrace);
                }
                // File (in) - java.io.FileInputStream.readBytes
                // File (out) - java.io.FileOutputStream.writeBytes
                else if (isElementEqual(topElement, "java.io.FileInputStream", "readBytes") || isElementEqual(topElement, "java.io.FileOutputStream", "writeBytes")) {
                    PKit.i().getLogger().log(Level.WARNING, "主线程存在IO操作 服务器处于停止状态...", stackTrace);
                }
            }
        }
    }

    private boolean isElementEqual(final StackTraceElement traceElement, final String className, final String methodName) {
        return traceElement.getClassName().equals(className) && traceElement.getMethodName().equals(methodName);
    }
}

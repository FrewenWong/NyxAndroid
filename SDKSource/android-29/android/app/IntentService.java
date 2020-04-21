/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import android.annotation.UnsupportedAppUsage;
import android.annotation.WorkerThread;
import android.annotation.Nullable;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

/**
 * IntentService is a base class for {@link Service}s that handle asynchronous
 * requests (expressed as {@link Intent}s) on demand.  Clients send requests
 * through {@link android.content.Context#startService(Intent)} calls; the
 * service is started as needed, handles each Intent in turn using a worker
 * thread, and stops itself when it runs out of work.
 *
 * <p>This "work queue processor" pattern is commonly used to offload tasks
 * from an application's main thread.  The IntentService class exists to
 * simplify this pattern and take care of the mechanics.  To use it, extend
 * IntentService and implement {@link #onHandleIntent(Intent)}.  IntentService
 * will receive the Intents, launch a worker thread, and stop the service as
 * appropriate.
 *
 * <p>All requests are handled on a single worker thread -- they may take as
 * long as necessary (and will not block the application's main loop), but
 * only one request will be processed at a time.
 *
 * <p class="note"><b>Note:</b> IntentService is subject to all the
 * <a href="/preview/features/background.html">background execution limits</a>
 * imposed with Android 8.0 (API level 26). In most cases, you are better off
 * using {@link android.support.v4.app.JobIntentService}, which uses jobs
 * instead of services when running on Android 8.0 or higher.
 * </p>
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For a detailed discussion about how to create services, read the
 * <a href="{@docRoot}guide/components/services.html">Services</a> developer
 * guide.</p>
 * </div>
 *
 * @see android.support.v4.app.JobIntentService
 * @see android.os.AsyncTask
 */
public abstract class IntentService extends Service {
    private volatile Looper mServiceLooper;
    @UnsupportedAppUsage
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // IntentService启动的时候发送的Intent信息会在这里交给onHandleIntent处理
            // 这个一个抽象方法，留给子类去实现，由用户去进行自行处理
            onHandleIntent((Intent)msg.obj);
            // 执行完任务之后，调用stopSelf停止IntentService
            // 之所以采用stopSelf(int startId)而不是stopSelf()来停止服务，
            // 那是因为stopSelf()会立刻停止服务，而这个时候可能还有其他消息未处理，
            // stopSelf(int startId)则会等待所有的消息都处理完毕后才终止服务。
            stopSelf(msg.arg1);
        }
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public IntentService(String name) {
        super();
        mName = name;
    }

    /**
     * Sets intent redelivery preferences.  Usually called from the constructor
     * with your preferred semantics.
     *
     * <p>If enabled is true,
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_REDELIVER_INTENT}, so if this process dies before
     * {@link #onHandleIntent(Intent)} returns, the process will be restarted
     * and the intent redelivered.  If multiple Intents have been sent, only
     * the most recent one is guaranteed to be redelivered.
     *
     * <p>If enabled is false (the default),
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_NOT_STICKY}, and if the process dies, the Intent
     * dies along with it.
     */
    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.

        super.onCreate();
        // 实例化HanlderThread的对象。线程的名称是我们传入的名称
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        // 启动对象线程
        thread.start();
        // 获取这个HanlderThread的线程的Looper对象
        mServiceLooper = thread.getLooper();
        // 传入当前Looper对象然后实例化Handler对象
        // 注意：使用哪个线程的Looper对象创建的Handler，那么事件就会异步的传递给哪个线程
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        // 从消息对象里面取出一个消息
        Message msg = mServiceHandler.obtainMessage();
        // 将启动Service传入的intent封装成消息发出
        msg.arg1 = startId;
        msg.obj = intent;
        // Handler对象发送这个消息
        mServiceHandler.sendMessage(msg);
    }

    /**
     * You should not override this method for your IntentService. Instead,
     * override {@link #onHandleIntent}, which the system calls when the IntentService
     * receives a start request.
     * @see android.app.Service#onStartCommand
     */
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        // 继而调用的就是这个onStart方法
        onStart(intent, startId);
        // 这个地方判断是否重传Intent
        // START_REDELIVER_INTENT重传Intent。使用这个返回值时，如果在执行完onStartCommand后，
        // 服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
        // 如果不需要重传Intent。则使用START_NOT_STICKY，不再重启服务
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    /**
     * Unless you provide binding for your service, you don't need to implement this
     * method, because the default implementation returns null.
     * @see android.app.Service#onBind
     */
    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(Intent)}.
     *               This may be null if the service is being restarted after
     *               its process has gone away; see
     *               {@link android.app.Service#onStartCommand}
     *               for details.
     */
    @WorkerThread
    protected abstract void onHandleIntent(@Nullable Intent intent);
}

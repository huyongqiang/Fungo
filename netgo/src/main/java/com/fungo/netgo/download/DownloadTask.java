package com.fungo.netgo.download;

import android.content.ContentValues;
import android.text.TextUtils;

import com.fungo.netgo.exception.ApiException;
import com.fungo.netgo.exception.HttpException;
import com.fungo.netgo.exception.StorageException;
import com.fungo.netgo.model.HttpHeaders;
import com.fungo.netgo.progress.Progress;
import com.fungo.netgo.request.base.Request;
import com.fungo.netgo.task.PriorityRunnable;
import com.fungo.netgo.utils.HttpUtils;
import com.fungo.netgo.utils.IOUtils;
import com.fungo.netgo.utils.NetLogger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Pinger
 * @since 18-10-24 上午9:46
 * <p>
 * 执行下载请求的任务
 */
public class DownloadTask implements Runnable {

    private static final int BUFFER_SIZE = 1024 * 8;

    public Progress progress;
    public Map<Object, DownloadListener> listeners;
    private ThreadPoolExecutor executor;
    private PriorityRunnable priorityRunnable;

    public DownloadTask(String tag, Request<File, ? extends Request> request) {
        HttpUtils.checkNotNull(tag, "tag == null");
        progress = new Progress();
        progress.tag = tag;
        progress.folder = NetDownload.getInstance().getFolder();
        progress.url = request.getUrl();
        progress.status = Progress.NONE;
        progress.totalSize = -1;
        progress.request = request;

        executor = NetDownload.getInstance().getThreadPool().getExecutor();
        listeners = new HashMap<>();
    }

    public DownloadTask(Progress progress) {
        HttpUtils.checkNotNull(progress, "progress == null");
        this.progress = progress;
        executor = NetDownload.getInstance().getThreadPool().getExecutor();
        listeners = new HashMap<>();
    }

    public DownloadTask folder(String folder) {
        if (folder != null && !TextUtils.isEmpty(folder.trim())) {
            progress.folder = folder;
        } else {
            NetLogger.w("folder is null, ignored!");
        }
        return this;
    }

    public DownloadTask fileName(String fileName) {
        if (fileName != null && !TextUtils.isEmpty(fileName.trim())) {
            progress.fileName = fileName;
        } else {
            NetLogger.w("fileName is null, ignored!");
        }
        return this;
    }

    public DownloadTask priority(int priority) {
        progress.priority = priority;
        return this;
    }

    public DownloadTask extra1(Serializable extra1) {
        progress.extra1 = extra1;
        return this;
    }

    public DownloadTask extra2(Serializable extra2) {
        progress.extra2 = extra2;
        return this;
    }

    public DownloadTask extra3(Serializable extra3) {
        progress.extra3 = extra3;
        return this;
    }

    public DownloadTask save() {
        if (!TextUtils.isEmpty(progress.folder) && !TextUtils.isEmpty(progress.fileName)) {
            progress.filePath = new File(progress.folder, progress.fileName).getAbsolutePath();
        }
        DownloadManager.getInstance().replace(progress);
        return this;
    }

    public DownloadTask register(DownloadListener listener) {
        if (listener != null) {
            listeners.put(listener.tag, listener);
        }
        return this;
    }

    public void unRegister(DownloadListener listener) {
        HttpUtils.checkNotNull(listener, "listener == null");
        listeners.remove(listener.tag);
    }

    public void unRegister(String tag) {
        HttpUtils.checkNotNull(tag, "tag == null");
        listeners.remove(tag);
    }

    public void start() {
        if (NetDownload.getInstance().getTask(progress.tag) == null || DownloadManager.getInstance().get(progress.tag) == null) {
            throw new IllegalStateException("you must call DownloadTask#save() before DownloadTask#start()！");
        }
        if (progress.status == Progress.NONE || progress.status == Progress.PAUSE || progress.status == Progress.ERROR) {
            postOnStart(progress);
            postWaiting(progress);
            priorityRunnable = new PriorityRunnable(progress.priority, this);
            executor.execute(priorityRunnable);
        } else if (progress.status == Progress.FINISH) {
            if (progress.filePath == null) {
                postOnError(progress, new StorageException("the file of the task with tag:" + progress.tag + " may be invalid or damaged, please call the method restart() to download again！"));
            } else {
                File file = new File(progress.filePath);
                if (file.exists() && file.length() == progress.totalSize) {
                    postOnFinish(progress, new File(progress.filePath));
                } else {
                    postOnError(progress, new StorageException("the file " + progress.filePath + " may be invalid or damaged, please call the method restart() to download again！"));
                }
            }
        } else {
            NetLogger.w("the task with tag " + progress.tag + " is already in the download queue, current task status is " + progress.status);
        }
    }

    public void restart() {
        pause();
        IOUtils.delFileOrFolder(progress.filePath);
        progress.status = Progress.NONE;
        progress.currentSize = 0;
        progress.fraction = 0;
        progress.speed = 0;
        DownloadManager.getInstance().replace(progress);
        start();
    }

    /**
     * 暂停的方法
     */
    public void pause() {
        executor.remove(priorityRunnable);
        if (progress.status == Progress.WAITING) {
            postPause(progress);
        } else if (progress.status == Progress.LOADING) {
            progress.speed = 0;
            progress.status = Progress.PAUSE;
        } else {
            NetLogger.w("only the task with status WAITING(1) or LOADING(2) can pause, current status is " + progress.status);
        }
    }

    /**
     * 删除一个任务,会删除下载文件
     */
    public void remove() {
        remove(false);
    }

    /**
     * 删除一个任务,会删除下载文件
     */
    public DownloadTask remove(boolean isDeleteFile) {
        pause();
        if (isDeleteFile) IOUtils.delFileOrFolder(progress.filePath);
        DownloadManager.getInstance().delete(progress.tag);
        DownloadTask task = NetDownload.getInstance().removeTask(progress.tag);
        postOnRemove(progress);
        return task;
    }

    @Override
    public void run() {
        //check breakpoint
        long startPosition = progress.currentSize;
        if (startPosition < 0) {
            postOnError(progress, ApiException.BREAKPOINT_EXPIRED());
            return;
        }
        if (startPosition > 0) {
            if (!TextUtils.isEmpty(progress.filePath)) {
                File file = new File(progress.filePath);
                if (!file.exists()) {
                    postOnError(progress, ApiException.BREAKPOINT_NOT_EXIST());
                    return;
                }
            }
        }

        //request network from startPosition
        Response response;
        try {
            Request<?, ? extends Request> request = progress.request;
            request.headers(HttpHeaders.HEAD_KEY_RANGE, "bytes=" + startPosition + "-");
            response = request.execute().body();
        } catch (IOException e) {
            postOnError(progress, e);
            return;
        }

        if (response == null) return;

        //check network data
        int code = response.code();
        if (code == 404 || code >= 500) {
            postOnError(progress, HttpException.NET_ERROR());
            return;
        }
        ResponseBody body = response.body();
        if (body == null) {
            postOnError(progress, new HttpException("response body is null"));
            return;
        }
        if (progress.totalSize == -1) {
            progress.totalSize = body.contentLength();
        }

        //create filename
        String fileName = progress.fileName;
        if (TextUtils.isEmpty(fileName)) {
            fileName = HttpUtils.getNetFileName(response, progress.url);
            progress.fileName = fileName;
        }
        if (!IOUtils.createFolder(progress.folder)) {
            postOnError(progress, StorageException.NOT_AVAILABLE());
            return;
        }

        //create and check file
        File file;
        if (TextUtils.isEmpty(progress.filePath)) {
            file = new File(progress.folder, fileName);
            progress.filePath = file.getAbsolutePath();
        } else {
            file = new File(progress.filePath);
        }
        if (startPosition > 0 && !file.exists()) {
            postOnError(progress, ApiException.BREAKPOINT_EXPIRED());
            return;
        }
        if (startPosition > progress.totalSize) {
            postOnError(progress, ApiException.BREAKPOINT_EXPIRED());
            return;
        }
        if (startPosition == 0 && file.exists()) {
            IOUtils.delFileOrFolder(file);
        }
        if (startPosition == progress.totalSize && startPosition > 0) {
            if (file.exists() && startPosition == file.length()) {
                postOnFinish(progress, file);
                return;
            } else {
                postOnError(progress, ApiException.BREAKPOINT_EXPIRED());
                return;
            }
        }

        //start downloading
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(startPosition);
            progress.currentSize = startPosition;
        } catch (Exception e) {
            postOnError(progress, e);
            return;
        }
        try {
            DownloadManager.getInstance().replace(progress);
            download(body.byteStream(), randomAccessFile, progress);
        } catch (IOException e) {
            postOnError(progress, e);
            return;
        }

        //check finish status
        if (progress.status == Progress.PAUSE) {
            postPause(progress);
        } else if (progress.status == Progress.LOADING) {
            if (file.length() == progress.totalSize) {
                postOnFinish(progress, file);
            } else {
                postOnError(progress, ApiException.BREAKPOINT_EXPIRED());
            }
        } else {
            postOnError(progress, ApiException.UNKNOWN());
        }
    }

    /**
     * 执行文件下载
     */
    private void download(InputStream input, RandomAccessFile out, Progress progress) throws IOException {
        if (input == null || out == null) return;

        progress.status = Progress.LOADING;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        int len;
        try {
            while ((len = in.read(buffer, 0, BUFFER_SIZE)) != -1 && progress.status == Progress.LOADING) {
                out.write(buffer, 0, len);

                Progress.changeProgress(progress, len, progress.totalSize, new Progress.Action() {
                    @Override
                    public void call(Progress progress) {
                        postLoading(progress);
                    }
                });
            }
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(input);
        }
    }

    private void postOnStart(final Progress progress) {
        progress.speed = 0;
        progress.status = Progress.NONE;
        updateDatabase(progress);
        HttpUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onStart(progress);
                }
            }
        });
    }

    private void postWaiting(final Progress progress) {
        progress.speed = 0;
        progress.status = Progress.WAITING;
        updateDatabase(progress);
        HttpUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                }
            }
        });
    }

    private void postPause(final Progress progress) {
        progress.speed = 0;
        progress.status = Progress.PAUSE;
        updateDatabase(progress);
        HttpUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                }
            }
        });
    }

    private void postLoading(final Progress progress) {
        updateDatabase(progress);
        HttpUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                }
            }
        });
    }

    private void postOnError(final Progress progress, final Throwable throwable) {
        progress.speed = 0;
        progress.status = Progress.ERROR;
        progress.exception = throwable;
        updateDatabase(progress);
        HttpUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                    listener.onError(progress);
                }
            }
        });
    }

    private void postOnFinish(final Progress progress, final File file) {
        progress.speed = 0;
        progress.fraction = 1.0f;
        progress.status = Progress.FINISH;
        updateDatabase(progress);
        HttpUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                    listener.onFinish(file, progress);
                }
            }
        });
    }

    private void postOnRemove(final Progress progress) {
        updateDatabase(progress);
        HttpUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onRemove(progress);
                }
                listeners.clear();
            }
        });
    }

    private void updateDatabase(Progress progress) {
        ContentValues contentValues = Progress.buildUpdateContentValues(progress);
        DownloadManager.getInstance().update(contentValues, progress.tag);
    }
}
package movingballsfx;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReaderWriterMonitor {

    private int writersActive = 0;
    private int readersActive = 0;
    private int writersWaiting = 0;
    private int readersWaiting = 0;
    private boolean isWriterPriorityOn = false;
    private boolean isReaderPriorityOn = false;

    private Lock lock = new ReentrantLock();
    private Condition writerQ = lock.newCondition();
    private Condition readerQ = lock.newCondition();

    public void enterReader() {
        lock.lock();
        try {
            while (writersActive == 1) {
                readersWaiting++;
                readerQ.await();
                readersWaiting--;
            }
            readersActive++;
        } catch (InterruptedException e) {
            readersWaiting--;
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void exitReader() {
        lock.lock();
        try {
            readersActive--;
            prioritization();
        } finally {
            lock.unlock();
        }
    }

    public void enterWriter() {
        lock.lock();
        try {
            while ((readersActive > 0 || writersActive == 1)) {
                writersWaiting++;
                writerQ.await();
                writersWaiting--;
            }
            writersActive++;
        } catch (InterruptedException e) {
            writersWaiting--;
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void exitWriter() {
        lock.lock();
        try {
            writersActive--;
            prioritization();
        } finally {
            lock.unlock();
        }
    }

    public void setWriterPriorityOn()
    {
        isWriterPriorityOn = !isWriterPriorityOn;
    }

    public void setReaderPriorityOn() {
        isReaderPriorityOn = !isReaderPriorityOn;
    }

    private void prioritization() {
        if (isWriterPriorityOn && writersWaiting > 0) {
            writerQ.signal();
        } else if (isReaderPriorityOn && readersWaiting > 0) {
            readerQ.signalAll();
        } else if (writersWaiting > 0) {
            writerQ.signal();
        } else {
            readerQ.signalAll();
        }
    }
}

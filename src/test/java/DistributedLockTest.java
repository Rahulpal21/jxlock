import com.gmail.rahulpal21.jxlock.DistributedLock;
import com.gmail.rahulpal21.jxlock.DistributedLockFactory;

public class DistributedLockTest {
    public static void main(String[] args) throws InterruptedException {
        DistributedLock lock = DistributedLockFactory.getLock("TestLock");
        lock.lock();
        lock.unlock();
    }
}

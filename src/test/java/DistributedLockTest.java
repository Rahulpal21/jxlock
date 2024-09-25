import org.jxlock.DistributedLock;
import org.jxlock.DistributedLockFactory;

public class DistributedLockTest {
    public static void main(String[] args) throws InterruptedException {
        DistributedLock lock = DistributedLockFactory.getLock("TestLock");
        lock.tryLock();
        lock.unlock();
    }
}

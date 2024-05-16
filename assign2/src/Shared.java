import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Shared {
    private ReentrantLock lock;
    private final Map<String, User> users;

    public Shared() {
        this.lock = new ReentrantLock();
        this.users = new ConcurrentHashMap<>();
    }

    public void addUser(User user) {
        lock.lock();
        try {
            users.put(user.getUsername(), user);
        } finally {
            lock.unlock();
        }
    }

    public User getUserByName(String username) {
        return users.get(username);
    }
}

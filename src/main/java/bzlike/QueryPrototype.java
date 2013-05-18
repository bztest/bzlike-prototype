package bzlike;

import com.google.common.primitives.Longs;

import java.util.*;

/**
 * Прототип. Без синхронизации.
 * update требует знания времени, убирается дополнительным id -> post маппингом
 * Оценки производительности в тесте
 * @author jkee
 */
public class QueryPrototype {

    private TreeSet<Post> requestTree;

    private static final Comparator<Post> timeComparator = new Comparator<Post>() {
        @Override
        public int compare(Post o1, Post o2) {
            int compare = Longs.compare(o1.getPostTime(), o2.getPostTime());
            if (compare == 0) compare = Longs.compare(o1.getPostId(), o2.getPostId());
            return compare;
        }
    };

    public QueryPrototype() {
        this.requestTree = new TreeSet<Post>(timeComparator);
    }

    public void put(Post post) {
        requestTree.add(post);
    }

    public void update(Post old, Post newPost) { //or just some edit info
        requestTree.remove(old);
        requestTree.add(newPost);
    }

    public List<Post> getPostsToDeploy(long currentTime, long thresholdTime) {
        List<Post> deployList = new ArrayList<Post>();
        while(true) {
            Post next = requestTree.pollFirst();
            if (next != null && next.getPostTime() <= currentTime + thresholdTime) {
                deployList.add(next);
                requestTree.remove(next);
            } else {
                if (next != null) requestTree.add(next);
                break;
            }
        }
        return deployList;
    }

    public int querySize() {
        return requestTree.size();
    }

}

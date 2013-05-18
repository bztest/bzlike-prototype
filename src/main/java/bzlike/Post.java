package bzlike;

/**
 * @author jkee
 */
public class Post {

    private final long postId;
    private long postTime;

    private final int dataId; //some data ids
    private final int targetId; //or all required metadata

    public Post(long postId, long postTime, int dataId, int targetId) {
        this.postId = postId;
        this.postTime = postTime;
        this.dataId = dataId;
        this.targetId = targetId;
    }

    public long getPostId() {
        return postId;
    }

    public int getDataId() {
        return dataId;
    }

    public int getTargetId() {
        return targetId;
    }

    public long getPostTime() {
        return postTime;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (postId != post.postId) return false;
        if (postTime != post.postTime) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (postId ^ (postId >>> 32));
        result = 31 * result + (int) (postTime ^ (postTime >>> 32));
        return result;
    }
}

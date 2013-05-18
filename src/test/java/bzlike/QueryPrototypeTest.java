package bzlike;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author jkee
 */
public class QueryPrototypeTest {

    private static final Logger log = LoggerFactory.getLogger(QueryPrototypeTest.class);

    QueryPrototype prototype;
    long baseTime;

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
        BasicConfigurator.configure();
        setUpPrototype();
    }

    private void setUpPrototype() {
        prototype = new QueryPrototype();
        baseTime = new GregorianCalendar(2012, 10, 25, 4, 1).getTimeInMillis();
    }

    @Test
    public void testOrder() throws Exception {


        prototype.put(new Post(0, baseTime + 5000, 1, 1));
        prototype.put(new Post(1, baseTime, 1, 1));
        prototype.put(new Post(2, baseTime - 5000, 1, 1));
        prototype.put(new Post(3, baseTime + 10000, 1, 1));

        List<Post> toDeploy = prototype.getPostsToDeploy(baseTime, 5500);
        //so, ids 0, 1 and 2 should be deployed in order 2, 1, 0

        assertEquals(3, toDeploy.size());
        assertEquals(2, toDeploy.get(0).getPostId());
        assertEquals(1, toDeploy.get(1).getPostId());
        assertEquals(0, toDeploy.get(2).getPostId());
    }

    @Test
    public void testEdit() throws Exception {
        prototype.put(new Post(0, baseTime + 5000, 1, 1));
        prototype.put(new Post(1, baseTime, 1, 1));
        prototype.put(new Post(2, baseTime - 5000, 1, 1));
        prototype.put(new Post(3, baseTime + 10000, 1, 1));

        prototype.update(new Post(1, baseTime, 1, 1), new Post(1, baseTime + 11000, 1, 1));
        prototype.update(new Post(0, baseTime + 5000, 1, 1), new Post(0, baseTime - 5500, 1, 1));

        List<Post> toDeploy = prototype.getPostsToDeploy(baseTime, 5500);
        //ids 0 and 2 should come in order 0, 2

        assertEquals(2, toDeploy.size());
        assertEquals(0, toDeploy.get(0).getPostId());
        assertEquals(2, toDeploy.get(1).getPostId());
    }

    //not a test, just some performance checking
    public void testPerformance(int postNum) throws Exception {
        Random random = new Random(4242);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < postNum; i++) {
            //from baseTime to baseTime + one day
            long time = baseTime + 1000L * random.nextInt((int)TimeUnit.DAYS.toSeconds(1));
            Post post = new Post(i, time, 1, 1);
            prototype.put(post);
        }
        log.info("Init time: " + (System.currentTimeMillis() - startTime) / 1000.0);
        //about 800 mb for 10m posts
        log.info("Memory usage: " +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + " mb");

        //ok, taking minute deploys
        startTime = System.currentTimeMillis();
        long threshold = TimeUnit.MINUTES.toMillis(1);
        long timeNow = baseTime;
        long deployed = 0;
        long acc = 0; //prevent method call elimination
        for (int i = 0; i < TimeUnit.DAYS.toMinutes(1); i++) {
            List<Post> toDeploy = prototype.getPostsToDeploy(timeNow, threshold);
            deployed += toDeploy.size();
            for (Post post : toDeploy) {
                acc += post.getPostId();
            }
            timeNow += threshold;
        }
        log.info("Posts deployed, time: " + (System.currentTimeMillis() - startTime) / 1000.0);
        log.info("deployed: " + deployed);
        log.info("acc: " + acc); //это же сумма арифметической прогрессии!

    }

    public static void main(String[] args) throws Exception {
        int postNum = 10000000; //10m posts
        int warmUpPosts = 100000;

        QueryPrototypeTest warmUpTest = new QueryPrototypeTest();
        warmUpTest.setUp();
        warmUpTest.testPerformance(warmUpPosts);

        QueryPrototypeTest queryPrototypeTest = new QueryPrototypeTest();
        queryPrototypeTest.setUpPrototype();
        queryPrototypeTest.testPerformance(postNum);

        /*
0 [main] INFO bzlike.QueryPrototypeTest  - Init time: 0.103
2 [main] INFO bzlike.QueryPrototypeTest  - Memory usage: 21 mb
96 [main] INFO bzlike.QueryPrototypeTest  - Posts deployed, time: 0.094
96 [main] INFO bzlike.QueryPrototypeTest  - deployed: 100000
96 [main] INFO bzlike.QueryPrototypeTest  - acc: 4999950000
33243 [main] INFO bzlike.QueryPrototypeTest  - Init time: 33.147
33243 [main] INFO bzlike.QueryPrototypeTest  - Memory usage: 801 mb
35830 [main] INFO bzlike.QueryPrototypeTest  - Posts deployed, time: 2.587
35830 [main] INFO bzlike.QueryPrototypeTest  - deployed: 10000000
35830 [main] INFO bzlike.QueryPrototypeTest  - acc: 49999995000000
        *
        * */
    }

}

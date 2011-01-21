import controllers.Tasks;
import controllers.utils.TaskIndex;
import models.Task;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Jan 21, 2011
 */
public class TasksControllerTest extends UnitTest {
    @Before
    public void setup() throws Exception {
        // test.yml is already loaded by Bootstrap.java
        TaskIndex.rebuildIndex();
    }

    @Test
    public void testFilter_noTagInGroup() throws Exception {
        // find all tasks with no tags in group 1
        List<Task> matchingTasks = Tasks.filterTasks (null, null, new String[]{"1"}, null, null, null);

        assertEquals(2, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 3);
        assertTrue(matchingTasks.get(1).id == 4);

        // find all tasks with no tags in group 2
        matchingTasks = Tasks.filterTasks (null, null, new String[]{"2"}, null, null, null);

        assertEquals(5, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 1);
        assertTrue(matchingTasks.get(1).id == 3);
        assertTrue(matchingTasks.get(2).id == 4);
        assertTrue(matchingTasks.get(3).id == 5);
        assertTrue(matchingTasks.get(4).id == 6);
    }

    @Test
    public void testFilter_checkedTags() throws Exception {
        // find all tasks with tag1
        List<Task> matchingTasks = Tasks.filterTasks (new String[]{"1"}, null, null, null, null, null);

        assertEquals(3, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 1);
        assertTrue(matchingTasks.get(1).id == 2);
        assertTrue(matchingTasks.get(2).id == 6);
    }

    @Test
    public void testFilter_checkedTags_twoInSameGroup() throws Exception {
        // find all tasks with tag1 OR tag2
        List<Task> matchingTasks = Tasks.filterTasks (new String[]{"1", "2"}, null, null, null, null, null);

        assertEquals(4, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 1);
        assertTrue(matchingTasks.get(1).id == 2);
        assertTrue(matchingTasks.get(2).id == 5);
        assertTrue(matchingTasks.get(3).id == 6);
    }

    @Test
    public void testFilter_checkedTags_twoInDifferentGroups() throws Exception {
        // find all tasks with tag1 AND tag13 (since they are in different groups)
        List<Task> matchingTasks = Tasks.filterTasks (new String[]{"1", "13"}, null, null, null, null, null);

        assertTrue(matchingTasks.get(0).id == 6);
    }

    @Test
    public void testFilter_checkedTags_twoInDifferentGroups_twoInSameGroup() throws Exception {
        // find all tasks with tag1 AND tag13 (since they are in different groups)
        List<Task> matchingTasks = Tasks.filterTasks (new String[]{"1", "2", "13"}, null, null, null, null, null);

        assertEquals(1, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 6);
    }

    @Test
    public void testFilter_searchText() throws Exception {
        // find all tasks with "lucene" in the content
        List<Task> matchingTasks = Tasks.filterTasks (null, "lucene", null, null, null, null);

        assertEquals(1, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 4);
    }

    @Test
    public void testFilter_searchText_checkedTags_false() throws Exception {
        // find all tasks with "lucene" in the content And with tag1
        List<Task> matchingTasks = Tasks.filterTasks (new String[]{"1"}, "lucene", null, null, null, null);

        assertEquals(0, matchingTasks.size());
    }

    @Test
    public void testFilter_searchText_checkedTags_true() throws Exception {
        // find all tasks with "lucene" in the content And with tag1
        List<Task> matchingTasks = Tasks.filterTasks (new String[]{"13"}, "lucene", null, null, null, null);

        assertEquals(1, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 4);
    }

    @Test
    public void testFilter_searchText_OR() throws Exception {
        // find all tasks with "lucene" OR "group" in the content
        List<Task> matchingTasks = Tasks.filterTasks (null, "lucene group", null, null, null, null);

        assertEquals(2, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 4);
        assertTrue(matchingTasks.get(1).id == 5);
    }

    @Test
    public void testFilter_searchText_AND() throws Exception {
        // find all tasks with "lucene" AND "group" in the content
        List<Task> matchingTasks = Tasks.filterTasks (null, "lucene AND group", null, null, null, null);

        assertEquals(0, matchingTasks.size());
    }

    @Test
    public void testFilter_raisedBy() throws Exception {
        // find all tasks owned by member1
        List<Task> matchingTasks = Tasks.filterTasks (null, null, null, new String[]{"1"}, null, null);

        assertEquals(2, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 1);
        assertTrue(matchingTasks.get(1).id == 2);
    }

    @Test
    public void testFilter_raisedBy_checkedTags() throws Exception {
        // find all tasks owned by member1 and has tag1
        List<Task> matchingTasks = Tasks.filterTasks (new String[]{"1"}, null, null, new String[]{"1"}, null, null);

        assertEquals(2, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 1);
        assertTrue(matchingTasks.get(1).id == 2);

        // find all tasks owned by member1 and has tag4
        matchingTasks = Tasks.filterTasks (new String[]{"4"}, null, null, new String[]{"1"}, null, null);

        assertEquals(1, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 2);
    }

    @Test
    public void testFilter_allYouCanEat() throws Exception {
        // find all tasks that match: has tag1, has "lucene" in content, OR does not have tags in group1, created by Member2
        List<Task> matchingTasks = Tasks.filterTasks (new String[]{"1"}, "lucene", new String[]{"1"}, new String[]{"2"}, null, null);

        assertEquals(1, matchingTasks.size());
        assertTrue(matchingTasks.get(0).id == 4);
    }
}

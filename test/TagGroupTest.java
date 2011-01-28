import models.Tag;
import models.TagGroup;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Jan 28, 2011
 */
public class TagGroupTest extends UnitTest {
    @Test
    public void testGetDefaultTags() throws Exception {
        Fixtures.deleteAll();
        Fixtures.load("../data/test.yml");

        assertSame(2, TagGroup.findAll().size());

        assertSame(0, TagGroup.getDefaultTags().size());

        TagGroup group1 = TagGroup.find("byName", "group1").first();
        TagGroup group2 = TagGroup.find("byName", "group2").first();

        Tag tag1 = Tag.find("byName", "tag1InGroup1").first();
        Tag tag2 = Tag.find("byName", "tag2InGroup1").first();
        Tag tag4 = Tag.find("byName", "tag4InGroup2").first();

        group1.setDefault(tag1);
        group2.setDefault(tag4);

        assertSame(2, TagGroup.getDefaultTags().size());


    }
}

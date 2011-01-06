package controllers.admin;

import controllers.CRUD;
import controllers.Secure;
import controllers.utils.TaskIndex;
import models.Task;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import play.mvc.With;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
@With(Secure.class)
@CRUD.For(Task.class)
public class Tasks extends CRUD {
    public static void reindex() throws Exception {
        IndexWriter writer = null;
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
            writer = new IndexWriter(FSDirectory.open(new File(TaskIndex.INDEX_PATH)), analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

            List<Task> tasks = Task.findAll();
            for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext();) {
                Task task = iterator.next();

                TaskIndex.addTaskToIndex(task, writer);
            }
        } finally {
            if (writer != null) {
                writer.optimize();
                writer.close();
            }
        }

        render();
    }
}

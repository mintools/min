package models;

import com.mortennobel.imagescaling.ResampleOp;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFImageWriter;
import org.hibernate.envers.Audited;
import play.Play;
import play.db.jpa.Model;

import javax.imageio.ImageIO;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@Entity
@Audited
public class Attachment extends Model {
    private static final String FILES_DIR = Play.configuration.getProperty("fileStorage.location");
    
    public Date createdDate;
    public String name; // UUID
    public String filename; // stored filename, ie. UUID.doc
    public String title; // original filename
    public String type;

    @ManyToOne(optional = false)
    public Task task;

    public static Attachment createAttachment(File file) throws IOException {
        // Destination directory
        File dir = new File(FILES_DIR);

        String filename = file.getName();
        String extension = filename.substring(filename.lastIndexOf('.') + 1);

        // generate a filename
        String uuid = UUID.randomUUID().toString();

        // attach it to task
        Attachment attachment = new Attachment();

        attachment.title = filename;
        attachment.name = uuid;
        attachment.createdDate = new Date();
        attachment.filename = attachment.name + "." + extension;

        // check if the file is an image
        if ("png".equalsIgnoreCase(extension) ||
                "gif".equalsIgnoreCase(extension) ||
                "jpg".equalsIgnoreCase(extension)) {

            attachment.type = "image";

            // read the image
            BufferedImage srcImage = ImageIO.read(file);

            // scale to thumbnail
            ResampleOp resampleOp = new ResampleOp(100, 100);
            BufferedImage thumbnail = resampleOp.filter(srcImage, null);

            // write the thumbnail
            ImageIO.write(thumbnail, "png", new File(dir, "thumbnail_" + uuid + ".png"));
        }
        else if ("pdf".equalsIgnoreCase(extension)) {
            attachment.type = "pdf";

            PDDocument document = PDDocument.load(file);

            // ignore encrypted pdfs
            if (!document.isEncrypted()) {
                List pages = document.getDocumentCatalog().getAllPages();

                PDPage firstPage = (PDPage) pages.get(0);
                BufferedImage image = firstPage.convertToImage(BufferedImage.TYPE_INT_RGB, 96);

                // scale to thumbnail
                ResampleOp resampleOp = new ResampleOp(100, 100);
                BufferedImage thumbnail = resampleOp.filter(image, null);

                // write the thumbnail
                ImageIO.write(thumbnail, "png", new File(dir, "thumbnail_" + uuid + ".png"));
            }
        }

        // todo: is there a better way?
        FileInputStream in = new FileInputStream(file);
        File outFile = new File(dir, uuid + "." + extension);

        FileOutputStream out = new FileOutputStream(outFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return attachment;
    }

    public String toString() {
        return title;
    }
}

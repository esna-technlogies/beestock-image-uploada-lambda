package beesstock.uploads.processor.services;

import beesstock.uploads.processor.model.Response;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PhotosHandler implements IHandler {

    static final String[] supportedTypes = {
            "jpg",
            "png",
            "tif",
    };

    private static final float MAX_WIDTH = 100;
    private static final float MAX_HEIGHT = 100;
    private final String JPG_TYPE = (String) "jpg";
    private final String JPG_MIME = (String) "image/jpeg";
    private final String PNG_TYPE = (String) "png";
    private final String PNG_MIME = (String) "image/png";
    private final String TIF_TYPE = (String) "tif";
    private final String TIF_MIME = (String) "image/tiff";

    public PhotosHandler() {}

    public Response handle(FileAnalyzer file) throws IOException {

        String dstBucket = file.getBucketName() ;
        String dstKey = file.getFileNameWithExtension() + "_resized";

        // Infer the image type.
        Matcher matcher = Pattern.compile(".*\\.([^\\.]*)").matcher(file.getFileNameWithExtension());
        if (!matcher.matches()) {
            String error = file.handleError( "Unable to infer image type for key "+ file.getFileNameWithExtension());
            return (new Response()).setMessage(error);
        }

        String imageType = matcher.group(1);
        if (!(JPG_TYPE.equals(imageType)) && !(PNG_TYPE.equals(imageType)) && !(TIF_TYPE.equals(imageType))) {

            String result = "Skipping non-image " + file.getFileNameWithExtension();
            System.out.println(result);
        }

        // Download the image from S3 into a stream
        AmazonS3 s3Client = new AmazonS3Client();
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(file.getBucketName(), file.getFileNameWithExtension()));
        InputStream objectData = s3Object.getObjectContent();

        // Read the source image
        BufferedImage srcImage = ImageIO.read(objectData);
        int srcHeight = srcImage.getHeight();
        int srcWidth = srcImage.getWidth();

        // Infer the scaling factor to avoid stretching the image
        float scalingFactor = Math.min(MAX_WIDTH / srcWidth, MAX_HEIGHT/ srcHeight);
        int width = (int) (scalingFactor * srcWidth);
        int height = (int) (scalingFactor * srcHeight);

        BufferedImage resizedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        // Fill with white before applying semi-transparent (alpha) images
        g.setPaint(Color.white);
        g.fillRect(0, 0, width, height);
        // Simple bilinear resize
        // If you want higher quality algorithms, check this link:
        // https://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(srcImage, 0, 0, width, height, null);
        g.dispose();

        // Re-encode image to target format
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, imageType, os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        // Set Content-Length and Content-Type
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(os.size());
        if (JPG_TYPE.equals(imageType)) {
            meta.setContentType(JPG_MIME);
        }
        if (PNG_TYPE.equals(imageType)) {
            meta.setContentType(PNG_MIME);
        }

        // Uploading to S3 destination bucket
        System.out.println("Writing to: " + dstBucket + "/" + dstKey);
        s3Client.putObject(dstBucket, dstKey, is, meta);
        System.out.println("Successfully resized " + file.getBucketName() + "/"
                + file.getFileNameWithExtension() + " and uploaded to " + dstBucket + "/" + dstKey);

        return (new Response()).setMessage("All Ok !");
    }
}

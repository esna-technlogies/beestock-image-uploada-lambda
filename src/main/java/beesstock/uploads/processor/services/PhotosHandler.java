package beesstock.uploads.processor.services;

import beesstock.uploads.processor.model.Response;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotosHandler implements IHandler {

    static final String[] supportedTypes = {
            "jpg",
            "png",
            "tif",
    };

    private final String JPG_TYPE = (String) "jpg";
    private final String JPG_MIME = (String) "image/jpeg";
    private final String PNG_TYPE = (String) "png";
    private final String PNG_MIME = (String) "image/png";
    private final String TIF_TYPE = (String) "tif";
    private final String TIF_MIME = (String) "image/tiff";

    public Response handle(FileAnalyzer file) throws IOException {

        String dstBucket = "beesstock-unapproved-files" ;

        String waterMarkUrl = "https://s3-us-west-2.amazonaws.com/beesstock-photos/standard-photos/watermark.png";

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

            return new Response().setMessage(result);
        }


        // Download the image from S3 into a stream
        AmazonS3 s3Client = new AmazonS3Client();
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(file.getBucketName(), file.getFileNameWithExtensionUTF()));
        InputStream objectData = s3Object.getObjectContent();


        // Read the source image
        BufferedImage srcImage = ImageIO.read(objectData);
        int srcHeight = srcImage.getHeight();
        int srcWidth = srcImage.getWidth();


        String original = file.getFileOwnerUUID() + "/" + file.getFileID() + "/original."+file.getFileExtension();
        s3Client.copyObject(file.getBucketName(), file.getFileNameWithExtensionUTF(), dstBucket, original);

        Integer[] sizes = { 250, 500 , 750, 1000};

        // watermark image
        URL url = new URL(waterMarkUrl);
        BufferedImage watermarkImage = ImageIO.read(url);

        for (Integer size : sizes){

            // Infer the scaling factor to avoid stretching the image
            float scalingFactor = Math.min( (float) size / srcWidth, (float) size/ srcHeight);
            int width = (int) (scalingFactor * srcWidth);
            int height = (int) (scalingFactor * srcHeight);

            BufferedImage resizedImage = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();

            // Fill with white before applying semi-transparent (alpha) images
            g.setPaint(Color.white);
            g.fillRect(0, 0, width, height);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(srcImage, 0, 0, width, height, null);
            g.dispose();

            if (size > 250) {
                float alpha =  0.6f;
                resizedImage = addImageWatermark(watermarkImage, resizedImage,  alpha);
            }

            // Re-encode image to target format
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, imageType, os);
            InputStream inputStream = new ByteArrayInputStream(os.toByteArray());

            // Set Content-Length and Content-Type
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(os.size());

            String mimeType ="";
            if (JPG_TYPE.equals(imageType)) {
                mimeType = JPG_MIME;
            }
            if (PNG_TYPE.equals(imageType)) {
                mimeType = PNG_MIME;
            }
            if (TIF_TYPE.equals(imageType)) {
                mimeType = TIF_MIME;
            }
            meta.setContentType(mimeType);

            String dstKey = file.getFileOwnerUUID() + "/" + file.getFileID() + "/" +size+"."+file.getFileExtension();
            s3Client.putObject(dstBucket, dstKey, inputStream, meta);

        }

//        String   _sizes         = String.join(",", new String[]{"250", "500", "750", "1000"});
//        String   _extensions     = file.getFileExtension();
//        String   _originalFile  = original;
//        String   _fileId        = file.getFileID();
//        String   _user          = file.getFileOwnerUUID() ;
//        String   _bucket        = dstBucket ;
//
//        new FileStorage(_sizes, _extensions, _originalFile, _fileId, _user, _bucket);

        return (new Response()).setMessage("All Ok !");
    }

    private static BufferedImage addImageWatermark(BufferedImage watermarkImage, BufferedImage sourceImage, float alpha) {

            Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(alphaChannel);

            // calculates the coordinate where the image is painted

            int topLeftX = (sourceImage.getWidth() - watermarkImage.getWidth()) / 2;
            int topLeftY = (sourceImage.getHeight() - watermarkImage.getHeight()) / 2;

            // paints the image watermark

            g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);
            g2d.dispose();

            return sourceImage;
    }
}

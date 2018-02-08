package beesstock.uploads.processor.services;

import ch.qos.logback.core.util.FileUtil;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

@Service
public class FileAnalyzer {

    private String bucketName = "";

    private String fileName = "";

    private String fileNameWithExtension = "";

    private String fileNameWithExtensionUTF = "";

    private String fileExtension = "";

    private boolean isValidFile ;

    private final String errorFile = "error.report";

    public void initialize(S3Event s3event) throws UnsupportedEncodingException {

        S3EventNotification.S3EventNotificationRecord record = s3event.getRecords().get(0);

        this.bucketName = record.getS3().getBucket().getName();

        // Object key may have spaces or unicode non-ASCII characters.
        this.fileNameWithExtension = record.getS3().getObject().getKey();

        String fixesFileNAme = this.fileNameWithExtension.replace('+', ' ');
        this.fileNameWithExtensionUTF = URLDecoder.decode(fixesFileNAme, "UTF-8");

        // checking file extension
        this.fileExtension = FilenameUtils.getExtension(this.fileNameWithExtension);
        this.fileName = this.fileNameWithExtension.substring(
                0, this.fileNameWithExtension.length() - (this.fileExtension.length()+1));

        // check if the file is valid
        String[] supportedTypes = ArrayUtils.addAll(PhotosHandler.supportedTypes, VectorHandler.supportedTypes);
        supportedTypes = ArrayUtils.addAll(supportedTypes, FootageHandler.supportedTypes);

        List<String> list = Arrays.asList(supportedTypes);

        this.isValidFile =  list.contains(this.fileExtension.trim().toLowerCase());
    }

    public boolean isPhoto(){
        return Arrays.asList(PhotosHandler.supportedTypes).contains(this.fileExtension);
    }

    public boolean isFootage(){
        return Arrays.asList(FootageHandler.supportedTypes).contains(this.fileExtension);
    }

    public boolean isVector(){
        return Arrays.asList(VectorHandler.supportedTypes).contains(this.fileExtension);
    }

    public String getFileNameWithExtension(){
        return this.fileNameWithExtension;
    }

    public boolean isValidFile() {
        return this.isValidFile;
    }

    public String getBucketName() {
        return bucketName;
    }


    public String getFileExtension() {
        return fileExtension;
    }

    public String getErrorFile() {
        return errorFile;
    }

    public String getFileNameWithExtensionUTF() {
        return fileNameWithExtensionUTF;
    }

    public String getFileName(){
        return this.fileName;
    }

    public String handleError(String error) {
        System.out.println(error);
        return error;
    }
}

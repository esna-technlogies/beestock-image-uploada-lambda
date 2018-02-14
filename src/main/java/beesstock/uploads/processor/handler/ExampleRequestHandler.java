package beesstock.uploads.processor.handler;

import beesstock.uploads.processor.services.FootageHandler;
import beesstock.uploads.processor.services.PhotosHandler;
import beesstock.uploads.processor.services.FileAnalyzer;
import beesstock.uploads.processor.services.VectorHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

import beesstock.uploads.processor.ApplicationConfiguration;
import beesstock.uploads.processor.model.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Our Lambda function's main logic takes place here, while we
 * leverage Spring's dependency injection features to inject the
 * services we need at construction time.
 *
 * This class is declared as a bean using the {@link Component}
 * annotation. You could also just as easily register it in the
 * {@link ApplicationConfiguration} class, or
 * in a Spring application configuration XML file.
 *
 * @author Chris Campo
 */
@Component
public class ExampleRequestHandler implements RequestHandler<S3Event, Response> {

    private FileAnalyzer file = new FileAnalyzer();

    private PhotosHandler photosHandler = new PhotosHandler();

    private FootageHandler footageHandler = new FootageHandler();

    private VectorHandler vectorHandler = new VectorHandler();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Response handleRequest(S3Event s3event, final Context context) {

        final Response response = new Response();

        try {

            file.initialize(s3event);

            System.out.println("BucketName "+ file.getBucketName());
            System.out.println("FileExtension "+ file.getFileExtension());
            System.out.println("FileName "+ file.getFileName());
            System.out.println("NameWithExtension "+ file.getFileNameWithExtension());
            System.out.println("NameWithExtensionUTF "+ file.getFileNameWithExtensionUTF());
            System.out.println("ErrorFile "+ file.getErrorFile());

            if (!file.isValidFile()){
                String error = file.handleError("Invalid fie type");
                return response.setMessage(error);
            }

            if (file.isPhoto()){
                photosHandler.handle(file);
            }

            if (file.isFootage()){
                footageHandler.handle(file);
            }

            if (file.isVector()){
                vectorHandler.handle(file);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        response.setMessage("All OK");
        response.setStatus(Response.Status.OK);
        return response;
    }

}

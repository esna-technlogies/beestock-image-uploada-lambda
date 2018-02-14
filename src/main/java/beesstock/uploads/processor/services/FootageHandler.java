package beesstock.uploads.processor.services;

import beesstock.uploads.processor.model.Response;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import org.springframework.stereotype.Service;

@Service
public class FootageHandler implements IHandler {

    static final String[] supportedTypes = {
            "mp4",
            "wmv",
    };

    public Response handle(FileAnalyzer file) {
        return null;
    }
}

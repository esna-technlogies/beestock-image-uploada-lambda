package beesstock.uploads.processor.services;

import beesstock.uploads.processor.model.Response;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import org.springframework.stereotype.Service;

@Service
public class VectorHandler implements IHandler {

//    .psd  Adobe Photoshop file
//    .ai   Adobe Illustrator
//    .cdr	CorelDRAW - Only 100% vector
//    .emf	Enhanced Metafile
//    .eps	Encapsulated Postscript
//    .fxg	Adobe Flash XML graphics
//    .pdf	Portable Document Format
//    .ps	Postscript
//    .swf	Macromedia Flash
//    .svg	Scalable Vector Graphics
//    .wmf	Windows Metafile

    static final String[] supportedTypes = {
            "psd",
            "ai",
            "cdr",
            "emf",
            "eps",
            "fxg",
            "pdf",
            "ps",
            "swf",
            "svg",
            "wmf"
    };

    public VectorHandler() {}

    public Response handle(FileAnalyzer file) {
        return null;
    }
}

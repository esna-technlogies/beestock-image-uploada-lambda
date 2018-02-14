package beesstock.uploads.processor.services;

import beesstock.uploads.processor.model.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public interface IHandler {

    Response handle(FileAnalyzer file) throws IOException;
}

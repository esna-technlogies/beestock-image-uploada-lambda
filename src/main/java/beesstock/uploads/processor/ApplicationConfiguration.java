package beesstock.uploads.processor;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Declare all your beans here, or use the annotation driven approach
 * with {@link org.springframework.context.annotation.ComponentScan}.
 *
 * @author Chris Campo
 */
@Configuration
@ComponentScan({"beesstock.uploads.processor.*"})
public class ApplicationConfiguration {

}

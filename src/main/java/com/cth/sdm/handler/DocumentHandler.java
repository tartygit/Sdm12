package com.cth.sdm.handler;

import java.io.File;
import java.util.Map;

/**
 * Interface representing extensible document handlers.
 * Any class that implements this interface can be registered as a handler
 * to parse and process custom document types.
 */
public interface DocumentHandler {

    /**
     * Supports files ending with specific extension (e.g., .xlsx, .docx, .xml)
     */
    boolean supports(String fileExtension);

    /**
     * Parses the given document file and returns a map of Sections/Tabs and their textual contents.
     */
    Map<String, String> handle(File file) throws Exception;
}

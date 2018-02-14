package beesstock.uploads.processor.services;


import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

class FileStorage {

    public static String sizes= "";
    public static String extensions;
    public static String originalFile;
    public static String fileId;
    public static String user;
    public static String bucket;

    FileStorage(
            String sizes,
            String extensions,
            String originalFile,
            String fileId,
            String user,
            String bucket
    ) throws IOException {

        if (!Objects.equals(FileStorage.sizes, "")){
            return;
        }

        FileStorage.sizes = sizes;

        try{
            URL url = new URL("http://api.beesstock.com/api/photo-service/file/storage/file");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject obj = new JSONObject();

            obj.put("sizes", sizes);
            obj.put("extensions", extensions);
            obj.put("originalFile", originalFile);
            obj.put("fileId", fileId);
            obj.put("user", user);
            obj.put("bucket", bucket);

            StringWriter out = new StringWriter();
            obj.writeJSONString(out);

            String jsonText = out.toString();
            OutputStream os = conn.getOutputStream();
            os.write(jsonText.getBytes());
            os.flush();

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            conn.disconnect();

        }catch (Error ignored){

        }
    }

}

package gov.nist.csd.pm.demos.cloud;

import com.google.gson.Gson;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.csd.pm.pep.requests.UpdateContentRequest;
import gov.nist.csd.pm.pep.services.AnalyticsService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

import static gov.nist.csd.pm.model.Constants.*;
import static gov.nist.csd.pm.model.Constants.PATH_PROPERTY;

public class ContentHelper {

    private static AnalyticsService analyticsService = new AnalyticsService();

    public static String getNodeContents(Node user, long process, long id, Node node) throws PropertyNotFoundException, IOException, NodeNotFoundException, SQLException, InvalidProhibitionSubjectTypeException, DatabaseException, InvalidPropertyException, NoSubjectParameterException, ClassNotFoundException, ConfigurationException, MissingPermissionException {
        analyticsService.checkPermissions(user, process, id, FILE_READ);

        String content = null;

        if(node.hasProperty(STORAGE_PROPERTY)) {
            //get contents
            //1 determine where it is stored - storage_location=gcs or aws or local
            Property storageProperty = node.getProperty(STORAGE_PROPERTY);

            //2 get the path
            if(node.hasProperty(PATH_PROPERTY)) {
                switch (storageProperty.getValue()) {
                    case GCS_STORAGE:
                        content = getGCSContents(node);
                        System.out.println(content);
                        break;
                    case AWS_STORAGE:
                        content = getAWSContent(node);
                        break;
                    case LOCAL_STORAGE:
                        content = getLocalContents(node);
                        break;
                }
            }
        }

        return content;
    }

    private static String getLocalContents(Node node) {
        return null;
    }

    private static String getAWSContent(Node node) throws PropertyNotFoundException {
        String awsURI = "http://localhost:8082/aws/buckets/objects";
        Client client = ClientBuilder.newClient();
        return client
                .target(awsURI)
                .queryParam("path", node.getProperty(PATH_PROPERTY).getValue())
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
    }

    private static String getGCSContents(Node node) throws PropertyNotFoundException {
        String gcsURI = "http://localhost:8084/gcs/buckets/objects";
        Client client = ClientBuilder.newClient();
        return client
                .target(gcsURI)
                .queryParam("path", node.getProperty(PATH_PROPERTY).getValue())
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
    }

    public static ImportFile updateNodeContents(Node user, long process, Node node, String content) throws PropertyNotFoundException, IOException, NodeNotFoundException, SQLException, InvalidProhibitionSubjectTypeException, DatabaseException, InvalidPropertyException, NoSubjectParameterException, ClassNotFoundException, ConfigurationException, MissingPermissionException {
        analyticsService.checkPermissions(user, process, node.getId(), FILE_WRITE);

        //get contents
        //1 determine where it is stored - storage_location=gcs or aws or local
        Property storageProperty = node.getProperty(STORAGE_PROPERTY);

        //2 get the path
        switch (storageProperty.getValue()) {
            case GCS_STORAGE:
                return updateGCSContent(node.getProperty(PATH_PROPERTY).getValue(), content);
            case AWS_STORAGE:
                return updateAWSContent(node.getProperty(PATH_PROPERTY).getValue(), content);
            case LOCAL_STORAGE:
                return updateLocalContent(node.getProperty(PATH_PROPERTY).getValue(), content);
        }

        return null;
    }

    private static ImportFile updateLocalContent(String path, String content) {
        return null;
    }

    private static ImportFile updateAWSContent(String path, String content) throws IOException {
        String awsURI = "http://localhost:8082/aws/buckets/objects";
        URL url = new URL(awsURI);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type",
                "application/json");

        //create json request string
        Gson gson = new Gson();
        String json = gson.toJson(new UpdateContentRequest(path, content));
        connection.setRequestProperty("Content-Length",
                Integer.toString(json.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream());
        wr.writeBytes(json);
        wr.close();

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();

        json = response.toString();
        return new Gson().fromJson(json, ImportFile.class);
    }

    private static ImportFile updateGCSContent(String path, String content) throws IOException {
        String gcsURI = "http://localhost:8084/gcs/buckets/objects";
        URL url = new URL(gcsURI);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type",
                "application/json");

        //create json request string
        Gson gson = new Gson();
        String json = gson.toJson(new UpdateContentRequest(path, content));
        connection.setRequestProperty("Content-Length",
                Integer.toString(json.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream());
        wr.writeBytes(json);
        wr.close();

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();

        json = response.toString();
        return new Gson().fromJson(json, ImportFile.class);
    }
    public static ImportFile createNodeContents(Node user, long process, Node node, String content) throws PropertyNotFoundException, IOException, NodeNotFoundException, SQLException, InvalidProhibitionSubjectTypeException, DatabaseException, InvalidPropertyException, NoSubjectParameterException, ClassNotFoundException, ConfigurationException, MissingPermissionException {
        analyticsService.checkPermissions(user, process, node.getId(), FILE_WRITE);

        //get contents
        //1 determine where it is stored - storage_location=gcs or aws or local
        Property storageProperty = node.getProperty(STORAGE_PROPERTY);

        switch (storageProperty.getValue()) {
            case GCS_STORAGE:
                return createGCSContent(node.getProperty(PATH_PROPERTY).getValue(), content);
            case AWS_STORAGE:
                return createAWSContent(node.getProperty(PATH_PROPERTY).getValue(), content);
            case LOCAL_STORAGE:
                return createLocalContent(node.getProperty(PATH_PROPERTY).getValue(), content);
        }

        return null;
    }

    private static ImportFile createLocalContent(String path, String content) {
        return null;
    }

    private static ImportFile createAWSContent(String path, String content) throws IOException {
        String awsURI = "http://localhost:8082/aws/buckets/objects";
        URL url = new URL(awsURI);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/json");

        //create json request string
        Gson gson = new Gson();
        String json = gson.toJson(new UpdateContentRequest(path, content));
        connection.setRequestProperty("Content-Length",
                Integer.toString(json.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream());
        wr.writeBytes(json);
        wr.close();

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();

        json = response.toString();
        return new Gson().fromJson(json, ImportFile.class);
    }

    private static ImportFile createGCSContent(String path, String content) throws IOException {
        String gcsURI = "http://localhost:8084/gcs/buckets/objects";
        URL url = new URL(gcsURI);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/json");

        //create json request string
        Gson gson = new Gson();
        String json = gson.toJson(new UpdateContentRequest(path, content));
        connection.setRequestProperty("Content-Length",
                Integer.toString(json.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream());
        wr.writeBytes(json);
        wr.close();

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();

        json = response.toString();
        return new Gson().fromJson(json, ImportFile.class);
    }

}

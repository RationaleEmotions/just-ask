package com.rationaleemotions.servlets;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rationaleemotions.config.ConfigReader;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openqa.grid.common.exception.GridConfigurationException;
import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.web.servlet.RegistryBasedServlet;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Represents a simple servlet that needs to be invoked in order to wire in our ghost node
 * which will act as a proxy for all proxies.
 */
public class EnrollServlet extends RegistryBasedServlet {
    private static String hubHost;

    public EnrollServlet(Registry registry) {
        super(registry);
    }

    public EnrollServlet() {
        this(null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addProxy();
    }

    private void addProxy() {
        //After the construction is finished, lets wrap up.
        int status;

        HttpClientFactory httpClientFactory = new HttpClientFactory();
        try {
            final int port = getRegistry().getHub().getConfiguration().port;
            hubHost = getRegistry().getHub().getConfiguration().host;
            final URL registration = new URL(String.format("http://%s:%d/grid/register", hubHost, port));
            BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST",
                registration.toExternalForm());
            request.setEntity(getJsonAsEntity(hubHost, port));
            HttpHost host = new HttpHost(registration.getHost(), registration.getPort());
            HttpClient client = httpClientFactory.getHttpClient();
            HttpResponse response = client.execute(host, request);
            status = response.getStatusLine().getStatusCode();

        } catch (IOException | GridConfigurationException e) {
            throw new GridException(e.getMessage(), e);
        } finally {
            httpClientFactory.close();
        }
        Preconditions.checkState(status == HttpStatus.SC_OK, "There was a problem in hooking in the ghost node.");
    }

    private StringEntity getJsonAsEntity(String host, int port) throws UnsupportedEncodingException {
        try {
            InputStream isr = Thread.currentThread().getContextClassLoader().getResourceAsStream("ondemand.json");
            String string = IOUtils.toString(new InputStreamReader(isr));
            JsonObject ondemand = new JsonParser().parse(string).getAsJsonObject();
            int maxSession =ConfigReader.getInstance().getMaxSession();
            JsonArray capsArray = ondemand.get("capabilities").getAsJsonArray();
            for (int i=0; i < capsArray.size(); i++) {
                capsArray.get(i).getAsJsonObject().addProperty("maxInstances", maxSession);
            }
            JsonObject configuration = ondemand.get("configuration").getAsJsonObject();
            configuration.addProperty("maxSession", maxSession);
            configuration.addProperty("hub", String.format("http://%s:%d", host, port));
            string = ondemand.toString();
            return new StringEntity(string);
        } catch (IOException e) {
            throw new GridException(e.getMessage(), e);
        }
    }

    public static String getHubHost() {
        return hubHost;
    }

}

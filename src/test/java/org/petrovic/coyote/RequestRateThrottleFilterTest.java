package org.petrovic.coyote;

import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.FilterWrapper;
import com.xoom.oss.feathercon.ServletConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestRateThrottleFilterTest {

    private FeatherCon server;

    @Before
    public void setUp() throws Exception {
        ServletConfiguration.Builder servletConfigBuilder = new ServletConfiguration.Builder();
        servletConfigBuilder.withServletClass((Class<? extends Servlet>) MyServlet.class).withPathSpec("/*");

        FilterWrapper.Builder filterBuilder = new FilterWrapper.Builder();
        filterBuilder.withFilterClass((Class<? extends Filter>) RequestRateThrottleFilter.class).withPathSpec("/*")
                .withInitParameter("hits", "4")
                .withInitParameter("period", "30");

        server = new FeatherCon.Builder().withServletConfiguration(servletConfigBuilder.build())
                .withFilter(filterBuilder.build()).build();

        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testInit() throws Exception {
        System.out.println("test init");
    }

    @Test
    public void testDoFilter() throws Exception {
        System.out.println("test do filter");
    }

    @Test
    public void testDoGet() throws Exception {
        while (true) {
            URL url = new URL("http://localhost:8080/whatever");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);
                in.close();
            } else {
                System.out.println("got " + responseCode + ", sleeping...");
                Thread.sleep(31000);
            }
            Thread.sleep(3000);
        }
    }
}

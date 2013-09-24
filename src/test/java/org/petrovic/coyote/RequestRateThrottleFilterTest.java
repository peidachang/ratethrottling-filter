package org.petrovic.coyote;

import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.FilterWrapper;
import com.xoom.oss.feathercon.ServletConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        servletConfigBuilder.withServletClass(MyServlet.class).withPathSpec("/*");
        ServletConfiguration servletConfiguration = servletConfigBuilder.build();

        FilterWrapper.Builder filter1Builder = new FilterWrapper.Builder();
        filter1Builder.withFilterClass(RequestRateThrottleFilter.class).withPathSpec("/filter1")
                .withInitParameter("hits", "4")
                .withInitParameter("period", "30");

        FilterWrapper.Builder filter2Builder = new FilterWrapper.Builder();
        filter2Builder.withFilterClass(RateLimitFilter.class).withPathSpec("/filter2");

        server = new FeatherCon.Builder()
                .withServletConfiguration(servletConfiguration)
                .withFilter(filter1Builder.build())
                .withFilter(filter2Builder.build())
                .build();

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
        System.out.println("test do filter. waiting...");
        while (true) ;
    }

    //    @Test
    public void testDoGetFilter1() throws Exception {
        while (true) {
            URL url = new URL("http://localhost:8080/filter1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);
                in.close();
                Thread.sleep(3000);
            } else {
                System.out.println("got " + responseCode + ", sleeping...");
                Thread.sleep(31000);
            }
        }
    }

    //    @Test
    public void testDoGetFilter2() throws Exception {
        while (true) {
            URL url = new URL("http://localhost:8080/filter2");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);
                in.close();
                Thread.sleep(3000);
            } else {
                System.out.println("got " + responseCode + ", sleeping...");
                Thread.sleep(31000);
            }
        }
    }
}

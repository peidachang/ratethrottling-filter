package org.petrovic.coyote;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.logging.Logger;

public class RateLimitFilter implements Filter {
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(RateLimitFilter.class
            .getName());

    private CacheManager mgr;
    private Cache cache;

    public void init(FilterConfig filterConfig) throws ServletException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/ehcache.xml");
        mgr = new CacheManager(resourceAsStream);
        cache = mgr.getCache("rateLimit");
        try {
            resourceAsStream.close();
        } catch (IOException e) {
        }
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {
        String remoteAddr = req.getRemoteAddr();
        String path = ((HttpServletRequest) req).getPathInfo();
        System.out.println("path: " + path);
        if (!exempt(path)) {
            String key = new StringBuilder(remoteAddr).append(":").append(path).toString();
            Element element = cache.get(key);
            if (element == null) {
                element = new Element(key, new ConnectionBag(path));
                cache.put(element);
                filterChain.doFilter(req, resp);
            } else {
                ConnectionBag bag = (ConnectionBag) element.getObjectValue();
                if (bag.ok()) {
                    filterChain.doFilter(req, resp);
                } else {
                    HttpServletResponse httpR = (HttpServletResponse) resp;
                    httpR.setStatus(HttpURLConnection.HTTP_GONE);
                    httpR.getWriter().write("failed");
                }
            }
        } else {
            filterChain.doFilter(req, resp);
        }
    }

    private boolean exempt(String path) {
//        exempt some paths
//        return !(path.startsWith("/1.1/") || path.startsWith("/1/") || path.contains("/statuses/"));
        return false;
    }

    public void destroy() {
        mgr.shutdown();
    }

    private class ConnectionBag {
        // all times are in milliseconds
        private long stamp = 0;
        private long delta = 2000;
        private int hitCount = 0;
        private long start_delta = 2000;
        private final long max_delta = 1200000;
        private long safePeriod = 300000;
        private final int threshold = 2;
        private final long longDelta = 3600000;
        private int longHitCount = 0;
        private final int longThreshold = 180;
        private long longStamp = 0;

        public ConnectionBag(String path) {
            if (path.endsWith(".rss")) {
                delta = 20000;
                start_delta = delta;
                safePeriod = 300000;
            }
        }

        public boolean ok() {
            boolean limitReached = false;
            long now = new Date().getTime();
            if (delta > max_delta) {
                delta = max_delta;
            }
            if (now <= stamp + delta) {
                ++hitCount;
                if (hitCount >= threshold) {
                    limitReached = true;
                    delta += delta;
                }
            } else {
                hitCount = 0;
            }
            // handle longThreshold per longDelta limit
            if (now <= longStamp + longDelta) {
                ++longHitCount;
                if (longHitCount >= longThreshold) {
                    limitReached = true;
                }
            } else {
                longHitCount = 0;
                longStamp = now;
            }
            if (now > stamp + safePeriod) {
                delta = start_delta;
                hitCount = 0;
            }
            stamp = now;
            return !limitReached;
        }
    }

}
package org.petrovic.coyote;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class VersionFilter implements Filter {
    private String version;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        version = filterConfig.getServletContext().getInitParameter("version");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse r = (HttpServletResponse) servletResponse;
        r.setHeader("Server", version);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}

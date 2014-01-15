/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jstakun
 */
public class ViewTahMapServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        OutputStream out = null;

        String encodings = request.getHeader("Accept-Encoding");
        if (encodings != null && encodings.indexOf("gzip") != -1) {
            // Go with GZIP
            if (encodings.indexOf("x-gzip") != -1) {
                response.setHeader("Content-Encoding", "x-gzip");
            } else {
                response.setHeader("Content-Encoding", "gzip");
            }
            out = new GZIPOutputStream(response.getOutputStream());
        } else if (encodings != null && encodings.indexOf("compress") != -1) {
            // Go with ZIP
            if (encodings.indexOf("x-compress") != -1) {
                response.setHeader("Content-Encoding", "x-compress");
            } else {
                response.setHeader("Content-Encoding", "compress");
            }
            out = new ZipOutputStream(response.getOutputStream());
            ((ZipOutputStream) out).putNextEntry(new ZipEntry("lm"));
        } else {
            // No compression
            out = response.getOutputStream();
        }
        response.setHeader("Vary", "Accept-Encoding");

        // Get the resource to view
        URL url = null;
        try {
            String queryString = request.getQueryString();

            if (request.getParameter("format") == null || request.getParameter("lat") == null || request.getParameter("long") == null
                    || request.getParameter("z") == null || request.getParameter("w") == null || request.getParameter("h") == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                url = new URL("http://tah.openstreetmap.org/MapOf/?" + queryString);

                URLConnection con = url.openConnection();
                con.connect();

                // Get and set the type of the resource
                String contentType = con.getContentType();
                response.setContentType(contentType);

                // Return the resource
                try {
                    returnURL(url, out);
                } catch (IOException e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Problem sending resource: " + e.getMessage());
                }
           }
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Extra path info must point to a valid resource to view: " + e.getMessage());
        }

        if (out != null)
            out.close();
    }

    public static void returnURL(URL url, OutputStream out) throws IOException {
        InputStream in = url.openStream();
        byte[] buf = new byte[4 * 1024]; // 4K buffer
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
    }
}


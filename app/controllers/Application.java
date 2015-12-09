/*Copyright (c) 2015 "hbz"

This file is part of thumby.

thumby is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package controllers;

import helper.ThumbnailGenerator;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import models.Thumbnail;
import play.cache.Cache;
import play.libs.F.Promise;
import play.mvc.Result;

import com.google.common.net.MediaType;

/**
 * @author Jan Schnasse
 *
 */
public class Application extends MyController {

    static final int CACHE_EXPIRATION = 60 * 60;

    /**
     * @return a form to post a url parameter to the uploadUrl endpoint
     */
    public static Promise<Result> registerUrlForm() {
	return Promise.promise(() -> {
	    return ok(views.html.uploadUrl.render(null));
	});
    }

    /**
     * @param urlAddress
     *            a url encoded string of a valid url
     * @param size
     *            the size of the thumbnail
     * @return image/jpeg
     */
    public static Promise<Result> getThumbnail(String urlAddress, int size) {
	return Promise
		.promise(() -> {
		    try {
			if (urlAddress == null || urlAddress.isEmpty())
			    return ok(views.html.uploadUrl.render(null));
			URL url = new URL(urlAddress);
			if (!isWhitelisted(url.getHost()))
			    return status(403,
				    "thumby is not allowed to access this url!");
			Thumbnail result = (Thumbnail) Cache.get(url.toString()
				+ size);
			if (result == null) {
			    result = uploadUrl(url, size);
			    Cache.set(url.toString() + size, result,
				    CACHE_EXPIRATION);
			}
			response()
				.setHeader("Content-Disposition", result.name);
			response().setHeader("Content-Type", "image/jpg");
			return ok(result.thumb);
		    } catch (Exception e) {
			response().setHeader("Content-Type", "text/plain");
			return internalServerError(e.toString());
		    }
		});
    }

    private static boolean isWhitelisted(String host) {
	for (String w : whitelist) {
	    if (w.equals(host))
		return true;
	}
	return false;
    }

    private static Thumbnail uploadUrl(URL url, int size) throws Exception {
	HttpURLConnection connection = null;
	String contentType = null;
	try {
	    connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("GET");
	    connection.connect();
	    contentType = connection.getContentType();
	    Thumbnail thumbnail = createThumbnail(connection.getInputStream(),
		    MediaType.parse(contentType), size, url);
	    return thumbnail;
	} catch(Exception e){
		Thumbnail thumbnail = getDefaultThumbnail(url, size, contentType);
		e.getStackTrace();
		return thumbnail;
	} finally {
	    if (connection != null)
		connection.disconnect();
	}
    }

    private static Thumbnail createThumbnail(InputStream in,
	MediaType contentType, int size, URL url) {
	Thumbnail result = new Thumbnail();
	result.id = UUID.randomUUID().toString();
	result.thumb = ThumbnailGenerator
		.createThumbnail(in, contentType, size);
	result.name = url.getPath();
	result.originalContentType = contentType.toString();
	return result;
    }
    
    private static Thumbnail getDefaultThumbnail(URL url, int size, String contentType) {
		Thumbnail result = new Thumbnail();
		String tbPath = defaultThumb;;
		result.name = tbPath;
		result.originalContentType = "image/jpg";
		result.thumb = new File(tbPath);
		if(contentType != null){
			result.thumb = ThumbnailGenerator.generateMimeThumbnail(contentType, mimeIconPath);
			result.originalContentType = "image/png";
		}
		result.id = UUID.randomUUID().toString();
    	return result;
    }
}

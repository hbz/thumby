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

import java.io.StringWriter;

import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;

import helper.Storage;

/**
 * @author Jan Schnasse
 * Refactored by Alessio Pellerito
 */
public class MyController extends Controller {
    
    protected static ObjectMapper mapper = new ObjectMapper();

    protected static String[] whitelist = ConfigFactory.load().getString("thumby.whitelist").split(",");
    protected static Storage storage = new Storage(ConfigFactory.load().getString("thumby.storageLocation"));

    /**
     * @param obj
     *            an arbitrary object
     * @return json serialization of obj
     */
    public Result getJsonResult(Object obj) {
       // setJsonHeader();
        try {
            return ok(json(obj)).as("application/json").withHeader("Access-Control-Allow-Origin", "*");
        } catch (Exception e) {
            return internalServerError("Not able to create response!");
        }
    }

    protected String json(Object obj) throws Exception {
        StringWriter w = new StringWriter();
        mapper.writeValue(w, obj);
        String result = w.toString();
        return result;
    }
}

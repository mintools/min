package utils;

import play.templates.JavaExtensions;

/**
 * User: soyoung
 * Date: Jan 7, 2011
 */
public class ExtraJavaExtensions extends JavaExtensions {
    public static String escapeJSON(String source) {
        return escapeJavaScript(source).replaceAll("\\\\'", "'");        
    }
}

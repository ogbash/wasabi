package org.wasabi.interceptors


import org.wasabi.http.Request
import org.wasabi.http.Response
import org.wasabi.http.ContentType
import org.apache.commons.codec.binary;
import io.netty.handler.codec.base64.Base64Decoder
import org.wasabi.encoding.decodeBase64
import org.wasabi.routing.RouteHandler
import org.wasabi.app.AppServer
import org.wasabi.http.StatusCodes

public class BasicAuthenticationInterceptor(val realm: String, val callback: (String, String) -> Boolean): Interceptor() {
    override fun intercept(request: Request, response: Response): Boolean {
        if (request.authorization != "") {
            val credentialsBase64Encoded = request.authorization.dropWhile { it != ' ' }
            val credentialsDecoded = credentialsBase64Encoded.decodeBase64("base64")
            val credentials = credentialsDecoded.split(':')
            if (callback(credentials[0], credentials[1])) {
                return true
            }
        }
        response.setStatus(StatusCodes.Unauthorized)
        response.contentType = ContentType.Companion.Text.Plain.toString()
        response.send("Authentication Failed")
        response.addRawHeader("WWW-Authenticate", "Basic Realm=${realm}")
        return false
    }
}


public fun AppServer.useBasicAuthentication(realm: String, callback: (String, String) -> Boolean, path: String = "*") {
    intercept(BasicAuthenticationInterceptor(realm, callback), path)
}
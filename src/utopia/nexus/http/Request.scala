package utopia.nexus.http

import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._
import utopia.flow.generic.FromModelFactory
import utopia.flow.datastructure.template
import utopia.flow.datastructure.template.Property

import scala.collection.immutable.Map
import scala.collection.immutable.HashMap
import utopia.access.http.Method
import utopia.access.http.Cookie
import utopia.access.http.Headers
import utopia.flow.parse.JSONReader

import scala.io.Source

/*
object Request extends FromModelFactory[Request]
{
    def apply(model: template.Model[Property]) = 
    {
        val method = model("method").string.flatMap { Method.parse }
        val path = model("path").string.map { Path.parse }
        
        if (method.isDefined && path.isDefined)
        {
            Some(new Request(method.get, path.get, model("parameters").modelOr(), 
                    model("headers").model.flatMap(Headers.apply).getOrElse(Headers()), 
                    model("cookies").vectorOr().flatMap { _.model }.flatMap { Cookie(_) }))
        }
        else 
        {
            None
        }
    }
}
*/

/**
 * A request represents an http request made from client side to server side. A request targets 
 * a single resource with an operation and may contain parameters, body and headers
 * @author Mikko Hilpinen
 * @since 3.9.2017
  * @param method http method used in request
  * @param targetUrl Uri targeted through this request
  * @param path Path parsed from the targeted uri
  * @param parameters The parameters provided with the request (query or post)
  * @param headers Headers provided with this request
  * @param body The body elements provided with this request (streamed)
  * @param rawCookies Cookies provided with this request
 */
class Request(val method: Method, val targetUrl: String, val path: Option[Path] = None, 
        val parameters: Model[Constant] = Model(Vector()), val headers: Headers = Headers(), 
        val body: Seq[StreamedBody] = Vector(), rawCookies: Traversable[Cookie] = Vector())
{
    // ATTRIBUTES    ---------------------------
    
    /**
     * The cookies provided with the request. All keys are cookie names in lower case letters
     */
    val cookies = rawCookies.map { cookie => (cookie.name.toLowerCase(), cookie) }.toMap
    
    
    // COMPUTED --------------------------------
    
    // def jsonBody = body.headOption.map { b => Source.fromInputStream(b.) }
    
    
    // OPERATORS    ----------------------------
    
    /**
     * @return This request with added parameters
     */
    def ++(params: Model[Constant]) = withAddedParameters(params)
    
    
    // OTHER METHODS    ------------------------
    
    def cookieValue(cookieName: String) = cookies.get(cookieName.toLowerCase()).map(_.value).getOrElse(Value.empty())
    
    /**
     * Creates a new request with some parameters added
     */
    def withAddedParameters(params: Model[Constant]) = new Request(method, targetUrl, path, 
            parameters ++ params, headers, body, cookies.values)
}
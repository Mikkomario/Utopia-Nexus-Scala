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
 * a single resource with an operation and may contain parameters, files and headers
 * @author Mikko Hilpinen
 * @since 3.9.2017
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
    /*
     * The file uploads provided with the request. All keys are parameter / part names in lower case 
     * letters
     */
    //val fileUploads = rawFileUploads.map { upload => (upload.name.toLowerCase(), upload) }.toMap
    
    
    // IMPLEMENTED METHODS / PROPERTIES    -----
    
    /*
    override def toModel = Model(Vector("method" -> method.name, "path" -> path.toString(), 
            "parameters" -> parameters, "headers" -> headers.toModel, 
            "cookies" -> cookies.values.map { _.toModel }.toVector)) */
    
    
    // OTHER METHODS    ------------------------
    
    def cookieValue(cookieName: String) = cookies.get(cookieName.toLowerCase()).map(
            _.value).getOrElse(Value.empty());
}
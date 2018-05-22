package utopia.nexus.rest

import utopia.flow.generic.ValueConversions._
import utopia.flow.util.AutoClose._

import utopia.nexus.http.Request
import utopia.nexus.http.Path
import utopia.nexus.http.Response
import utopia.flow.datastructure.immutable.Model
import utopia.nexus.http.ServerSettings
import utopia.access.http.Headers
import utopia.access.http.MethodNotAllowed
import utopia.access.http.Method

/**
 * This class handles a request by searching for the targeted resource and performing the right 
 * operation on the said resource
 * @author Mikko Hilpinen
 * @since 9.9.2017
 */
class RequestHandler[C <: Context](val childResources: Traversable[Resource[C]], 
        val path: Option[Path] = None, val makeContext: () => C)
{
    // COMPUTED PROPERTIES    -------------
    
    private def currentDateHeader = Headers().withCurrentDate
    
    private def get(implicit context: C) = 
    {
        val childLinks = childResources.map { child => (child.name, (context.settings.address + "/" + 
                path.map { _/(child.name).toString() }.getOrElse(child.name)).toValue) }
        Response.fromModel(Model(childLinks))
    }
    
    
    // OPERATORS    -----------------------
    
    /**
     * Forms a response for the specified request
     */
    def apply(request: Request) = handlePath(request, request.path)
    
    
    // OTHER METHODS    -------------------
    
    private def handlePath(originalRequest: Request, targetPath: Option[Path]): Response = 
    {
        // Sets up request context
        implicit val context = makeContext()
        try
        {
            var currentRequest = originalRequest
            
            // Parses the target path (= request path - handler path)
            var remainingPath = targetPath
            var error: Option[Error] = None
            var pathToSkip = path
            
            // Skips the path that leads to this handler resource
            while (pathToSkip.isDefined && error.isEmpty)
            {
                if (remainingPath.isEmpty || !remainingPath.get.head.equalsIgnoreCase(pathToSkip.get.head))
                {
                    error = Some(Error())
                }
                else
                {
                    remainingPath = remainingPath.get.tail
                    pathToSkip = pathToSkip.get.tail
                }
            }
            
            val firstResource = remainingPath.map{ _.head }.flatMap { resourceName => 
                        childResources.find { _.name.equalsIgnoreCase(resourceName) } }
            if (remainingPath.isDefined && firstResource.isEmpty)
            {
                error = Some(Error())
            }
            
            // Case: Error
            if (error.isDefined)
            {
                error.get.toResponse()
            }
            else if (remainingPath.isEmpty)
            {
                // Case: RequestHandler was targeted
                get
            }
            else
            {
                // Case: A resource under the handler was targeted
                // Finds the initial resource for the path
                var lastResource = firstResource
                if (lastResource.isDefined)
                {
                    // Drops the first resource from the remaining path
                    remainingPath = remainingPath.flatMap { _.tail }
                }
                
                var foundTarget = remainingPath.isEmpty
                var redirectPath: Option[Path] = None
                
                // Searches as long as there is success and more path to discover
                while (lastResource.isDefined && remainingPath.isDefined && error.isEmpty && 
                        !foundTarget && redirectPath.isEmpty)
                {
                    // Sees what's the resources reaction
                    val result = lastResource.get.follow(remainingPath.get, currentRequest);
                    result match
                    {
                        case Ready(remaining) => 
                        {
                            foundTarget = true
                            remainingPath = remaining
                        }
                        case Follow(next: Resource[C], remaining, updates) => 
                        {
                            lastResource = Some(next)
                            remainingPath = remaining
                            
                            if (!updates.isEmpty)
                                currentRequest ++= updates
                            
                            // If there is no path left, assumes that the final resource is ready to 
                            // receive the request
                            if (remainingPath.isEmpty)
                            {
                                foundTarget = true
                            }
                        }
                        case Redirected(newPath, updates) => 
                        {
                            if (!updates.isEmpty)
                                currentRequest ++= updates
                                
                            redirectPath = Some(newPath)
                        }
                        case foundError: Error => error = Some(foundError)
                    }
                }
                
                // Handles search results
                if (error.isDefined)
                {
                    // TODO: Use correct charset
                    error.get.toResponse()
                }
                else if (redirectPath.isDefined)
                {
                    handlePath(currentRequest, redirectPath)
                }
                else if (foundTarget)
                {
                    // Makes sure the method can be used on the targeted resource
                    val allowedMethods = lastResource.get.allowedMethods
                    
                    if (allowedMethods.exists(_ == currentRequest.method))
                    {
                        lastResource.get.toResponse(currentRequest, remainingPath)
                    }
                    else
                    {
                        val headers = Headers().withCurrentDate.withAllowedMethods(allowedMethods.toVector)
                        new Response(MethodNotAllowed, headers)
                    }
                }
                else
                {
                    Error().toResponse()
                }
            }
        }
        finally
        {
            context.closeQuietly()
        }
    }
    
    private def makeNotAllowedResponse(allowedMethods: Seq[Method]) = new Response(
            MethodNotAllowed, currentDateHeader.withAllowedMethods(allowedMethods))
}
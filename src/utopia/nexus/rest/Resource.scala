package utopia.nexus.rest

import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property
import utopia.access.http.Method
import utopia.nexus.http.Request
import utopia.nexus.http.Path
import utopia.nexus.http.ServerSettings
import utopia.nexus.http.Response

trait Resource
{
    // ABSTRACT PROPERTIES & METHODS ------------------
    
    /**
     * The name of this resource
     */
    def name: String
    
    /**
     * The methods this resource supports
     */
    def allowedMethods: Traversable[Method]
    
    /**
     * Performs an operation on this resource and forms a response. The resource may expect that 
     * this method will only be called with methods that are allowed by the resource.
     * @param request the request targeted to the resource
     * @param remainingPath if any of the path was left unfollowed by this resource earlier, it 
     * is provided here
     */
    def toResponse(request: Request, remainingPath: Option[Path])(implicit settings: ServerSettings): Response
    
    /**
     * Follows the path to a new resource. Returns a result suitable for the situation.
     * @param path the path remaining <b>after</b> this resource
     */
    def follow(path: Path, request: Request)(implicit settings: ServerSettings): ResourceSearchResult
}
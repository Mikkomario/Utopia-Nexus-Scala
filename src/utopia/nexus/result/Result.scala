package utopia.nexus.result

import utopia.access.http
import utopia.access.http.Status
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.access.http.NoContent
import utopia.access.http.OK
import utopia.nexus.http.Request
import utopia.nexus.rest.Context

object Result
{
    /**
     * This result may be returned when there is no data to return
     */
    case object Empty extends Result
    {
        def status = NoContent
        def description = None
        def data = Model.empty
    }
    
    /**
     * This result may be returned when a request is invalid or when an error occurs
     */
    case class Failure(val status: Status, val description: Option[String] = None) extends Result
    {
        def data = Model.empty
    }
    
    /**
     * This result may be returned when the API wants to return specific data
     */
    case class Success(val data: Model[Constant], val status: Status = OK, 
            val description: Option[String] = None)
}

/**
* API Responses are simple responses returned by a restful service. The responses can then be handled 
* and represented in different ways
* @author Mikko
* @since 24.5.2018
**/
trait Result
{
    // ABSTRACT    ------------------------
    
    /**
     * The status of the result
     */
	def status: Status
	
	/**
	 * The description for the result (optional)
	 */
	def description: Option[String]
	
	/**
	 * The data returned by this result
	 */
	def data: Model[Constant]
	
	
	// COMPUTED    ------------------------
	
	/**
	 * Whether this is a result of an successful operation
	 */
	def isSuccess = status.group == http.Success
	
	
	// OTHER METHODS    ------------------
	
	/**
	 * Converts this result into a response for the specified request
	 */
	def toResponse(implicit context: Context) = context.resultParser(this, context.request)
}


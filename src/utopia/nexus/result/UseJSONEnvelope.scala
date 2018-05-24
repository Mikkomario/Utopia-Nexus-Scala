package utopia.nexus.result

import utopia.flow.generic.ValueConversions._

import utopia.nexus.http.Request
import utopia.access.http.Status
import utopia.access.http.Success
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Value
import utopia.nexus.http.Response

/**
* This result parser wraps the result in an envelope
* @author Mikko Hilpinen
* @since 24.5.2018
**/
class UseJSONEnvelope(val getDataName: Status => String = 
        s => if (s.group == Success) "data" else "error", 
        val descriptionName: String = "description", 
        val statusName: String = "status") extends ResultParser
{
	def apply(result: Result, request: Request) = 
	{
	    val buffer = Vector.newBuilder[Tuple2[String, Value]]
	    
	    buffer += statusName -> result.status.code
	    result.description.foreach { buffer += descriptionName -> _ }
	    if (!result.data.isEmpty)
	        buffer += getDataName(result.status) -> result.data
	    
	    Response.fromModel(Model(buffer.result()))
	}
}
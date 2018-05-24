package utopia.nexus.rest

import utopia.access.http.ContentCategory._

import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.access.http.Status
import utopia.nexus.http.Request

/**
* This raw result parser uses json or xml, depending on the request headers
* @author Mikko Hilpinen
* @since 24.5.2018
**/
class RawXMLOrJSONResultParser(rootElementName: String = "Response", val preferJSON: Boolean = true) 
        extends RawResultParser
{
    private val xmlParser = new RawXMLResultParser(rootElementName)
    
	def parseDataResponse(data: Model[Constant], status: Status, request: Request) = 
	{
	    val jsonAccepted = request.headers.accepts(Application.json)
	    if (jsonAccepted && preferJSON)
	        RawJSONResultParser.parseDataResponse(data, status, request)
	    else 
	    {
	        val xmlAccepted = request.headers.accepts(Application.xml)
	        if (xmlAccepted || !preferJSON)
	            xmlParser.parseDataResponse(data, status, request)
	        else
	            RawJSONResultParser.parseDataResponse(data, status, request)
	    }
	}
}
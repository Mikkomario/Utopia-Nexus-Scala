package utopia.nexus.result

import utopia.access.http.ContentCategory._
import utopia.flow.datastructure.immutable.{Constant, Model, Value}
import utopia.access.http.Status
import utopia.nexus.http.Request

/**
* This raw result parser uses json or xml, depending on the request headers
* @author Mikko Hilpinen
* @since 24.5.2018
**/
class UseRawXMLOrJSON(rootElementName: String = "Response", val preferJSON: Boolean = true) extends RawResultParser
{
    private val xmlParser = new UseRawXML(rootElementName)
    
	def parseDataResponse(data: Value, status: Status, request: Request) =
	{
		val jsonAccepted = request.headers.accepts(Application.json)
		if (jsonAccepted && preferJSON)
				UseRawJSON.parseDataResponse(data, status, request)
		else
		{
			val xmlAccepted = request.headers.accepts(Application.xml)
			if (xmlAccepted || !preferJSON)
					xmlParser.parseDataResponse(data, status, request)
			else
					UseRawJSON.parseDataResponse(data, status, request)
		}
	}
}
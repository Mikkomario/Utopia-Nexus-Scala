package utopia.nexus.result

import utopia.access.http.ContentCategory._
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.access.http.Status
import utopia.nexus.http.Request
import utopia.flow.parse.XmlElement
import utopia.nexus.http.Response
import utopia.access.http.Headers
import java.nio.charset.StandardCharsets
import utopia.flow.parse.XmlWriter
import scala.Vector

/**
* This result parser parses data into xml format
* @author Mikko Hilpinen
* @since 24.5.2018
**/
class UseRawXML(val rootElementName: String = "Response") extends RawResultParser
{
	def parseDataResponse(data: Model[Constant], status: Status, request: Request) = 
	{
	    val element = XmlElement.apply(rootElementName, data)
	    val charset = request.headers.preferredCharset getOrElse StandardCharsets.UTF_8
	    
	    new Response(status, Headers().withContentType(Application.xml, Some(charset)), Vector(),
	            Some(stream => XmlWriter.writeElementToStream(stream, element, charset)))
	}
}
package utopia.nexus.http

import utopia.access.http.ContentCategory._
import utopia.access.http.ContentType
import utopia.access.http.Headers

/**
* This type of body holds the whole data in memory
* @author Mikko Hilpinen
* @since 12.5.2018
**/
class BufferedBody[T](val contents: T, val contentType: ContentType = Text.plain, 
        val contentLength: Option[Long] = None, val headers: Headers = Headers(), 
        val name: Option[String] = None) extends Body
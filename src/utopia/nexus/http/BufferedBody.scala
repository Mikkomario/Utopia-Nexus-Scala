package utopia.nexus.http

import utopia.access.http.ContentCategory._
import utopia.access.http.ContentType
import utopia.access.http.Headers

/**
* This type of body holds the whole data in memory
* @author Mikko Hilpinen
* @since 12.5.2018
**/
case class BufferedBody[+T](contents: T, contentType: ContentType = Text.plain,
        contentLength: Option[Long] = None, headers: Headers = Headers.currentDateHeaders,
        name: Option[String] = None) extends Body
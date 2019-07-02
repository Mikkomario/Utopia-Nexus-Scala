package utopia.nexus.http

import scala.language.postfixOps
import utopia.access.http.ContentCategory._
import utopia.flow.util.AutoClose._

import java.io.BufferedReader
import utopia.access.http.ContentType
import utopia.access.http.Headers
import scala.util.Try
import java.io.OutputStream
import java.io.File
import java.io.FileOutputStream

/**
* This class represents a body send along with a request. These bodies can only be read once.
* @author Mikko Hilpinen
* @since 12.5.2018
**/
class StreamedBody(val reader: BufferedReader, val contentType: ContentType = Text.plain, 
        val contentLength: Option[Long] = None, val headers: Headers = Headers.currentDateHeaders,
        val name: Option[String] = None) extends Body
{
    // OTHER METHODS    --------------------
    
    def buffered[T](f: BufferedReader => T) = BufferedBody(f(reader), contentType, contentLength, headers, name)
    
    /**
     * Writes the contents of this body into an output stream. Best performance is
     * achieved if the output stream is buffered.
     */
    def writeTo(output: OutputStream) =
    {
        // See: https://stackoverflow.com/questions/6927873/
        // how-can-i-read-a-file-to-an-inputstream-then-write-it-into-an-outputstream-in-sc
          reader.tryConsume(r => Iterator
                  .continually (r.read)
                  .takeWhile (-1 !=)
                  .foreach (output.write))
    }
    
    /**
     * Writes the contents of this body into a file
     */
    def writeToFile(file: File) = new FileOutputStream(file).consume(writeTo)
}
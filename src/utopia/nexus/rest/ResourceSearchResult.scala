package utopia.nexus.rest

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import utopia.access.http.Status
import utopia.access.http.NotFound
import utopia.nexus.http.Path
import utopia.nexus.http.Response

/**
 * There are different types of results that can be get when following a path alongside resources. 
 * All of those result types are under this trait.
 */
sealed trait ResourceSearchResult

/**
 * Ready means that the resource is ready to fulfil the request and form the response
 * @param remainingPath the path that is still left to cover, if there is any
 */
final case class Ready(val remainingPath: Option[Path] = None) extends ResourceSearchResult

/**
 * Follow means that the next resource was found but there is still some path to cover. A follow 
 * response should be followed by another search.
 * @param resource The next resource on the path
 * @param remainingPath The path remaining after the provided resource, if one exists
 */
final case class Follow(val resource: Resource, val remainingPath: Option[Path]) extends ResourceSearchResult

/**
 * A redirect is returned when a link is found and must be followed using a separate path
 * @param newPath The new path to follow to the original destination resource
 */
final case class Redirected(val newPath: Path) extends ResourceSearchResult

/**
 * An error is returned when the next resource is not found or is otherwise not available
 */
final case class Error(val status: Status = NotFound, val message: Option[String] = None) extends ResourceSearchResult
{
    def toResponse(charset: Charset = StandardCharsets.UTF_8) = message.map { 
            Response.plainText(_, status, charset) }.getOrElse(Response.empty(status))
}

// TODO: Add contextRequest
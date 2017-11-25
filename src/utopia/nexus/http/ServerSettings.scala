package utopia.nexus.http

/**
 * Server settings specify values commonly used by server-side resources
 * @author Mikko Hilpinen
 * @since 17.9.2017
 * @param address The server address, including domain name and possible port. Should not end with a 
 * directory separator ('/')
 * @param uploadPath an absolute path that determines where files are uploaded
 */
// TODO: Add parameter encoding as well
case class ServerSettings(val address: String, val uploadPath: java.nio.file.Path)
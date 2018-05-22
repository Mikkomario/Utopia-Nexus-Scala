package utopia.nexus.rest

import utopia.nexus.http.ServerSettings

/**
 * A base context is a very simple context that only contains server settings
* @author Mikko Hilpinen
* @since 22.5.2018
**/
class BaseContext(implicit val settings: ServerSettings) extends Context
{
    // Doesn't need to close anything
    def close() = Unit
}
package utopia.nexus.rest

import utopia.nexus.http.ServerSettings

/**
* Contexts are used for storing and sharing data during a request handling
* @author Mikko Hilpinen
* @since 22.5.2018
**/
trait Context extends AutoCloseable
{
    // ABSTRACT    ------------------------
    
    /**
     * The settings associated with this context
     */
	def settings: ServerSettings
	
	
	// IMPLEMENTED    ---------------------
	
	/**
	 * Closes / finalises the context before it is discarded
	 */
	override def close()
}
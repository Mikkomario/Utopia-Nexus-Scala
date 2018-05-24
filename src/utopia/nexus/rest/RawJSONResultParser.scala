package utopia.nexus.rest

import utopia.nexus.http.Request
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.access.http.Status
import utopia.nexus.http.Response

/**
* This parser outputs the data in JSON format as "raw" (http response-like) as possible
* @author Mikko Hilpinen
* @since 24.5.2018
**/
object RawJSONResultParser extends RawResultParser
{
    def parseDataResponse(data: Model[Constant], status: Status, request: Request) = 
        Response.fromModel(data, status)
}
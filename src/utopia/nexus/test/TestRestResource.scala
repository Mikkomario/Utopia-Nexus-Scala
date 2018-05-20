package utopia.nexus.test

import utopia.access.http.Method._

import utopia.nexus.http.Path
import utopia.nexus.http.Request
import utopia.nexus.http.Response
import utopia.nexus.http.ServerSettings
import utopia.nexus.rest.Error
import utopia.nexus.rest.Follow
import utopia.nexus.rest.Ready
import utopia.nexus.rest.Resource
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.template
import utopia.flow.datastructure.template.Property
import utopia.flow.generic.ValueConversions.ValueOfString
import utopia.access.http.BadRequest
import utopia.access.http.NotImplemented
import utopia.access.http.Created
import utopia.access.http.Forbidden

private object TestRestResource
{
    // Parses all model type children from a model
    private def childrenFromModel(model: template.Model[Property]) = model.attributes.flatMap(attribute => 
            attribute.value.model.map(attribute.name -> _)).map { case (name, model) => 
            new TestRestResource(name, model) }
    
    // Separates "normal" values from model type values
    private def nonChildValuesFromModel(model: template.Model[Constant]) = model.attributes.filter(_.value.model.isEmpty)
}

/**
 * This resource works as a mutable model that accepts rest commands
 * @author Mikko Hilpinen
 * @since 10.10.2017
 */
class TestRestResource(val name: String, initialValues: template.Model[Constant] = Model(Vector())) extends Resource
{
    // ATTRIBUTES    -----------------
    
    override val allowedMethods = Vector(Get, Post, Delete, Put)
    
    // All initial values with model type are transformed into children
    private var children = TestRestResource.childrenFromModel(initialValues)
    // The other values are stored as local values
    private var values = TestRestResource.nonChildValuesFromModel(initialValues)
    
    
    // IMPLEMENTED METHODS    --------
    
    override def toResponse(request: Request, remainingPath: Option[Path])(implicit settings: ServerSettings) = 
    {
        request.method match 
        {
            case Get => handleGet(request.path)
            case Post => 
            {
                if (request.path.isEmpty) 
                    Response.plainText("Path required", BadRequest) 
                else 
                    handlePost(request.path.get, request.parameters)
            }
            case Delete => 
            {
                if (request.path.isEmpty)
                    Response.plainText("Path required", BadRequest)
                else
                    handleDelete(request.path.get.lastElement)
            }
            case Put => handlePut(request.parameters)
            case _ => Response.empty(NotImplemented)
        }
    }
    
    override def follow(path: Path, request: Request)(implicit settings: ServerSettings) = 
    {
        // Post & Delete can be targeted on non-existing items at the end of the paths
        val remainingPath = path.tail
        if ((request.method == Delete || request.method == Post) && remainingPath.isEmpty) 
        {
            Ready(Some(path))
        }
        else
        {
            // If the path leads to a child, forwards the request to that resource
            val targetName = path.head.toLowerCase()
            children.find(_.name.toLowerCase() == targetName) match 
            {
                // For some methods, stops if the next element would be at the end of the path
                case Some(next) => Follow(next, remainingPath)
                case None => Error()
            }
        }
    }
    
    
    // OTHER METHODS    -------------
    
    // Wraps values into a model and Displays the children as links
    private def handleGet(path: Option[Path])(implicit settings: ServerSettings) = Response.fromModel(
            new Model[Constant](values ++ children.map(child => 
            new Constant(child.name, (path/child.name).toServerUrl.toValue))));
    
    private def handlePost(path: Path, parameters: template.Model[Constant])
            (implicit settings: ServerSettings) = 
    {
        children :+= new TestRestResource(path.lastElement, parameters)
        Response.empty(Created).withModifiedHeaders(_.withLocation(path.toServerUrl))
    }
    
    private def handleDelete(targetName: String) = 
    {
        children = children.filterNot(_.name.toLowerCase() == targetName.toLowerCase())
        values = values.filterNot(_.name.toLowerCase() == targetName.toLowerCase())
        
        Response.empty()
    }
    
    private def handlePut(parameters: template.Model[Constant]) = 
    {
        // Cannot delete any existing children with PUT
        if (children.exists(child => parameters.findExisting(child.name).isDefined))
        {
            Response.plainText("Modification of children is not allowed in PUT", Forbidden)
        }
        else
        {
            children ++= TestRestResource.childrenFromModel(parameters)
            values ++= TestRestResource.nonChildValuesFromModel(parameters)
            
            Response.empty()
        }
    }
}
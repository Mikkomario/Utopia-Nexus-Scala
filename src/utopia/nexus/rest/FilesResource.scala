package utopia.nexus.rest

import collection.JavaConverters._
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.NullSafe._
import utopia.access.http.Method._

import utopia.flow.datastructure.template
import utopia.flow.datastructure.immutable
import java.nio.file
import utopia.flow.datastructure.template.Property
import java.io.File
import java.nio.file.Files
import scala.util.Try
import scala.util.Failure
import utopia.flow.datastructure.immutable.Model
import utopia.nexus.http.Path
import utopia.nexus.http.Request
import utopia.nexus.http.ServerSettings
import utopia.nexus.http.Response
import utopia.access.http.MethodNotAllowed
import utopia.access.http.NotFound
import utopia.access.http.BadRequest
import utopia.access.http.Forbidden
import utopia.nexus.http.FileUpload
import utopia.access.http.InternalServerError
import utopia.access.http.OK
import utopia.access.http.Created

/**
 * This resource is used for uploading and retrieving file data.<br>
 * GET retrieves a file / describes a directory (model)<br>
 * POST targets a directory and uploads the file(s) to that directory. Returns CREATED along with 
 * a set of links to uploaded files.<br>
 * DELETE targets a file or a directory and deletes that file + all files under it. Returns 
 * OK if deletion was successful and Internal Server Error (500) if it was not. Returns Not Found (404) 
 * if no valid file or directory was targeted
 * @author Mikko Hilpinen
 * @since 17.9.2017
 */
class FilesResource(override val name: String) extends Resource
{
    // IMPLEMENTED METHODS & PROPERTIES    ---------------------
    
    override def allowedMethods = Vector(Get, Post, Delete)
    
    override def follow(path: Path, request: Request)(implicit settings: ServerSettings) = Ready(Some(path));
         
    override def toResponse(request: Request, remainingPath: Option[Path])(implicit settings: ServerSettings) = 
    {
        request.method match 
        {
            case Get => handleGet(request, remainingPath)
            case Post => handlePost(request, remainingPath)
            case Delete => handleDelete(remainingPath)
            case _ => Response.empty(MethodNotAllowed)
        }
    }
    
    
    // OTHER METHODS    ---------------------------------------
    
    private def handleGet(request: Request, remainingPath: Option[Path])(implicit settings: ServerSettings) = 
    {
        val targetFilePath = targetFilePathFrom(remainingPath)
        
        if (Files.isDirectory(targetFilePath))
        {
            Response.fromModel(makeDirectoryModel(targetFilePath.toFile(), request.targetUrl))
        }
        else if (Files.isRegularFile(targetFilePath))
        {
            Response.fromFile(targetFilePath)
        }
        else
        {
            Response.empty(NotFound)
        }
    }
    
    private def handlePost(request: Request, remainingPath: Option[Path])(implicit settings: ServerSettings) = 
    {
        if (request.fileUploads.isEmpty)
        {
            // TODO: Use correct encoding
            Response.plainText("No files were provided", BadRequest)
        }
        else
        {
            val uploadResults = request.fileUploads.map { case (name, file) => 
                    (name, upload(file, remainingPath)) }
            val successes = uploadResults.filter { _._2.isSuccess }
                  
            if (successes.isEmpty)
            {
                // TODO: For some reason, the error message only tells the directory which 
                // couldn't be created
                val errorMessage = uploadResults.head._2.failed.get.getMessage.toOption
                errorMessage.map(Response.plainText(_, Forbidden)).getOrElse(Response.empty(Forbidden))
            }
            else
            {
                // TODO: Add better handling for cases where request path is empty for some reason
                val myPath = myLocationFrom(request.path.getOrElse(Path(name)), remainingPath)
                val resultUrls = successes.mapValues { result => (myPath/result.get).toServerUrl }
                
                val location = if (resultUrls.size == 1) resultUrls.head._2 else myPath.toServerUrl
                val body = Model.fromMap(resultUrls)
                
                Response.fromModel(body, Created).withModifiedHeaders { _.withLocation(location) }
            }
        }
    }
    
    private def handleDelete(remainingPath: Option[Path])(implicit settings: ServerSettings) = 
    {
        if (remainingPath.isEmpty)
            Response.plainText("May not delete the root upload folder", Forbidden)
        else
            Response.empty(delete(remainingPath.get))
    }
    
    /**
     * @param directory the directory whose data is returned
     * @param directoryAddress the request url targeting the directory
     */
    private def makeDirectoryModel(directory: File, directoryAddress: String) = 
    {
        val allFiles = directory.listFiles().toSeq.groupBy { _.isDirectory() }
        val files = allFiles.getOrElse(false, Vector()).map { directoryAddress + "/" + _.getName }
        val directories = allFiles.getOrElse(true, Vector()).map { directoryAddress + "/" + _.getName }
        
        immutable.Model(Vector("files" -> files.toVector, "directories" -> directories.toVector))
    }
    
    private def upload(fileUpload: FileUpload, remainingPath: Option[Path])(implicit settings: ServerSettings) = 
    {
        val makeDirectoryResult = remainingPath.map { remaining => 
                Try(Files.createDirectories(settings.uploadPath.resolve(remaining.toString()))) }
        
        if (makeDirectoryResult.isEmpty || makeDirectoryResult.get.isSuccess)
        {
            val fileName = remainingPath.map { _ / fileUpload.submittedFileName }.getOrElse(
                    Path(fileUpload.submittedFileName));
            fileUpload.write(fileName)
        }
        else
        {
            Failure(makeDirectoryResult.get.failed.get)
        }
    }
    
    private def delete(remainingPath: Path)(implicit settings: ServerSettings) = 
    {
        val targetFilePath = targetFilePathFrom(Some(remainingPath))
        if (Files.exists(targetFilePath))
        {
            if (recursiveDelete(targetFilePath.toFile)) OK else InternalServerError
        }
        else
        {
            NotFound
        }
    }
    
    private def targetFilePathFrom(remainingPath: Option[Path])(implicit settings: ServerSettings) = 
            remainingPath.map { remaining => settings.uploadPath.resolve(
            remaining.toString) }.getOrElse(settings.uploadPath)
    
    private def myLocationFrom(targetPath: Path, remainingPath: Option[Path]) = 
            remainingPath.flatMap(targetPath.before).getOrElse(targetPath);
    
    private def parseLocation(targetPath: Path, remainingPath: Option[Path], generatedPath: Path) = 
            myLocationFrom(targetPath, remainingPath)/generatedPath;
    
    private def recursiveDelete(file: File): Boolean = 
    {
        if (file.isDirectory())
        {
            // If a directory is targeted, removes all files from the said directory
            file.listFiles().foreach(recursiveDelete)
        }
        // removes the file itself as well
        file.delete()
    }
}
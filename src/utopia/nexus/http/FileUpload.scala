package utopia.nexus.http

import java.nio.file
import java.io.InputStream
import java.io.FileInputStream
import scala.util.Try
import utopia.access.http.ContentType


/**
 * Instances of this class are used for representing files the client sends to the server.<br>
 * The instance is not internally immutable, since it uses streams, but attempts to have value 
 * semantics in relation to the user.
 * @author Mikko Hilpinen
 * @since 7.9.2017
 */
class FileUpload(val name: String, val sizeBytes: Long, 
        val contentType: ContentType, val submittedFileName: String, 
        getInputStream: => InputStream, writeToFile: String => Unit)
        (private implicit val settings: ServerSettings)
{
    // ATTRIBUTES    ----------------------------
    
    // The path to where the file has been permanently (?) stored
    private var fileSavePath: Option[Path] = None
    
    
    // COMPUTED PROPERTIES    ------------------
    
    def toInputStream = filePath.map { path => Try(new FileInputStream(path.toFile())).orElse(
            Try(getInputStream)) }.getOrElse(Try(getInputStream));
            
    def filePath = fileSavePath.map { path => settings.uploadPath.resolve(path.toString()) }
    
    
    // OTHER METHODS    -------------------------
    
    /**
     * Writes the uploaded file into the provided relative path. If the file was already saved, 
     * just returns the previous destination
     * @param relativePath the target save path relative to the upload directory
     * @return the relative path that was used for saving the file. Relative to the upload directory.
     */
    def write(relativePath: Path = Path(submittedFileName)) = Try({
        if (fileSavePath.isEmpty) 
        {
            writeToFile(relativePath.toString)
            fileSavePath = Some(relativePath)
        }
        
        fileSavePath.get
    })
}
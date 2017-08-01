import java.io.File;
//import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

//Unir dos archivos TSV en uno solo
//Idea de: https://stackoverflow.com/a/18655523
public class union{
    public static void main(String[] arg) throws Exception {
    	//Archivos
   		File l1 = new File(arg[0]);
    	File l2 = new File(arg[1]);
    	File l3 = new File(arg[2]);
    	// Leemos los archivos como string
    	String l1str = FileUtils.readFileToString(l1,"ISO_8859_1");
    	System.out.println("L1: "+l1str);
    	String l2str = FileUtils.readFileToString(l2,"ISO_8859_1");
    	System.out.println("L2: "+l2str);
    	// Escribimos el archivo
    	FileUtils.writeStringToFile(l3, l1str,"ISO_8859_1");
    	FileUtils.writeStringToFile(l3, l2str,"ISO_8859_1",true);
    }
}
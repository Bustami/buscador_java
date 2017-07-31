import java.io.*;
import java.nio.charset.StandardCharsets;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

public class listado 
{ 
	public static void main(String... args) throws FileNotFoundException, IOException, NullPointerException {
		//Opcion de programa
		//d: directorio
		Option inO = new Option("d", "Directorio a listar");
		inO.setArgs(1);
		inO.setRequired(true);
		Options options = new Options();
		options.addOption(inO);
		//l: archivos de listas
		Option in1 = new Option("tsv", "Archivo .tsv a almacenar");
		in1.setArgs(1);
		in1.setRequired(true);
		options.addOption(in1);
		//l: archivos de listas
		Option in2 = new Option("m", "Mascara de archivo (por defecto: doc,docx)");
		in2.setArgs(1);
		in2.setRequired(false);
		options.addOption(in2);
		//bd: blacklist de directorio
		Option in3 = new Option("bd", "Archivo TXT con Blacklist de directorios. Nombres de directorios, separador por entre, que no deben ser analizados (por defecto: vacio)");
		in3.setArgs(1);
		in3.setRequired(false);
		options.addOption(in3);
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("***ERROR: faltan parametros requeridos : ");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parametros:", options );
			return;
		}
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parametros:", options );
			return;
		}
		String d = cmd.getOptionValue(inO.getOpt());
		System.err.println("Se revisara el directorio "+d);
		String tsv = cmd.getOptionValue(in1.getOpt());
		//https://stackoverflow.com/questions/5585634/apache-commons-cli-option-type-and-default-value/14309108#14309108
		String m = cmd.getOptionValue(in2.getOpt(),"docx").replace(",","|");//por defecto, dejamos docx y reemplazamos , por |
		System.err.println("Mascara: " + m);
		String bd = cmd.getOptionValue(in3.getOpt(),"");
		String dirblacklist;
		if(bd!=""){
			String listado = FileUtils.readFileToString(new File(bd), StandardCharsets.ISO_8859_1);//charset por si tenemos acentos, que es el del sistema
			dirblacklist = listado.replace("\r\n","|");
		}
		else{
			System.out.println("Sin archivo de blacklist.");
			dirblacklist = "";
		}
		System.err.println("Blacklist directorios: " + dirblacklist);
		File[] files = new File(d).listFiles();
		showFiles(files,tsv,m,dirblacklist);
		System.out.println("Proceso finalizado.");
	}
	public static void showFiles(File[] files,String tsv,String mask, String bd) throws FileNotFoundException, IOException, NullPointerException {
	    for (File file : files) {
	    	if (file.isDirectory() && ( !file.getName().matches(bd) ) ) {
	           	showFiles(file.listFiles(),tsv,mask,bd); // volvemos a entrar a la funcion
	        } else {
	        		//Filtro inicial segun mascara de usuario
	        		if (  file.getName().toLowerCase().matches(".*?\\.("+mask+")$")
	        				&& !file.getName().startsWith("~") && !file.getName().startsWith("._")){
	        			//Para ver que paseamos
	        			System.out.println(file.getAbsolutePath());
		    			System.out.println(file.length()/1024+" KB");
		    			System.out.println(file.lastModified());
		    			//Archivo donde se guarda listado
		    			File log = new File(tsv);
            			try{
            				FileWriter fileWriter = new FileWriter(log, true);
            				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            				// Ruta \t Fecha \t Peso
            				bufferedWriter.write(file.getAbsolutePath()+"\t"+file.lastModified()+"\t"+file.length()+"\n");
            				bufferedWriter.close();
            			} catch (IOException e) {
            	            System.err.println("Problemas al escribir el archivo " + e.getMessage());
            	        }
            			
	        		}
	       		}
	    	}
	    System.out.println("No hay mas carpetas, bajando.");
	}
}
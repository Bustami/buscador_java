import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import org.apache.lucene.document.DateTools;

public class indexar 
{ 
	//Variables internas
	//verbose: mostramos mas detalles de los procesos.
	private static boolean verbose = false;

	public static void main(String... args) throws IOException, TikaException, SAXException, NumberFormatException, org.apache.lucene.queryparser.classic.ParseException {
		//Opcion de programa con Command CLI
		//tsv: TSV a analizar
		Option inO = new Option("tsv", "Listado de archivos, en formato .tsv, a revisar");
		inO.setArgs(1);
		inO.setRequired(true);
		Options options = new Options();
		options.addOption(inO);
		//o: Archivo TSV de salida con datos
		Option in1 = new Option("o", "Archivo .tsv con los indexados (existosos)");
		in1.setArgs(1);
		in1.setRequired(true);
		options.addOption(in1);
		//e: Archivo TSV de errores (archivos no indexados)
		Option in2 = new Option("e", "Archivo .tsv con los errores (archivos no indexados)");
		in2.setArgs(1);
		in2.setRequired(true);
		options.addOption(in2);
		//dir: Directorio de lucene donde almacenamos
		Option in3 = new Option("dir", "Directorio de Lucene, donde almacenamos la indexacion");
		in3.setArgs(1);
		in3.setRequired(true);
		options.addOption(in3);

		//Manejo de ayuda y opciones que falten
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
		String tsv = cmd.getOptionValue(inO.getOpt());
		System.err.println("Listado a revisar: "+tsv);
		String exitosos = cmd.getOptionValue(in1.getOpt());
		System.err.println("Existosos se almacenan en: "+exitosos);
		String errores = cmd.getOptionValue(in2.getOpt());
		System.err.println("Errores se almacenan en: "+errores);
		String dirlucene = cmd.getOptionValue(in3.getOpt());
		System.err.println("Directorio de Lucene: "+dirlucene);
		te_parseo(tsv,dirlucene,exitosos,errores);
		System.err.println("Proceso finalizado.");
        
	}
	public static void te_parseo(String tsv,String dirlucene,String exitosos, String errores) throws IOException, TikaException, SAXException  {
    	//Listado TSV
    	File l1 = new File(tsv);
		//Creamos lo necesario para Lucene
    	Directory dir = FSDirectory.open(Paths.get(dirlucene));
		Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(dir, iwc);
        //Leemos TSV
        BufferedReader tsv1 = new BufferedReader(new FileReader(l1));
        String filadatos = tsv1.readLine();
        while (filadatos != null){
        	String[] archivo = filadatos.split("\t");
        	//Leemos archivo exitosos (cada vez), para no repetirnos analizar algo listo (pensando en poder hacer procesos paralelos o tener que parar el proceso algun punto()
        	List<String>dataexitosos = new ArrayList<String>();
        	try{
            	File lexitosos = new File(exitosos); // Base completo
            	BufferedReader tsvexitosos = new BufferedReader(new FileReader(lexitosos));
                String dataexitoso = tsvexitosos.readLine();
                while (dataexitoso != null){
                    dataexitosos.add(dataexitoso); //para buscar
                    dataexitoso = tsvexitosos.readLine();
                }
                //Cerramos exitosos
                tsvexitosos.close();
        	}catch (FileNotFoundException e) {
        		dataexitosos.add("");//Archivo vacio
        	}
        	if(!dataexitosos.contains(filadatos)){
            	String[] extraccion = extraer(archivo[0]);
        		String data = extraccion[0];
            	String meta = extraccion[1];
            	System.out.println("Indexando");
            	//Insertamos en Lucene
            	Boolean indexado = indexarDoc(writer,data,meta,archivo[0],Long.valueOf(archivo[1]),Long.valueOf(archivo[2]));
            	//en realidad, en la forma que esta escrito nunca falla (no devuelve false), pero estamos en proceso de entenderlo
            	if(indexado){
            		//Agregamos a listado de procesados
            		System.out.println("Indexado. Almacenamos en existosos");
            		logear(exitosos,filadatos);
            	}else{
            		//Agregamos a lista de errores
            		System.err.println("No indexado. Almacenamos en errores");
            		logear(errores,filadatos);
            	}
        	}
        	else{
        		System.err.println("El archivo ya fue analizado exitosamente: "+archivo[0]);
        	}
        	//vamos a siguiente linea
        	filadatos = tsv1.readLine();
        }
        //Cerramos el writer de Lucene
        writer.close();
        //Cerramos el archivo TSV
        tsv1.close();
	}
	
	public static String[] extraer(String archivo) throws IOException, TikaException, SAXException {
		System.out.println("Archivo a procesar: " + archivo);
		File file = new File(archivo);
		//Para arreglar algunos reclmaos de Tika, usamos archivo config
		//https://stackoverflow.com/a/21787813
		//Por ahora la configuracion esta "hardcoded", viene con el paquete de cosas (y deberia estar en la misma carpeta del jar)
		TikaConfig config = new TikaConfig("tika-config.xml");
		//Configuracion especial de Tika, solo Word (nuevo y antiguo)
		Tika tika = new Tika(config);
		Metadata metadata = new Metadata();
		tika.parse(file, metadata);
		//Limpiamos data
		//enters, tab, enter return, dobles espacios que podamos generar
		String clean_data = tika.parseToString(file).replaceAll("\\n|\\r\\n|\\r|\\t", " ").replaceAll("( )+", " ");
		if(verbose) System.out.println("clean_data: "+clean_data);
		//Creamos JSON de metadata, quizas podemos analizar despues
		//https://stackoverflow.com/a/8876284
		String[] metadataNames = metadata.names();
		JSONObject jsonObj = new JSONObject();
		for(String name : metadataNames) {
			jsonObj.put(name, metadata.get(name).toString());
			}
		if(verbose) System.out.println("JSON: "+jsonObj.toString());
		//Devolvemos dos strings
		//https://stackoverflow.com/questions/2301165/how-to-return-two-strings-in-one-return-statement
		return new String[] {clean_data , jsonObj.toString() };
	}
	
	static boolean indexarDoc(IndexWriter writer, String data, String meta, String path, long size, long lastModified) throws IOException {
    	if (verbose) System.out.println("Ruta Archivo: "+path);
    	Document doc = new Document();
        //Ruta de archivo
    	doc.add(new TextField("path", path, Store.YES));
        //Contenido de archivo
    	doc.add(new TextField("content", data, Store.YES));
    	//Metadata del archivo (ahora en string json)
    	doc.add(new TextField("metadata", meta, Store.YES));
    	
    	//Para almacenar datos de fecha y tamaño
    	//Algo redundante, pero quedo almacenado 
    	//https://stackoverflow.com/questions/42482451/lucene-6-recommended-way-to-store-numeric-fields-with-term-vocabulary
    	
    	//Fecha de modificacion
    	//con formato adecuado segun Lucene, funciones especiales de DateTools.timeToString devuelve 
    	//https://stackoverflow.com/questions/21990332/how-to-index-and-search-for-documents-between-two-dates-in-lucene
    	Long modified = Long.parseLong(DateTools.timeToString(lastModified,DateTools.Resolution.SECOND));
    	if(verbose) System.out.println("Fecha con resolucion segundos: "+modified);
    	doc.add(new NumericDocValuesField("modified", modified));
        doc.add(new LongPoint("modified", modified)); //solo este era el inicial
        doc.add(new StoredField("modified", modified));
        //Tamaño de archivo
        doc.add(new NumericDocValuesField("size", size));
        doc.add(new LongPoint("size", size)); //solo este era el inicial
        doc.add(new StoredField("size", size));
        //Actualizamos entrada de documento basado en path
        writer.updateDocument(new Term("path", path), doc);
        //TODO ¿Tenemos forma de confirmar que funciono? Deberia hacer throws de algo si no funciona
        //Deberiamos revisa lo que nos devuelve el writer cuando falla
        return true;
	}

	//Funcion para logear cosas... archivo + string con datos
	//crea LOG, anexa al final DATA, les pone un enter...
	//Se agrega al final del archivo (append)
	//simple, bonito... es mas larga la explicacion que el codigo
	//Inspiracion de https://stackoverflow.com/a/1625266
	static void logear(String log,String data) throws IOException{
		FileWriter fw = new FileWriter(log,true);
		fw.write(data+"\n");
		fw.close();
	}
}
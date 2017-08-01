//Basado en ...
//https://stackoverflow.com/a/26463002
//y otros mas
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//Argumentos Ej: "C:\Users\pprie\Desktop\tsv\1base.tsv" "C:\Users\pprie\Desktop\tsv\2actualizado.tsv" "C:\Users\pprie\Desktop\tsv\3diferencias.tsv"
public class diferencias{
    public static void main(String[] arg) throws Exception {
    	//Archivos
    	File l1 = new File(arg[0]); // Listado base
    	File l2 = new File(arg[1]); // Listado actualizado
    	File l3 = new File(arg[2]); // Nuevo listado con diferencias
    	List<String>data1 = new ArrayList<String>();
    	BufferedReader TSVFile1 = new BufferedReader(new FileReader(l1));
        String dataRow1 = TSVFile1.readLine();
        while (dataRow1 != null){
            data1.add(dataRow1); //para buscar
            dataRow1 = TSVFile1.readLine();
        }
        TSVFile1.close();
        BufferedReader TSVFile2 = new BufferedReader(new FileReader(l2));
        String dataRow2 = TSVFile2.readLine();
        while (dataRow2 != null){
        	if (!data1.contains(dataRow2)){
            	//Como no esta, 
            	System.out.println("no esta: "+dataRow2);
        		try{
        			FileWriter fileWriter = new FileWriter(l3, true);
        			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        			bufferedWriter.write(dataRow2+"\n");
        			bufferedWriter.close();
        		} catch (IOException e) {
                    System.err.println("Problemas al escribir el archivo " + e.getMessage());
                }
            }
        	dataRow2 = TSVFile2.readLine(); //vamos a siguiente linea
        }
        TSVFile2.close();
    }
}
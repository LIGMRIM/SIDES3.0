import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class DataPreparationForMallet {

	
	 public static void printText(String mainFolder, String subfolder, String qID, String text){

			Writer writer = null;
			try {
					File subF = new File(mainFolder+subfolder); 
					if(!subF.exists())
						subF.mkdirs();

		        	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mainFolder+subfolder+"\\"+qID), "utf-8"));

		        	writer.write(text+ " ");
				} catch (IOException ex) {
			    // Report
				} finally {
					try {writer.close();} catch (Exception ex) {/*ignore*/}
				}
				
		}
	
	public static void fromCSVToFoldersAndFiles(String inputFile, String OutPath){
		  try
		   {
			  BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(inputFile),"UTF8"));
		      String line = br.readLine();
		      while (line != null)
		      {
		    	  String[] parts=line.split("[,]");
		    	  
		    	  if(parts.length==3){
		    		  File folder = new File(OutPath+parts[2]);
			    	  if(!folder.exists())
			    		  folder.mkdirs();
			    	  else {//if(folder.listFiles().length<1000){
			    		  printText(OutPath,parts[2].trim(),parts[0].trim(),parts[1].trim());
			    	  }
		    	  }
		    	  line = br.readLine();
		      }
		   br.close();   
	     }
	     catch (Exception e)
	     {
	       e.printStackTrace();
	     } 
	  }
	 public static String getText(String filePath){
		  String result="";
		  try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "utf-8"))){
			    String line = br.readLine();
			    while (line != null) {
			    	result+=line+" ";
			    	line = br.readLine();
			
			    }
			    br.close();
			}
			    catch (Exception e) {
			    	e.printStackTrace();		    
			   	}
		  return result;
		  
	  }
	public static void fromFolderToCSV(String mainFolder){
	    File folder = new File(mainFolder);
	    File[] subFolders= folder.listFiles();
	    for (int j = 0; j < subFolders.length; j++) {
		    File[] files= subFolders[j].listFiles();
		    for (int i = 0; i < files.length; i++) {
				String qtext= getText(files[i].getAbsolutePath());
				System.out.println(files[i].getName()+","+qtext+","+subFolders[j].getName());
				//System.out.println(files[i].getName()+"\t"+subFolders[j].getName());

			}

	    }
	  
  }
	
	public static void main(String[] args)
	  {
		//CSV 
		DataPreparationForMallet.fromCSVToFoldersAndFiles("Questions_with_medical_specialties(Dataset1).csv","./mallet/bin/Questions_with_medical/");
	  }
}

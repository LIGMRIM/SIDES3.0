import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FilterLinks {
	
	
	private Hashtable <String,String> links = new Hashtable<String,String>();
	
	
	public void load(String filePath){
		
		BufferedReader br = null;
		FileReader fr = null;

		try {

				fr = new FileReader(filePath);
				br = new BufferedReader(fr);
				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
					links.put(sCurrentLine.trim(), "");
				}

			} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}

		
	}
	
	
	public void print (){
		
		
		Enumeration<String> linksEnum= links.keys();
		
		while(linksEnum.hasMoreElements()){
			
			String link = linksEnum.nextElement();
			System.out.println(link);
			System.out.println(getHTMLContent(link));
		}
	}
		
	
	
	   public org.jsoup.select.Elements getHTMLParagraphs(String link){
		   try {
		    Document doc = Jsoup.connect(link).get();  
	        org.jsoup.select.Elements paragraphs = doc.select("p");
	        return paragraphs;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   public org.jsoup.select.Elements getHTMLParagraphs(String link, String username, String password){
		   try {
			   		Response res = (Response) Jsoup
					.connect(link)
					.method(Method.POST)
					.execute();

					//This will get you cookies
					Map<String, String> cookies = res.cookies();
					cookies.put("dk_uness_dkt_UserID", "63");
					cookies.put("dk_uness_dkt_UserName", "Almasrmo");
					cookies.put("dk_uness_dkt__session", "9ndih2h57cl0jl5scsigtl7bkup9itn6");


					//And this is the easieste way I've found to remain in session
					Document doc = Jsoup.connect(link).cookies(cookies).get();
			        org.jsoup.select.Elements paragraphs = doc.select("p");

			        return paragraphs;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   

		
	   
	   public org.jsoup.select.Elements getHTMLH1(String link){
		   try {
		    Document doc = Jsoup.connect(link).get();  
	        org.jsoup.select.Elements h1s = doc.select("h1");
	        return h1s;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   
	   public org.jsoup.select.Elements getHTMLH1(String link, String username, String password){
		   try {
			   			Response res = (Response) Jsoup
						.connect(link)
						.method(Method.POST)
						.execute();

						//This will get you cookies
						Map<String, String> cookies = res.cookies();
						cookies.put("dk_uness_dkt_UserID", "63");
						cookies.put("dk_uness_dkt_UserName", "Almasrmo");
						cookies.put("dk_uness_dkt__session", "9ndih2h57cl0jl5scsigtl7bkup9itn6");
						Document doc = Jsoup.connect(link).cookies(cookies).get();


						org.jsoup.select.Elements h1s = doc.select("h1");

			   return h1s;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   public org.jsoup.select.Elements getHTMLH2(String link){
		   try {
		    Document doc = Jsoup.connect(link).get();  
	        org.jsoup.select.Elements h2s = doc.select("h2");
	        return h2s;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   public org.jsoup.select.Elements getHTMLH2(String link, String username, String password){
		   try {
			   Response res = (Response) Jsoup
						.connect(link)
						.method(Method.POST)
						.execute();

						//This will get you cookies
						Map<String, String> cookies = res.cookies();
						cookies.put("dk_uness_dkt_UserID", "63");
						cookies.put("dk_uness_dkt_UserName", "Almasrmo");
						cookies.put("dk_uness_dkt__session", "9ndih2h57cl0jl5scsigtl7bkup9itn6");
						Document doc = Jsoup.connect(link).cookies(cookies).get();

			   org.jsoup.select.Elements h2s = doc.select("h2");
			   return h2s;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   public org.jsoup.select.Elements getHTMLH3(String link){
		   try {
		    Document doc = Jsoup.connect(link).get();  
	        org.jsoup.select.Elements h3s = doc.select("h3");
	        return h3s;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   public org.jsoup.select.Elements getHTMLH3(String link, String username, String password){
		   try {
			   Response res = (Response) Jsoup
						.connect(link)
						.method(Method.POST)
						.execute();

						//This will get you cookies
						Map<String, String> cookies = res.cookies();
						cookies.put("dk_uness_dkt_UserID", "63");
						cookies.put("dk_uness_dkt_UserName", "Almasrmo");
						cookies.put("dk_uness_dkt__session", "9ndih2h57cl0jl5scsigtl7bkup9itn6");
						Document doc = Jsoup.connect(link).cookies(cookies).get();

			   org.jsoup.select.Elements h3s = doc.select("h3");
			   return h3s;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   public org.jsoup.select.Elements getHTMLH4(String link){
		   try {
		    Document doc = Jsoup.connect(link).get();  
	        org.jsoup.select.Elements h4s = doc.select("h4");
	        return h4s;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
	   public org.jsoup.select.Elements getHTMLH4(String link,String username, String password){
		   try {
			   Response res = (Response) Jsoup
						.connect(link)
						.method(Method.POST)
						.execute();

						//This will get you cookies
						Map<String, String> cookies = res.cookies();
						cookies.put("dk_uness_dkt_UserID", "63");
						cookies.put("dk_uness_dkt_UserName", "Almasrmo");
						cookies.put("dk_uness_dkt__session", "9ndih2h57cl0jl5scsigtl7bkup9itn6");
						Document doc = Jsoup.connect(link).cookies(cookies).get();

			   org.jsoup.select.Elements h4s = doc.select("h4");
			   return h4s;
	    	} catch (IOException e) {
				e.printStackTrace();

	    	}
		   return null;
		   
	   }
	   
		
		public String getHTMLContent(String link){
			
			String text="";
			try {
				Document doc = Jsoup.connect(link).get();
				text += doc.body().text();
     			 //text=doc.text();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    return text;
		}
		
		
		public String getHTMLContent(String link, String username, String password)
		  {
		    String text = "";
		    try
		    {
		      Response res = (Response)
		        Jsoup.connect(link)
		        .method(Method.POST)
		        .execute();
		      
		      Map<String, String> cookies = res.cookies();
		      cookies.put("dk_uness_dkt_UserID", "63");
		      cookies.put("dk_uness_dkt_UserName", "Almasrmo");
		      cookies.put("dk_uness_dkt__session", "9ndih2h57cl0jl5scsigtl7bkup9itn6");
		      Document doc = Jsoup.connect(link).cookies(cookies).get();
		      
		      text = text + doc.body().text();
		    }
		    catch (IOException e)
		    {
		      e.printStackTrace();
		    }
		    return text;
		  }
	
	public static void main(String[] args) {
		FilterLinks fl =new FilterLinks();
		try {
		        Document doc = Jsoup.connect("https://wiki.side-sante.fr/doku.php?id=sides:ref:anesthrea:item_131_unique").get();  

		        org.jsoup.select.Elements paragraphs = doc.select("h5");
		        for(Element p : paragraphs)
		        	System.out.println("======================================================\n"+p.text()+p.text().length());
		    	} catch (IOException ex) {
		    	}
		

	}

}

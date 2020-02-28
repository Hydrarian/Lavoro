
/**
 * Example written by Bruno Lowagie in answer to
 * http://stackoverflow.com/questions/29152313/fix-the-orientation-of-a-pdf-in-order-to-scale-it
 */
 
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfRectangle;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//import sandbox.WrapToTest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.xml.sax.SAXException;
 
/**
 * This solution is suboptimal as it throws away all interactivity.
 * If you want to keep the interactive elements (annotations, form fields,...),
 * you need to do much more work. If you don't need to scale down, but instead
 * only have to scale up, you should try the example that was written in
 * answer to http://stackoverflow.com/questions/21871027/rotating-in-itextsharp-while-preserving-comment-location-orientation
 */

public class PDFScaling {
    
    public static final String SRC = "C:\\Users\\tommolini\\Desktop\\PROVABARCODEESTERI\\BERARDI\\FF";
    public static final String DEST = "C:\\Users\\tommolini\\Desktop\\PROVABARCODEESTERI";
    String nomePath;
    String nomePathRemoto;				//path remoto in base alla compagnia
    static File source;
    static File path;
    
    public class ScaleEvent extends PdfPageEventHelper {
        
        protected float scale = 1;
        protected PdfDictionary pageDict;
        
        public ScaleEvent(float scale) {
            this.scale = scale;
        }
        
        public void setPageDict(PdfDictionary pageDict) {
            this.pageDict = pageDict;
        }
        
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            writer.addPageDictEntry(PdfName.ROTATE, pageDict.getAsNumber(PdfName.ROTATE));
            writer.addPageDictEntry(PdfName.MEDIABOX, scaleDown(pageDict.getAsArray(PdfName.MEDIABOX), scale));
            writer.addPageDictEntry(PdfName.CROPBOX, scaleDown(pageDict.getAsArray(PdfName.CROPBOX), scale));
        }
    }
    
    public static void main(String[] args) throws IOException, DocumentException, SAXException, ParserConfigurationException {
        File file = new File(DEST);
        file.getParentFile().mkdirs();
        new PDFScaling().manipulatePdf(SRC, DEST);
        applicaBarcode();
    }
    
    
	public static void applicaBarcode() throws InvalidPasswordException, IOException, DocumentException, SAXException, ParserConfigurationException {
		
		//prende il percorso della cartella per poi ciclare tutti i file all'interno
		boolean processato = false;				//per capire se ci sono file
		path = new File("C:\\Users\\tommolini\\Desktop\\PROVABARCODEESTERI\\");
		String nomePath = path.toString();					//converto in stringa il path ottenuto in precedenza per darlo in pasto al get seguente					//scelta cartella
				
		new File(path + "/Barcode").mkdir();	//creo la cartella per mettere i file con i barcode
		File [] files = path.listFiles();		//elenco tutti i file della cartella del percorso path
		//System.out.println("files: " + files);
		String nomeFileSenzaEstensione;
		String nomeDaBarcode="";					//stringa in cui filtro il codice da mettere (prime 9 cifre del nome)
		int posizioneUnderscore;
		//System.out.println("files.lenght: " + files.length);
		
		int lunghezzaListaFile= files.length;
		
		for (int i = 0; i < files.length; i++){	//ciclo tutti i file nella cartella
			
			//anche se ci sono dei file nella cartella con estensione diversa da .pdf non vengono considerati grazie al filtro
			if (files[i].isFile() & files[i].getName().endsWith((".pdf")) & !files[i].getName().contains("_A")){ 	//finchè c'è un file (CON FILTRO .PDF)
				processato=true;

				//ripulisco il file dalla sua estensione (mi serve per sapere cosa scrivere come barcode)
				nomeFileSenzaEstensione = files[i].getName().replaceFirst("[.][^.]+$", "");	//prendo solo il nome file escludendo l'estenzione
				//System.out.println("nomeFileSenzaEstensione " + nomeFileSenzaEstensione);
		
				
				posizioneUnderscore = nomeFileSenzaEstensione.indexOf("_"); //trova la posizione del carattere _ fin dove devo arrivare per estrarre il barcode 
				if (posizioneUnderscore != -1) {	//se _ non è in posizione -1
				    nomeDaBarcode= nomeFileSenzaEstensione.substring(0 , posizioneUnderscore); //estraggo i primi caratteri prima del primo _
				    
				    
				    System.out.println("nomeFileSenzaEstensione: " + nomeFileSenzaEstensione + " . Barcode estratto: " + nomeDaBarcode);
				}
				
				//System.out.print(path + "/" + files[i].getName() + "\n");
	        	PdfReader reader = new PdfReader(path + "\\" + files[i].getName());		//creo flusso di lettura verso il file
	        	System.out.println(path + "\\" + files[i].getName());
	        	PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(path + "/Barcode/" + files[i].getName() ));	//creo il flusso di scrittura verso un nuovo file all'interno della cartella barcode
			        	
	        	Rectangle pageSize = reader.getPageSize(1);						//il parametro "1" si riferisce alla prima pagina
	        	PdfContentByte over = stamper.getOverContent(1);				//overlay sulla prima pagina
			        
            	Barcode128 code128 = new Barcode128();
                code128.setBaseline(-1);										//posizione della scritta del barcode
                code128.setSize(7);												//dimensione della scritta del barcode
                code128.setCode(nomeDaBarcode);									//codice del barcode da mostrare
                code128.setCodeType(Barcode128.CODE128);						//codifica del barcode
                Image code128Image = code128.createImageWithBarcode(over, BaseColor.BLACK, BaseColor.BLACK);	//colori del barcode
                code128Image.scalePercent(100, 70);							//scala del barcode
                
                
                //System.out.println("code128Image.getWidth() "+code128Image.getWidth());
                //System.out.println("code128Image.getHeight() "+code128Image.getHeight());
                float x = pageSize.getRight() - code128Image.getWidth() - 110;	//posizionamento del barcode rispetto alle dimensioni della pagina
                float y = pageSize.getTop() - code128Image.getHeight() - 10;	//posizionamento del barcode rispetto alle dimensioni della pagina
                float w = pageSize.getRight() - 10;								//posizionamento del barcode rispetto alle dimensioni della pagina
                float h = pageSize.getTop() - 10;								//posizionamento del barcode rispetto alle dimensioni della pagina
                
                over.saveState();
                over.setColorFill(BaseColor.WHITE);								//riempio lo sfondo dell'immagine col colore bianco
                over.rectangle(x-10, y-10, code128Image.getScaledWidth()+14, code128Image.getScaledHeight()+14);							//imposto le dimensioni del rettangolo bianco che fa da sfondo al barcode					
                over.fill();													
                over.restoreState();
                code128Image.setAbsolutePosition(x-10, y-10);					//imposto la posizione in termini di riferimento assoluti dell'immagine che vado ad aggiungere
                over.addImage(code128Image);      								//aggiungo l'immagine al pdf	  	
               

                
	        	stamper.close();												//chiudo la scrittura
	            reader.close();													//chiudo la lettura
	            
	            //System.out.println("ifffff ");
	            //elimino i file nella cartella in cui li ho scaricati
	            //files[i].delete();

			} 
		}	
	}
	
    
    public void manipulatePdf(String src, String dest) throws IOException, DocumentException {
    	source = new File("C:\\Users\\tommolini\\Desktop\\PROVABARCODEESTERI\\BERARDI\\FF\\");
    	File [] files = source.listFiles();		//elenco tutti i file della cartella del percorso path
    	
    	for (int i = 0; i < files.length; i++){	//ciclo tutti i file nella cartella
    		if (files[i].isFile() & files[i].getName().endsWith((".pdf")) & !files[i].getName().contains("_A")){ 	//finchè c'è un file (CON FILTRO .PDF)
    		System.out.println("aaa"+files[i]);
            PdfReader reader = new PdfReader(source+"\\"+files[i].getName());
            float scale = 0.5f;
            ScaleEvent event = new ScaleEvent(scale);
            event.setPageDict(reader.getPageN(1));
            
            int n = reader.getNumberOfPages();
            Document document = new Document();
            System.out.println("aaa"+dest);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest+"\\"+files[i].getName()));
            writer.setPageEvent(event);
            document.open();
            Image page;
            for (int p = 1; p <= n; p++) {
                page = Image.getInstance(writer.getImportedPage(reader, p));
                page.setAbsolutePosition(30, 30);
                page.scalePercent(scale * 80);
                document.add(page);
                if (p < n) {
                    event.setPageDict(reader.getPageN(p + 1));
                }
                document.newPage();
            }
            document.close();
    		}
    	}
    	

    }
    
    public PdfArray scaleDown(PdfArray original, float scale) {
        if (original == null)
            return null;
        float width = original.getAsNumber(2).floatValue()
                - original.getAsNumber(0).floatValue();
        float height = original.getAsNumber(3).floatValue()
                - original.getAsNumber(1).floatValue();
        return new PdfRectangle(width * scale, height * scale);
    }
}

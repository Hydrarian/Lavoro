package applicaBarcode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.xml.sax.SAXException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class applicaB {

	public static void main(String[] args) {
		try {
			applicaBarcode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

public static void applicaBarcode() throws IOException, DocumentException, SAXException, ParserConfigurationException {
		
		//prende il percorso della cartella per poi ciclare tutti i file all'interno
		boolean processato = false;				//per capire se ci sono file
		File path= new File("C:\\Users\\tommolini\\Desktop\\FCP\\");	
		String nomePath = path.toString();					//converto in stringa il path ottenuto in precedenza per darlo in pasto al get seguente					//scelta cartella
				
		new File("C:\\Users\\tommolini\\Desktop\\FCP\\Da stampare\\Barcode").mkdir();	//creo la cartella per mettere i file con i barcode
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
				    //System.out.println("nomeFileSenzaEstensione: " + nomeFileSenzaEstensione + " . Barcode estratto: " + nomeDaBarcode);
				}
				
				//System.out.print(path + "/" + files[i].getName() + "\n");
	        	PdfReader reader = new PdfReader(path + "/" + files[i].getName());		//creo flusso di lettura verso il file
	        	PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(path + "/Da stampare/Barcode/" + files[i].getName() ));	//creo il flusso di scrittura verso un nuovo file all'interno della cartella barcode
			        	
	        	Rectangle pageSize = reader.getPageSize(1);						//il parametro "1" si riferisce alla prima pagina
	        	PdfContentByte over = stamper.getOverContent(1);				//overlay sulla prima pagina
			        
            	Barcode128 code128 = new Barcode128();
                code128.setBaseline(-1);										//posizione della scritta del barcode
                code128.setSize(7);												//dimensione della scritta del barcode
                code128.setCode(nomeDaBarcode);									//codice del barcode da mostrare
                code128.setCodeType(Barcode128.CODE128);						//codifica del barcode
                Image code128Image = code128.createImageWithBarcode(over, BaseColor.BLACK, BaseColor.BLACK);	//colori del barcode
                code128Image.scalePercent(215, 170);							//scala del barcode
                
                
                //System.out.println("code128Image.getWidth() "+code128Image.getWidth());
                //System.out.println("code128Image.getHeight() "+code128Image.getHeight());
                float x = pageSize.getRight() - code128Image.getWidth() - 370;	//posizionamento del barcode rispetto alle dimensioni della pagina
                float y = pageSize.getTop() - code128Image.getHeight() - 65;	//posizionamento del barcode rispetto alle dimensioni della pagina
                float w = pageSize.getRight() - 10;								//posizionamento del barcode rispetto alle dimensioni della pagina
                float h = pageSize.getTop() - 10;								//posizionamento del barcode rispetto alle dimensioni della pagina
                
                over.saveState();
                over.setColorFill(BaseColor.WHITE);								//riempio lo sfondo dell'immagine col colore bianco
                over.rectangle(x-20, y-20, code128Image.getScaledWidth()+14, code128Image.getScaledHeight()+14);							//imposto le dimensioni del rettangolo bianco che fa da sfondo al barcode					
                over.fill();													
                over.restoreState();
                code128Image.setAbsolutePosition(x-10, y-10);					//imposto la posizione in termini di riferimento assoluti dell'immagine che vado ad aggiungere
                over.addImage(code128Image);      								//aggiungo l'immagine al pdf	  	
               
                //aggiungo il tag DataRicezione sotto al barcode:

	        	stamper.close();												//chiudo la scrittura
	            reader.close();													//chiudo la lettura      
		}
	}
}
}


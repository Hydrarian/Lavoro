import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.spi.FileSystemProvider;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.tika.Tika;
import org.xml.sax.SAXException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

//librerie per SSH (SFTP)
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import javax.swing.SwingConstants;
import javax.swing.JProgressBar;

public class scaricaFile extends JFrame {

	private JPanel contentPane;
	Vector listaPresaDaSFTP;
	
    Vector<ChannelSftp.LsEntry> list;	//lista di file presenti nella cartella remota
    Vector<ChannelSftp.LsEntry> lista;
    static String company;
    File[] listaF;
    ChannelSftp sftpChannel;			//canale SFTP per il trasferimento dei file
    Session session;					//nome delle sessione SFTP
    String downloadFileInCorso;			//nome del file che si sta scaricando
    int segnaLista;						//numero di file rimanenti per la fine del download
    String nomePath;
    String nomePathRemoto;				//path remoto in base alla compagnia
    File path;
    boolean downloadCompletato;			//true=pulsante scarica ha finito il suo lavoro ; false=ho annullato il download
    JLabel lblNewLabel = new JLabel();
    boolean dwnInCorso=false;			//mi serve per capire se sto scaricando i file oppure no, così aggiorno il task che mostra il progresso 
    
    JTextArea textArea = new JTextArea();
    String elementiLista;				//lista da mostrare sulla JTextArea
    int numeroFileSuSFTP;
    
    JTextArea textArea_1 = new JTextArea();	//textarea dove mostrare lo scaricamento dei file e tutto il resto dell operazioni
    JProgressBar progressBar = new JProgressBar();
    
    boolean consentiAggiorna=false;
    
    String host; String user; String pwd; int porta=2411;	//parametri sftp
    
	public static void main(String args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					scaricaFile frame = new scaricaFile(args);
					frame.setVisible(true);
					frame.setResizable(false);
					//frame.setUndecorated(true);
				} catch (Exception e) { e.printStackTrace(); }
			}
		});
	}
	
//inizializzazione del programma e dell'interfaccia grafica===============================================
	public scaricaFile(String compagniaScelta) throws JSchException, SftpException, IOException {
		company=compagniaScelta;
		//System.out.println("company: " + company);
		setTitle("Fatturazione ciclo passivo");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) { e.printStackTrace(); } catch (InstantiationException e) {e.printStackTrace();} catch (IllegalAccessException e) {e.printStackTrace();} catch (UnsupportedLookAndFeelException e) {e.printStackTrace();}
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 979, 669);
		setResizable(false);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		//setUndecorated(true);
		
//PULSANTE CHIUDI===============================================================
		JButton btnChiudi = new JButton("Indietro");
		btnChiudi.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnChiudi.setBounds(748, 478, 188, 44);
//imposto l'icona del pulsante===========================================
        String nomeIconaChiudi = "indietro.png";
        try {
			BufferedImage iconaChiudi = ImageIO.read(this.getClass().getResource(nomeIconaChiudi));
			java.awt.Image newimg4 = iconaChiudi.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaChiudi1 = new ImageIcon(newimg4);  // transform it back, y);
			btnChiudi.setIcon(iconaChiudi1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
//======================================================================
		contentPane.add(btnChiudi);
		btnChiudi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				rimuoviSemaforo();
				//System.exit(0);
				Fatture1 f1 = new Fatture1(compagniaScelta);
				f1.frmFatturazioneCicloPassivo.setVisible(true);
				dispose();
			}
		});
		getContentPane().add(btnChiudi);
//==============================================================================

/*Connessione SFTP (con passaggio di parametri ( richiama la funzione connetti)===================
		switch (compagniaScelta) {
		case "Berardi": 
			assegnaDati("\\\\BER-OFFICE\\FattureB2B\\For\\BER", "/B2B/PASSIVO/009306_00628991200/", "ftp.archivagroup.it", "berardibulloneriesrl", "fzU4@wUD_Joy");		//DA CAMBIARE la prima stringa
			connetti();
			break; 
        case "Vitman": 
        	assegnaDati("\\\\BER-OFFICE\\FattureB2B\\For\\VIT", "/B2B/PASSIVO/009308_02091110409/", "ftp.archivagroup.it", "vitmansrl", "HMFs>3cm@tez");		
        	connetti();
        	break; 
        case "Vibolt": 
        	assegnaDati("\\\\BER-OFFICE\\FattureB2B\\For\\VIB", "/B2B/PASSIVO/009307_02100250352/", "ftp.archivagroup.it", "viboltsrlunipersonale", "QeHz^BHK>rGc");		
			connetti();
            break; 
        default: 
        	JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP");
		}
//================================================================================================*/
		
		//Connessione SFTP (con passaggio di parametri ( richiama la funzione connetti)===================
				switch (compagniaScelta) {
				case "Berardi": 
					assegnaDati("\\\\BER-OFFICE\\FattureB2B\\For\\BER", "/B2B/PASSIVO/009306_00628991200/", "ftp.archivagroup.it", "berardibulloneriesrl", "fzU4@wUD_Joy");		//DA CAMBIARE la prima stringa
					connetti();
					break; 
		        case "Vitman": 
		        	assegnaDati("\\\\BER-OFFICE\\FattureB2B\\For\\VIT", "/B2B/PASSIVO/009308_02091110409/", "ftp.archivagroup.it", "vitmansrl", "HMFs>3cm@tez");		
		        	connetti();
		        	break; 
		        case "Vibolt": 
		        	assegnaDati("\\\\BER-OFFICE\\FattureB2B\\For\\VIB", "/B2B/PASSIVO/009307_02100250352/", "ftp.archivagroup.it", "viboltsrlunipersonale", "QeHz^BHK>rGc");		
					connetti();
		            break; 
		        default: 
		        	JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP");
				}
		//================================================================================================
		creaListaPerJtextArea();

//area dove viene mostrata la lista dei file retrieved from SFTP=========================================
		
		textArea.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		textArea.setEditable(false);
		textArea.setEnabled(false);
		textArea.setBounds(12, 63, 453, 364);
		textArea.setCaretPosition(textArea.getText().length());		//Autoscroll all'ultima riga
		contentPane.add(textArea);
		textArea.setText(elementiLista);
//===================================================================================================
		
// aggiunta della barra per scorrere===================================
		JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(36, 103, 900, 362);                     
        getContentPane().add(scroll);
//=====================================================================
        
//Titolo a video della lista
		JLabel lblListaDeiFile = new JLabel("File presenti su SFTP:");
		lblListaDeiFile.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblListaDeiFile.setBounds(36, 42, 412, 39);
		contentPane.add(lblListaDeiFile);
//==============================================================================
		
//Pulsante per avviare il download di tutti i file==========================================================
		JButton btnScarica = new JButton("Scarica");
		btnScarica.setFont(new Font("Tahoma", Font.PLAIN, 14));
		//imposto l'icona del pulsante===========================================
        String nomeIconaScarica = "scarica.png";
        try {
			BufferedImage iconaScarica = ImageIO.read(this.getClass().getResource(nomeIconaScarica));
			java.awt.Image newimg2 = iconaScarica.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaScarica1 = new ImageIcon(newimg2);  // transform it back, y);
			btnScarica.setIcon(iconaScarica1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
//======================================================================
        btnScarica.setEnabled(true);
		btnScarica.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnChiudi.setEnabled(false);
				btnScarica.setEnabled(false);
				start();
				
				}
		});
		btnScarica.setBounds(36, 478, 188, 44);
		contentPane.add(btnScarica);
//========================================================================================================00
		JLabel label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(777, 13, 158, 90);
		contentPane.add(label);
		
		String nomeLogo = compagniaScelta + ".png";
		BufferedImage logo = ImageIO.read(this.getClass().getResource(nomeLogo));
		java.awt.Image newimg;
		if (compagniaScelta=="Berardi") {
			newimg = logo.getScaledInstance(240, 230,  java.awt.Image.SCALE_SMOOTH); // Se la compagnia è Berardi, la ingrandisco di più  
		} else newimg = logo.getScaledInstance(150, 100,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		
		Icon logo1 = new ImageIcon(newimg);  // transform it back
		label.setIcon(logo1);
//imposto l'icona del pulsante===========================================
        String nomeIconaApri = "apri.png";
        try {
			BufferedImage iconaApri = ImageIO.read(this.getClass().getResource(nomeIconaApri));
			java.awt.Image newimg2 = iconaApri.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaApri1 = new ImageIcon(newimg2);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
        
        textArea_1.setBounds(36, 505, 698, 138);
        textArea_1.setCaretPosition(textArea_1.getText().length());		//Autoscroll all'ultima riga
        contentPane.add(textArea_1);
        
        JScrollPane scroll2 = new JScrollPane(textArea_1);
        scroll2.setBounds(36, 535, 900, 83);                     
        getContentPane().add(scroll2);
        
        progressBar.setStringPainted(false);
        progressBar.setString("Connessione ed ottenimento lista file in corso...");
        progressBar.setBounds(236, 490, 500, 30);
        contentPane.add(progressBar);
	}
//==========================================================================================================
	
	//funzione per assegnare i valori giusti alle variabili a seconda della compagnia
	public void assegnaDati(String pathDaAssegnare, String pathRemotoDaAssegnare, String hostDaAssegnare, String userDaAssegnare, String pwdDaAssegnare) {
		path = new File(pathDaAssegnare);
		nomePathRemoto=pathRemotoDaAssegnare; 	//DA CAMBIARE CON QUELLO GIUSTO PASSIVO
		host=hostDaAssegnare;
		user=userDaAssegnare;
		pwd=pwdDaAssegnare;
	}
	
	
	public void rimuoviSemaforo() {
		//RIMUOVO IL SEMAFORO (DOPO CHE HO COMPLETATO L'OPERAZIONE DI DOWNLOAD=============
		   try {
			   System.out.print(nomePathRemoto + "semaforo.sem");
			   sftpChannel.cd("/" + nomePathRemoto); // Change Directory on SFTP Server
			   sftpChannel.rm("semaforo.sem");
			   System.out.print("semaforo.sem rimosso.");
		} catch (SftpException e) {
			JOptionPane.showMessageDialog(null, "ERRORE SFTP: Non è stato possibile rimuovere semaforo.sem. Rimuoverlo manualmente.");	
			e.printStackTrace();
		}
//===========================================================================
	}
	
public void start() {
	progressBar.setStringPainted(true);
		SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
			
			@Override
			protected Void doInBackground() throws Exception {
				progressBar.setVisible(true);
				progressBar.setIndeterminate(true);
				downloadFile();
				try {
					listaPresaDaSFTP= stampaLista(sftpChannel);
				} catch (SftpException e) { e.printStackTrace(); }
				creaListaPerJtextArea();
				
		    	textArea.setText(elementiLista);
		    	textArea.update(textArea.getGraphics());
		    	
				applicaBarcode(company);
				System.out.println("dopo barcode");
				muoviAltriFile(company);
				progressBar.setString("Operazioni terminate ...");
				progressBar.setVisible(false);
				return null;
			}

			@Override
			protected void done() {
				
			}
			
		};
		worker.execute();
	}

	public void creaListaPerJtextArea() {
//LISTA (per far vedere a video i file remoti)=======================================================================
				elementiLista = "";	
				for( numeroFileSuSFTP = 0 ; numeroFileSuSFTP < listaPresaDaSFTP.size(); numeroFileSuSFTP++ ) {
					//System.out.println("listaPresaDaSFTP.get(numeroFileSuSFTP)"+listaPresaDaSFTP.get(numeroFileSuSFTP));
					elementiLista = elementiLista + listaPresaDaSFTP.get(numeroFileSuSFTP) + "\n"; //prendo il nome di tutti i file remoti per mostrarli dopo a video
				}
//=================================================================================================================
	}
	
//funzione richiamata dal pulsante Scarica per scaricare tutti i file nella cartella locale selezionata
	public void downloadFile() throws JSchException, SftpException {
	    
		   //path = new File("C:/Users/tommolini/Desktop/FatturazioneCP/"); //DA CAMBIARE prendo il path della cartella locale selezionata
		   nomePath = path.toString();					//converto in stringa il path ottenuto in precedenza per darlo in pasto al get seguente

			dwnInCorso =true;
			
			boolean esitoFileCorrente;		//esito del download: positivo=true, errore= false
			
			JSch jsch = new JSch();
	        session = jsch.getSession(user, host, porta);				//creo una nuova sessione verso l'host
	        session.setPassword(pwd);									//immetto la password
	        session.setConfig("StrictHostKeyChecking", "no");
	        //System.out.println("Tentativo di connessione...");
	        session.connect();											//avvio la sessione
	        //System.out.println("Connessione stabilita.");
	        //System.out.println("Canale SFTP in creazione...");
	        sftpChannel = (ChannelSftp) session.openChannel("sftp");	//creo nuovo canale SFTP
	        sftpChannel.connect();										//mi connetto al canale
	        //System.out.println("Canale SFTP creato.");
	        
	        progressBar.setString("Download dei file...");
	        
	        int i=0;
//SCARICO I FILE IN LOCALE SU nomePath==========================================================================
		   for (ChannelSftp.LsEntry oListItem : list) { // Iterate objects in the list to get file/folder names.
			   esitoFileCorrente=true;
			   
			   downloadFileInCorso = oListItem.getFilename();
			   
			   i++;
			   segnaLista = segnaLista - 1;
				try {
					textArea_1.append("\n [ " + i + " | " + list.size() + " ] Download del file " + downloadFileInCorso +" in corso...");
					//System.out.println(" nomePathRemoto + downloadFileInCorso, nomePath "+nomePathRemoto+" " + downloadFileInCorso+" " +nomePath);
					sftpChannel.get(nomePathRemoto + downloadFileInCorso, nomePath);	//DA MODIFICARE //scarico il file nella cartella selezionata			
					
					textArea_1.append("\n [ " + i + " | " + list.size() + " ] Download del file " + downloadFileInCorso +" completato.");
					textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
				} catch (SftpException e) {
					esitoFileCorrente=false;
					textArea_1.append("\n [ " + i + " | " + list.size() + " ] Impossibile scaricare il file " + downloadFileInCorso + ". Errore SFTP. Il file non verrà rimosso dal server");
					JOptionPane.showMessageDialog(null, "Impossibile scaricare il file " + downloadFileInCorso + ". Errore SFTP. Il file non verrà rimosso dal server");
					textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
				}
				catch (Exception e) {
					esitoFileCorrente=false;
					textArea_1.append("\n [ " + i + " | " + list.size() + " ] Impossibile scaricare il file " + downloadFileInCorso + ". Errore ECCEZIONE. Il file non verrà rimosso dal server");
					JOptionPane.showMessageDialog(null, "Impossibile scaricare il file " + downloadFileInCorso + ". Errore ECCEZIONE. Il file non verrà rimosso dal server");
					textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
				}
				
//VALUTO L'ESITO DEL DOWNLOAD E, NEL CASO DI NESSUN ERRORE ELIMINO IL FILE SULLA CARTELLA REMOTA=========================
				if (esitoFileCorrente==true) {
					sftpChannel.rm(nomePathRemoto + downloadFileInCorso);		//DA CAMBIARE
					textArea_1.append("\n [ " + i + " | " + list.size() + " ] " + downloadFileInCorso + " rimosso dal server SFTP.");
					textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
					//System.out.println("FILE RIMOSSO");				
				} 
			}
		   rimuoviSemaforo();
		   dwnInCorso =false;
		   textArea_1.append("\n Download dei file completato nella cartella: " + nomePath);
		   textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
		   //JOptionPane.showMessageDialog(null, "Download dei file completato nella cartella: " + nomePath);	
		   downloadCompletato=true;
//RIMUOVO IL SEMAFORO (DOPO CHE HO COMPLETATO L'OPERAZIONE DI DOWNLOAD=============
		   
//===========================================================================
	}
//===========================================================================================================

//Funzione che applica il barcode a tutti i file che trova in una determinata cartella==========================
	public void applicaBarcode(String compagniaScelta) throws InvalidPasswordException, IOException, DocumentException, SAXException, ParserConfigurationException {
		
		//prende il percorso della cartella per poi ciclare tutti i file all'interno
		boolean processato = false;				//per capire se ci sono file
		progressBar.setString("Applicazione del barcode...");
		
		nomePath = path.toString();					//converto in stringa il path ottenuto in precedenza per darlo in pasto al get seguente					//scelta cartella
				
		new File(path + "/Da stampare/Barcode").mkdir();	//creo la cartella per mettere i file con i barcode
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
				
				//TROVO IL TAG DATARICEZIONE SUL CORRISPONDENTE FILE XML CON NOME UGUALE AL FILE PDF
				DocumentBuilderFactory dbfaFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = dbfaFactory.newDocumentBuilder();
				File fileXml = new File(path + "\\" + nomeFileSenzaEstensione + ".xml");
				org.w3c.dom.Document doc =  documentBuilder.parse(fileXml);
				
				//trovo il tag xml e lo converto nella formattazione dd-MM-yyyy
				String dataR = ((org.w3c.dom.Document) doc).getElementsByTagName("DataRicezione").item(0).getTextContent();
				String anno= dataR.substring(0, 4);
				String mese= dataR.substring(5, 7);
				String giorno=dataR.substring(8, 10);
				String dataRicezione= giorno+"-"+mese+"-"+anno;
				
				
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
                ColumnText.showTextAligned(over, Element.ALIGN_LEFT, new Phrase("Ricezione: "+dataRicezione),x-17,y-34,0);
                
	        	stamper.close();												//chiudo la scrittura
	            reader.close();													//chiudo la lettura
	            
	            //System.out.println("ifffff ");
	            //elimino i file nella cartella in cui li ho scaricati
	            files[i].delete();
	            textArea_1.append("\n[ " + i + " | " +lunghezzaListaFile + "] Barcode applicato al file " + files[i].getName());
	            textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
			} 
		}
			
		textArea_1.append("\n Barcode applicato a tutti i file.");
		textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
		//JOptionPane.showMessageDialog(null, "Barcode applicato a tutti i file.");	//fine dell'operazione di applicazione barcode
		
	}
//===========================================================================================================
	
//Funzione per spostare gli altri file che non sono le fatture pdf, ma sono xml, e _A*.pdf
	public void muoviAltriFile(String compagniaScelta) throws IOException, JSchException, SftpException {
		System.out.println("dentro muovialtrifile");
		nomePath = path.toString();					//converto in stringa il path ottenuto in precedenza per darlo in pasto al get seguente					//scelta cartella
		progressBar.setString("Spostamento dei file allegati...");
		new File(path + "/Da stampare/Barcode").mkdir();	//creo la cartella per mettere i file con i barcode
		File [] files = path.listFiles();		//elenco tutti i file della cartella del percorso path
		
		int lunghezzaListaFile= files.length;
		for (int i = 0; i < lunghezzaListaFile; i++){	//ciclo tutti i file nella cartella
			System.out.println("dentro FOR muovialtrifile");
			//anche se ci sono dei file nella cartella con estensione diversa da .pdf non vengono considerati grazie al filtro
			if (files[i].isFile() /*& !files[i].getName().endsWith((".compressed"))*/){ 	//finchè c'è un file (CON FILTRO .PDF)
				System.out.println("DENTRO AL PRIMO IF");
				File dir = new File(path+"\\Da stampare\\");	
				dir.mkdir();
				File fileDaSpostare = new File(path + "\\"+ files[i].getName());
				System.out.println("DOPO IL DIR");
				//FileUtils.copyFileToDirectory(fileDaSpostare, dir);
				
				//vedo di che tipo è il file
				Tika tika = new Tika();
			    String filetype = tika.detect(files[i]);

			    if (filetype.compareTo("application/pdf")==0 ) {
			    	//System.out.println(files[i] + " è di tipo pdf");
			    	if (!fileDaSpostare.getName().endsWith(".pdf")) {//se l'estensione del file pdf non è .pdf 
			    		//System.out.println("Il file da cambiare a .pdf è " + fileDaSpostare);
			    		File nomeFileSenzaEstensione = new File((fileDaSpostare.getName().replaceFirst("[.][^.]+$", "")+".pdf").toString());
			    		//System.out.println("nomeFileSenzaEstensione " + nomeFileSenzaEstensione);
			    		
			    		File nomeFileSenzaEstensione2 = new File(path+"\\Da stampare\\"+nomeFileSenzaEstensione);
			    		//System.out.println("nomeFileSenzaEstensione " + nomeFileSenzaEstensione2);
			    		
			    		boolean b = fileDaSpostare.renameTo(nomeFileSenzaEstensione2);
			    		//JOptionPane.showMessageDialog(null, "Ho rinominato il file in "+ nomeFileSenzaEstensione2);

			    		//System.out.println("valore di b "+ b);
			    		//System.out.println("fileDaSpostare " + fileDaSpostare );
			    		//int nuovoNomeConGiustaEstensione = fileDaSpostare.getName().toString().lastIndexOf('.');
			    	} else {
			    		//System.out.println("Ho spostato l'allegato .pdf: " + fileDaSpostare);
			    		FileUtils.copyFileToDirectory(fileDaSpostare, dir);
			    	}
					//FileUtils.copyFileToDirectory(fileDaSpostare, dir);
					//System.out.println("prima del delete");
					files[i].delete();
					//System.out.println("dopo il delete");
		            textArea_1.append("\n[ " + i + " | " +lunghezzaListaFile + "] Sto spostando il file " + files[i].getName());
		            textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
				} else if (filetype.compareTo("application/xml")==0){
					FileUtils.copyFileToDirectory(fileDaSpostare, dir);
					
					files[i].delete();
		            textArea_1.append("\n[ " + i + " | " +lunghezzaListaFile + "] Sto spostando il file " + files[i].getName());
		            textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
				}

			    else if (filetype.compareTo("application/zip")==0) {
					System.out.print(files[i] + " è di tipo zip");
					//creo una cartella in stampati col nome del file zippato
					File dirZippato = new File(path+ "/Da stampare/" + files[i].getName().toString().substring(0, files[i].getName().toString().lastIndexOf('.')));
					System.out.println("Cartella del file compressed: " + dirZippato.toString());
					dirZippato.mkdir();
					
					//unzippo il file nella cartella stampati
					 String fileZip = path + "\\"+ files[i].getName();		//percorso+nome del file compresso
					 //System.out.println("fileZip " + fileZip);
					 textArea_1.append("\n[ " + i + " | " +lunghezzaListaFile + "] Sto estraendo i file conenuti in " + files[i].getName());
			            textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
				        byte[] bufferZ = new byte[1024];
				        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
				        ZipEntry zipEntry = zis.getNextEntry();
				        while (zipEntry != null) {
				        	System.out.println("zipEntry: " + zipEntry);
				            File newFile = newFile(dirZippato, zipEntry);
				            FileOutputStream fos = new FileOutputStream(newFile);
				            int len;
				            while ((len = zis.read(bufferZ)) > 0) {
				                fos.write(bufferZ, 0, len);
				            }
				            fos.close();
				            zipEntry = zis.getNextEntry();
				        }
				        zis.closeEntry();
				        zis.close();
				        
				}

	            //elimino i file nella cartella in cui li ho scaricati
				files[i].delete();
	            textArea_1.append("\n[ " + i + " | " +lunghezzaListaFile + "] Sto spostando il file " + files[i].getName());
	            textArea_1.setCaretPosition(textArea_1.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
			} 
		}
		
		scaricaFile s=new scaricaFile(compagniaScelta);
		s.setVisible(true);
		dispose();
		
	}
	
//Funzione per l'estrazione dei file compressi
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
         
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
         
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
         
        return destFile;
    }
	
//Funzione per la stampa dell'elenco dei file nella finestra tramite JList============
	public Vector stampaLista (ChannelSftp canale) throws SftpException {

		//canale.
		list = canale.ls(nomePathRemoto + "*.*");				//DA MODIFICARE COL NOME DELLA CARTELLA REMOTA E ESTENSIONI DEI FILE
		
//LEGGO IL SEMAFORO: scorro tutta la lista alla ricerca del file .sem: se c'è chiudo il programma e non mi connetto
			for (int i=0; i<list.size(); i++) {
				if (list.get(i).toString().contains(".sem")) {
					JOptionPane.showMessageDialog(null, "ERRORE! Server SFTP occupato da un altro utente.");
					canale.disconnect();
					System.exit(0);
				}
			}
		segnaLista = list.size();

		return list;
	}
//====================================================================================
	
//connessione al server SFTP con dati presi dalla precedente selezione nella combobox===========================
	public void connetti(/*String host, String user, String pwd, int porta*/) throws JSchException, SftpException, IOException {
		
	    try {
	        JSch jsch = new JSch();
	        session = jsch.getSession(user, host, porta);				//creo una nuova sessione verso l'host
	        session.setPassword(pwd);									//immetto la password
	        session.setConfig("StrictHostKeyChecking", "no");
	        //System.out.println("Tentativo di connessione...");
	        session.connect();											//avvio la sessione
	        //System.out.println("Connessione stabilita.");
	        //System.out.println("Canale SFTP in creazione...");
	        sftpChannel = (ChannelSftp) session.openChannel("sftp");	//creo nuovo canale SFTP
	        sftpChannel.connect();										//mi connetto al canale
	        //System.out.println("Canale SFTP creato.");
			listaPresaDaSFTP = stampaLista(sftpChannel);				//prendo la lista dei file remoti per stamparla

//METTO IL SEMAFORO
			sftpChannel.cd(nomePathRemoto);
			PrintWriter writer = new PrintWriter("\\\\BER-OFFICE\\FattureB2B\\For\\semaforo.sem", "UTF-8"); 	//DA CAMBIARE CON IL PATH DOVE VENGONO SCARICATE LE FATTURE
			//PrintWriter writer = new PrintWriter("C:\\Users\\tommolini\\Desktop\\FCP\\semaforo.sem", "UTF-8"); 
			writer.println("semaforo per accesso esclusivo");
			writer.close();
	        File f = new File("\\\\BER-OFFICE\\FattureB2B\\For\\semaforo.sem");	//DA CAMBIARE CON IL PATH DOVE VENGONO SCARICATE LE FATTURE
			//File f = new File("C:\\Users\\tommolini\\Desktop\\FCP\\semaforo.sem");
			sftpChannel.put(new FileInputStream(f), f.getName()); // here you can also change the target file name by replacing f.getName() with your choice of name
			//System.out.println("semaforo messo.");
			
	    } catch(JSchException | SftpException e) {						//gestisco le eccezioni	
	    	JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP.");
			JLabel label = new JLabel("ERRORE! Non è stato possibile raggiungere il server SFTP.");
			contentPane.add(label, BorderLayout.NORTH);
	        System.out.println(e);
	    }    
	}
}
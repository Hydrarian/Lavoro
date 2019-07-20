import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

//librerie per SSH (SFTP)
import com.jcraft.jsch.Channel;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import java.awt.Image;

import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class scaricaFile extends JFrame {

	private JPanel contentPane;
	Vector listaFileTXT;
	Vector listaFilePDF;
	
	String cartellaRemota;					//cartella SFTP da cui scaricare i file 
    String cartellaLocale;					//cartella locale su cui scaricare i file
    Vector<ChannelSftp.LsEntry> listTXT;	//lista di file TXT presenti nella cartella remota
    Vector<ChannelSftp.LsEntry> listPDF;	//lista di file PDF presenti nella cartella remota
    ChannelSftp sftpChannel;				//canale SFTP per il trasferimento dei file
    Channel channel;						//altro canale per il traferimento file (o è sftpChannel <- CONTROLLARE)
    Session session;						//nome delle sessione SFTP
    String uploadFileInCorso;				//nome del file che si sta caricando
    //String downloadFileInCorso;			//nome del file che si sta scaricando
    int segnaLista;							//numero di file rimanenti per la fine del download
    //boolean downloadCompletato;			//true=pulsante scarica ha finito il suo lavoro ; false=ho annullato il download
    boolean uploadCompletato;				//true=pulsante scarica ha finito il suo lavoro ; false=ho annullato il download
    JLabel lblNewLabel = new JLabel();
    Vector<ChannelSftp.LsEntry> lista;		//usata da stampaListaFile per prendere la lista dei file remoti e assegnarli a listaFileTXT e listaFilePDF
    
//DOWNLOAD FILE TXT:
	String nomePathTXT;	
	String nomePathRemotoTXT;													
//DOWNLOAD FILE PDF:
    String nomePathPDF;		
    String nomePathRemotoPDF;													
    
    String elementiListaTXT;		
    String elementiListaPDF;		
    int occorrenzeTXT[];					//array che contiene i numeri dei file TXT
    int occorrenzePDF[];					//array che contiene i numeri dei file TXT
    String nomeFileTabella[];				//array che contiene i nomi file contati nella tabella
    
	ArrayList<String> nomiFile = new ArrayList<String>();
	ArrayList<Integer> ArrayOccorrenzeTXT = new ArrayList<Integer>();
	ArrayList<Integer> ArrayOccorrenzePDF = new ArrayList<Integer>();
	
	JProgressBar progressBar = new JProgressBar();
	JTextArea textArea = new JTextArea();
	JButton btnPulisci = new JButton("Pulisci cartella");
	
	public static void main(String args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					scaricaFile frame = new scaricaFile(args);
					frame.setVisible(true);
				} catch (Exception e) { e.printStackTrace(); }
			}
		});
	}
//===================================================================
	
	public String prendiListaFile(String path) throws SftpException {
		File folder = new File(path);					//path= da passare in input sia quello dei TXT che PDF
		File[] listOfFiles = folder.listFiles();
		String elementiLista = "";
		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
			  elementiLista = elementiLista + listOfFiles[i].getName() + "\n";
		  }
		}
//===================================================================================================================
		return elementiLista;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	public scaricaFile(String compagniaScelta) throws JSchException, SftpException, IOException {
		setTitle("Fatturazione ciclo attivo");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) { e.printStackTrace(); } catch (InstantiationException e) { e.printStackTrace(); } catch (IllegalAccessException e) { e.printStackTrace(); } catch (UnsupportedLookAndFeelException e) { e.printStackTrace(); }
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 994, 655);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		contentPane.setLayout(null);

//PULSANTE CHIUDI=================================================================
		JButton btnChiudi = new JButton("Chiudi");
		btnChiudi.setBounds(834, 405, 147, 49);
		btnChiudi.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
//imposto l'icona del pulsante===========================================
        String nomeIconaChiudi = "chiudi.png";
        try {
			BufferedImage iconaChiudi = ImageIO.read(this.getClass().getResource(nomeIconaChiudi));
			Image newimg4 = iconaChiudi.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaChiudi1 = new ImageIcon(newimg4);  //transform it back, y);
			btnChiudi.setIcon(iconaChiudi1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//======================================================================
        
		contentPane.add(btnChiudi);
		btnChiudi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		getContentPane().add(btnChiudi);
//================================================================================
		
//Connessione SFTP (con passaggio di parametri ( richiama la funzione connetti)===================
		switch (compagniaScelta) {
		case "Berardi": 
			nomePathRemotoTXT="B2B/ATTIVO/009306_00628991200/";	
			nomePathRemotoPDF="B2B/ATTIVO/009306_00628991200/";	
			nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\BER";
			nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\BER";
			//nomePathTXT="C:\\Users\\tommolini\\Desktop\\FCP";
			//nomePathPDF="C:\\Users\\tommolini\\Desktop\\FCP";
			connetti("ftp.archivagroup.it", "berardibulloneriesrl", "fzU4@wUD_Joy", 2411); 		
            break; 
		case "Vitman": 
			nomePathRemotoTXT="B2B/ATTIVO/009308_02091110409/";	
			nomePathRemotoPDF="B2B/ATTIVO/009308_02091110409/";	
			nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\VIT";
			nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\VIT";
			connetti("ftp.archivagroup.it", "vitmansrl", "HMFs>3cm@tez", 2411); 		
            break; 
		case "Vibolt": 
			nomePathRemotoTXT="B2B/ATTIVO/009307_02100250352/";	
			nomePathRemotoPDF="B2B/ATTIVO/009307_02100250352/";	
			nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\VIB";
			nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\VIB";
			connetti("ftp.archivagroup.it", "viboltsrlunipersonale", "QeHz^BHK>rGc", 2411); 	
            break; 
		default:
			JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP");
		}
//================================================================================================	
	
		//faccio vedere i file nelle cartelle pdf e txt locali/////////////////
		elementiListaTXT = prendiListaFile(nomePathTXT);
		elementiListaPDF = prendiListaFile(nomePathPDF);
		///////////////////////////////////////////////////////////////////////
		
		JTextArea textAreaTXT = new JTextArea();
		textAreaTXT.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		textAreaTXT.setBounds(10, 63, 481, 333);
		textAreaTXT.setEditable(false);
		textAreaTXT.setEnabled(false);
		textAreaTXT.setCaretPosition(textAreaTXT.getText().length());
		contentPane.add(textAreaTXT);
		textAreaTXT.setText(elementiListaTXT);
		
// aggiunta della barra per scorrere===================================
		JScrollPane scrollTXT = new JScrollPane(textAreaTXT);
		scrollTXT.setBounds(10, 50, 400, 404);
        getContentPane().add(scrollTXT);
//=====================================================================
        
		JTextArea textAreaPDF = new JTextArea();
		textAreaPDF.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		textAreaPDF.setBounds(1, 1, 479, 333);
		textAreaPDF.setEditable(false);
		textAreaPDF.setEnabled(false);
		textAreaPDF.setCaretPosition(textAreaPDF.getText().length());
		contentPane.add(textAreaPDF);
		textAreaPDF.setText(elementiListaPDF);
		
// aggiunta della barra per scorrere===================================
		JScrollPane scrollPDF = new JScrollPane(textAreaPDF);
		scrollPDF.setBounds(422, 50, 400, 404);
        getContentPane().add(scrollPDF);
//Fine pulsante "Scarica"============================================================
	
//Label Lista File TXT===============================================================
		JLabel lblListaFileTxt = new JLabel("Lista file CSV");
		lblListaFileTxt.setBounds(154, 13, 143, 24);
		lblListaFileTxt.setFont(new Font("Tahoma", Font.PLAIN, 20));
		contentPane.add(lblListaFileTxt);
//=================================================================================

//Label Lista File PDF===============================================================
		JLabel lblListaFilePdf = new JLabel("Lista file PDF");
		lblListaFilePdf.setBounds(562, 13, 143, 24);
		lblListaFilePdf.setFont(new Font("Tahoma", Font.PLAIN, 20));
		contentPane.add(lblListaFilePdf);
		
//Pulsante per il controllo dell'integrità
		JButton btnControllaIntegrit = new JButton("Controlla");
		btnControllaIntegrit.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnControllaIntegrit.setBounds(834, 211, 147, 49);
//imposto l'icona del pulsante===========================================
        String nomeIconaControllo = "controllo.png";
        try {
			BufferedImage iconaControllo = ImageIO.read(this.getClass().getResource(nomeIconaControllo));
			Image newimg = iconaControllo.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); 	// scale it the smooth way  
			Icon iconaControllo1 = new ImageIcon(newimg);  											// transform it back, y);
			btnControllaIntegrit.setIcon(iconaControllo1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//======================================================================
		btnControllaIntegrit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnControllaIntegrit.setEnabled(false);
				progressBar.setStringPainted(true);

				SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
					
					@Override
					protected Void doInBackground() throws Exception {
						progressBar.setVisible(true);
						progressBar.setIndeterminate(true);
						calcolaTabella(compagniaScelta, sftpChannel, session);	
						dispose();			
						return null;
					}

					@Override
					protected void done() {
						
					}
				};
				worker.execute();
			}
		});
		contentPane.add(btnControllaIntegrit);
//===============================================

//Label per il logo=============================
		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(834, 10, 145, 90);
		contentPane.add(lblNewLabel_1);
//===============================================	
		String nomeLogo = compagniaScelta + ".png";
		BufferedImage logo = ImageIO.read(this.getClass().getResource(nomeLogo));
		java.awt.Image newimg;
		if (compagniaScelta=="Berardi") {
			newimg = logo.getScaledInstance(240, 230,  java.awt.Image.SCALE_SMOOTH); // Se la compagnia è Berardi, la ingrandisco di più  
		} else newimg = logo.getScaledInstance(150, 100,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		
		Icon logo1 = new ImageIcon(newimg);  // transform it back
		lblNewLabel_1.setIcon(logo1);
//=================================================================================	
		
		progressBar.setStringPainted(false);
		progressBar.setString("Operazione di controllo congruità in corso...");
	
		progressBar.setBounds(32, 467, 769, 24);
		contentPane.add(progressBar);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
		
		textArea.setBounds(40, 542, 986, 93);
		contentPane.add(textArea);
		JScrollPane scrolltextArea = new JScrollPane(textArea);
		scrolltextArea.setBounds(10, 502, 812, 105);
        getContentPane().add(scrolltextArea);
        
        //JButton btnPulisci = new JButton("Pulisci cartella");
        
      //imposto l'icona del pulsante===========================================
        String nomeIconaPulisci = "pulisci.png";
        try {
			BufferedImage iconaPulisci = ImageIO.read(this.getClass().getResource(nomeIconaPulisci));
			Image newimg7 = iconaPulisci.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaPulisci1 = new ImageIcon(newimg7);  //transform it back, y);
			btnPulisci.setFont(new Font("Tahoma", Font.PLAIN, 14));
			btnPulisci.setIcon(iconaPulisci1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//======================================================================
        btnPulisci.setBounds(834, 113, 147, 49);
        btnPulisci.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int dialogButton = JOptionPane.YES_NO_OPTION;
				int dialogResult = JOptionPane.showConfirmDialog (null, "Sei sicuro di voler eliminare tutti i PDF?","Warning",dialogButton);
				if(dialogResult == JOptionPane.YES_OPTION){
					pulisciCartella();
					try {
						scaricaFile sF = new scaricaFile(compagniaScelta);
						sF.setVisible(true);
						dispose();
					} catch (JSchException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SftpException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});
        
        btnPulisci.setEnabled(false);
        if (elementiListaTXT.length()==0 & elementiListaPDF.length()!=0) {
        	btnPulisci.setEnabled(true);
        }
        
        if (elementiListaTXT.length()==0 & elementiListaPDF.length()==0 | elementiListaTXT.length()==0) {
        	btnControllaIntegrit.setEnabled(false);
        }
        contentPane.add(btnPulisci);
        
        JButton btnIndietro = new JButton("Indietro");
        btnIndietro.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		Fatture2 F2 = new Fatture2(compagniaScelta);
				F2.frmFatturazioneCicloAttivo.setVisible(true);
				dispose();
        	}
        });
        btnIndietro.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnIndietro.setBounds(834, 307, 147, 49);
        String nomeIconaIndietro = "indietro.png";
        try {
			BufferedImage iconaIndietro = ImageIO.read(this.getClass().getResource(nomeIconaIndietro));
			java.awt.Image newimg3 = iconaIndietro.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaIndietro1 = new ImageIcon(newimg3);  // transform it back, y);
			btnIndietro.setIcon(iconaIndietro1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        contentPane.add(btnIndietro);
	}
	
	public void calcolaTabella(String compagnia,ChannelSftp sftpChannel, Session sessione) {
		txtFix();	//converto prima eventuali file txt col nome non appropriato
		
		//Creo array con i nomi file PDF
		File folderA = new File(nomePathTXT);							//cartella in cui prendo i file txt
		
		//imposto un filtro per la tabella per i file .txt
		File[] listOfFilesInA = folderA.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".csv");		//MODIFICATO
            }
        });
		//File[] listOfFilesInA = folderA.listFiles();					//listo tutti i file nella cartella
		
		ArrayList<String> listaTXT = new ArrayList<String>();
		int correnteTXT =0;
		for (File fileA : listOfFilesInA) {	
			if (fileA.isFile()) {	
				correnteTXT++;
				textArea.append("\n [ "+correnteTXT+" | " + listOfFilesInA.length + " ] Sto processando il file: " + fileA.getName() + "...");
				textArea.setCaretPosition(textArea.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
				int indiceUnderscoreA = fileA.getName().indexOf(".");	//prendo la posizione dell'ultimo carattere che voglio considerare di un file
				//fileNameWithOutExt = "test.xml".replaceFirst("[.][^.]+$", "");
				//String nomeFileASenzaSuffisso = fileA.getName().substring(0,indiceUnderscoreA+1);	//prendo il nome del file senza suffisso
				String nomeFileASenzaSuffisso = fileA.getName().replaceFirst("[.][^.]+$", "");	//prendo il nome del file senza suffisso
				//System.out.println("nomeFileASenzaSuffisso " + nomeFileASenzaSuffisso);
				listaTXT.add(nomeFileASenzaSuffisso);
				}
		}
		
		//Creo array con i nomi file PDF
		File folderB = new File(nomePathPDF);
		
		//imposto un filtro per la tabella per i file .pdf
		File[] listOfFilesInB = folderB.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".pdf");
            }
        });
		
		ArrayList<String> listaPDF = new ArrayList<String>();
		int correntePDF =0;
		for (File fileB : listOfFilesInB) {	
			if (fileB.isFile()) {
				correntePDF++;
				textArea.append("\n [ "+correntePDF+" | " + listOfFilesInB.length + " ] Sto processando il file: " + fileB.getName() + "...");
				textArea.setCaretPosition(textArea.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
				int indiceUnderscoreB = fileB.getName().indexOf(".");	//prendo la posizione dell'ultimo carattere che voglio considerare di un file
       	 		//String nomeFileBSenzaSuffisso = fileB.getName().substring(0,indiceUnderscoreB+1);	//prendo il nome del file senza suffisso
				
				String nomeFileBSenzaSuffisso = fileB.getName().replaceFirst("[.][^.]+$", "");	//prendo il nome del file senza suffisso
				//System.out.println("nomeFileBSenzaSuffisso " + nomeFileBSenzaSuffisso);
				listaPDF.add(nomeFileBSenzaSuffisso);
				}
		}
		
		//Faccio l'unione delle due liste (eliminando i duplicati)
		Set<String> mergedArray = new LinkedHashSet<String>();
		mergedArray.addAll(listaTXT);
		mergedArray.addAll(listaPDF);
		//System.out.println("MERGED" +mergedArray);
		
		//converto il Set nel tipo ArrayList per accedere ai suoi elementi più facilmente
		List<String> mergedArrayList = new ArrayList<String>();
		mergedArrayList.addAll(mergedArray);		
		
		List<Integer> contatoriTXT = new ArrayList<Integer>();		//array di contatori dei file TXT
		List<Integer> contatoriPDF= new ArrayList<Integer>();		//array di contatori dei file TXT
		
		//Conto il numero di occorrenze e creo rispettivamente array di occorrenzeTXT e array di occorrenzePDF
		for (int i=0; i<mergedArray.size();i++) {
			contatoriTXT.add(Collections.frequency(listaTXT, mergedArrayList.get(i))); 	//vedo quante occorrenze per ogni file dell'array mergiato e le metto nell'array di occorrenze
			contatoriPDF.add(Collections.frequency(listaPDF, mergedArrayList.get(i)));	//vedo quante occorrenze per ogni file dell'array mergiato e le metto nell'array di occorrenze
		}
		
		//converto in array gli arraylist
		String[] arrayDiNomi = new String[mergedArray.size()];
		arrayDiNomi = mergedArray.toArray(arrayDiNomi);
		Object[] arrayDiTXT = new Object[contatoriTXT.size()];
		arrayDiTXT = contatoriTXT.toArray(arrayDiTXT);
		Object[] arrayDiPDF = new Object[contatoriPDF.size()];
		arrayDiPDF = contatoriPDF.toArray(arrayDiPDF);
		
		DefaultTableModel model = new DefaultTableModel(); 
	    
		showDataInTable(arrayDiNomi, arrayDiTXT, arrayDiPDF, compagnia, sftpChannel, sessione);	//funzione che organizza i dati in una tabella
		 JTextArea tA = new JTextArea();
		tA.setColumns(100);
		tA.setLineWrap(true);
		tA.setWrapStyleWord(true);
		tA.setSize(800, 400);
		 
		int fileTroppoGrandi = 0;
		for (File fileB : listOfFilesInB) {
			//System.out.println(fileB.length());
			 
			if (fileB.length()>=3000000) {
				fileTroppoGrandi++;
				tA.append("ATTENZIONE: Il file " + fileB.getName() +" supera le dimensioni consentite!"); 
			}
		}
		
		if (fileTroppoGrandi>0) {
			JOptionPane.showMessageDialog(null, new JScrollPane(tA), "Eccesso nelle dimensione dei file.", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public void pulisciCartella() {
		//File cartellaDaPulire = new File(nomePathPDF);C:\Users\tommolini\Desktop\provissima
		File cartellaDaPulire = new File(nomePathPDF);
		for(File file: cartellaDaPulire.listFiles()) {
			System.out.println(file.getName() + " eliminato");
		    if (!file.isDirectory()) file.delete();
		}
	}
	
//Organizzo i dati e apro il JFrame della tabella==============
	public void showDataInTable(String[] listNome, Object[] contTXT, Object[] contPDF, String company,ChannelSftp sftpChannel, Session sessione){
	     DefaultTableModel model = new DefaultTableModel(new Object[]{"Nome", "n° CSV", "n° PDF"}, 0);
	     for(int i=0;i<listNome.length;i++){
	          model.addRow(new Object[]{listNome[i], contTXT[i], contPDF[i]});
	     }

	     tabella TR = new tabella(model, company, sftpChannel, sessione); 	
	     TR.setVisible(true);
	}
//==============================================================
	
//Controlla se ci sono eventuali file .txt, elimina la parte finale che non serve e li salva come .csv
	public void txtFix() {
		//Creo array con i nomi file PDF
				File folderA = new File(nomePathTXT);							//cartella in cui prendo i file txt
				
				//imposto un filtro per la tabella per i file .txt
				File[] listOfFilesTXTInA = folderA.listFiles(new FileFilter() {
		            @Override
		            public boolean accept(File pathname) {
		                return pathname.getName().toLowerCase().endsWith(".txt");		//MODIFICATO
		            }
		        });				
		
				for (File fileTXT : listOfFilesTXTInA) {	
					if (fileTXT.isFile()) {
						String nomeFileTXTSenzaSuffisso = fileTXT.getName().replaceFirst("[.][^.]+$", "");	//prendo il nome del file senza estensione
						int indiceUnderscoreTXT = nomeFileTXTSenzaSuffisso.indexOf(")");	//prendo la posizione dell'ultimo carattere che voglio considerare di un file
						String nomeFileTXTSenzaSuffissoNeEstensione = nomeFileTXTSenzaSuffisso.substring(0,indiceUnderscoreTXT+1);	//prendo il nome del file senza suffisso e senza estensione
						//System.out.println("nomeFileTXTSenzaSuffissoNeEstensione " + nomeFileTXTSenzaSuffissoNeEstensione);	
						fileTXT.renameTo(new File(nomePathTXT +"\\"+ nomeFileTXTSenzaSuffissoNeEstensione+".csv"));
						//System.out.println("path salvato editato " + nomePathTXT + nomeFileTXTSenzaSuffissoNeEstensione+".csv");
						}
				}
	}
	
//Stampa elenco dei file nella finestra tramite JList
	public Vector stampaLista (ChannelSftp canale, String cartellaRemota) throws SftpException {
		lista = canale.ls(cartellaRemota + "*.*");							//path su SFTP dove prendere i file
		segnaLista = lista.size();

		return lista;
	}
//===========================================================================
	
//Connessione al server SFTP
	public void connetti(String host, String user, String pwd, int porta) throws JSchException, SftpException, IOException {

	    try {
	        JSch jsch = new JSch();
	        session = jsch.getSession(user, host, porta);				//creo una nuova sessione verso l'host
	        session.setPassword(pwd);									//immetto la password
	        session.setConfig("StrictHostKeyChecking", "no");
	        //System.out.println("Tentativo di connessione...");
	        session.connect();											//avvio la sessione
	        //System.out.println("Connessione stabilita.");
	        //System.out.println("Canale SFTP in creazione...");
	        channel = session.openChannel("sftp");
	        channel.connect();
	        sftpChannel = (ChannelSftp)channel;							//creo nuovo canale SFTP
	        //sftpChannel.connect();									//mi connetto al canale
	        //System.out.println("Canale SFTP creato.");
	        
			listaFileTXT = stampaLista(sftpChannel, nomePathRemotoTXT);	//trova la lista dei file TXT nella cartella dei TXT		//prendo la lista dei file remoti per stamparla
			listaFilePDF = stampaLista(sftpChannel, nomePathRemotoPDF);	//trova la lista dei file PDF nella cartella dei TXT		//prendo la lista dei file remoti per stamparla

	    } catch(JSchException | SftpException e) {						//gestisco le eccezioni	
	    	JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP");
			JLabel label = new JLabel("ERRORE! Non è stato possibile raggiungere il server SFTP");
			contentPane.add(label, BorderLayout.NORTH);
	        //System.out.println(e);
	    } 
	}
}
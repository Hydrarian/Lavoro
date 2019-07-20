import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.io.FileUtils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import com.jcraft.jsch.Session;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.JScrollBar;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Font;
import java.awt.Image;
import javax.swing.JTextArea;


public class tabella extends JFrame {
	
	private JPanel contentPane;
	private JTable table;
	private JButton btnNewButton;
	private JButton btnAggiorna;
	
 //DOWNLOAD FILE TXT:
	static String nomePathTXT;				//path di origine txt	
	static String nomePathRemotoTXT;		//path di destinazione txt
	
 //DOWNLOAD FILE PDF
	static String nomePathPDF;				//path di origine pdf
    static String nomePathRemotoPDF;		//path di destinazione pdf
    
    int segnaLista;							//numero di file rimanenti per la fine dell'upload
    boolean uploadCompletato;				//true=pulsante carica ha finito il suo lavoro ; false=ho annullato l' upload
    String uploadFileInCorso;				//nome del file che si sta caricando
    String elementiListaTXT;				//DA CAMBIARE: path di origine txt
    String elementiListaPDF;				//DA CAMBIARE: path di origine pdf
    String pathDelFileDiLog = "\\\\BER-OFFICE\\FattureB2B\\Cli";
    
    static Session session;
    JTextArea textArea = new JTextArea();	//area di testo dove mostrare i file in caricamento
    JProgressBar progressBar = new JProgressBar();
    
    boolean uploadSospeso = false;		//variabile che determina la sospensione dell'upload nel caso di upload fallito di un pdf o csv
    boolean filtro=false;				//variabile filtro ON/OFF
	public static void main(DefaultTableModel model, String company, ChannelSftp sftpChannel, Session sessione) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				try {
					String compagniaScelta=company;
					
					//configuro i path locali e remoti giusti per la company corrente
							switch (compagniaScelta) {
							case "Berardi": 
								nomePathRemotoTXT="B2B/ATTIVO/009306_00628991200/";	
								nomePathRemotoPDF="B2B/ATTIVO/009306_00628991200/";	
								//nomePathTXT="C:\\Users\\tommolini\\Desktop\\FCA\\csv";
								//nomePathPDF="C:\\Users\\tommolini\\Desktop\\FCA\\pdf";
								nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\BER";
								nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\BER";	
					            break; 
							case "Vitman": 
								nomePathRemotoTXT="B2B/ATTIVO/009308_02091110409/";	
								nomePathRemotoPDF="B2B/ATTIVO/009308_02091110409/";	
								nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\VIT";
								nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\VIT";		
					            break; 
							case "Vibolt": 
								nomePathRemotoTXT="B2B/ATTIVO/009307_02100250352/";	
								nomePathRemotoPDF="B2B/ATTIVO/009307_02100250352/";	
								nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\VIB";
								nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\VIB";	
					            break; 
							default:
								JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP");
							}
					//================================================================================================	
							
					ChannelSftp canaleSFTP=sftpChannel;
					session=sessione;
					JFrame.setDefaultLookAndFeelDecorated(true);
					tabella frame = new tabella(model,compagniaScelta, canaleSFTP, sessione);
					frame.setVisible(true);
					frame.setLocationRelativeTo(null);  // *** this will center your app ***
				} catch (Exception e) { e.printStackTrace(); }
			}
		});
	}

	
	public tabella(DefaultTableModel model, String compagnia, ChannelSftp sftpChannel, Session sessione) {
		
		ChannelSftp canSFTP=sftpChannel;
		
		setTitle("Tabella risultati congruit\u00E0");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 832, 745);
		setLocationRelativeTo(null);  // *** this will center your app ***
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
//TABELLA=============================================================================
		table = new JTable(model){
	        @Override
	        	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int columnIndex) {
	            JComponent component = (JComponent) super.prepareRenderer(renderer, rowIndex, columnIndex);  
	            int modelColumn = convertColumnIndexToModel(columnIndex);
	            int modelRow = convertRowIndexToModel(rowIndex);
	            if((!getValueAt(rowIndex, 1).toString().equalsIgnoreCase("1") || !getValueAt(rowIndex, 2).toString().equalsIgnoreCase("1") ) && (columnIndex == 1 || columnIndex == 2)) {
	                component.setBackground(Color.RED);
	            } else {
	                component.setBackground(Color.GREEN);
	            } 

	            if (modelColumn == 0 ) { component.setBackground( Color.WHITE);}	//coloro di bianco la prima colonna
	            
	            return component;
	        }
	    };
		table.setEnabled(false);
	   
		table.setFillsViewportHeight(true);
		table.setBackground(Color.WHITE);
		table.setFont(new Font("Tahoma", Font.PLAIN, 14));
	   
//Coloro l'header====================================
	    table.getTableHeader().setOpaque(false);
	    table.getTableHeader().setBackground(Color.yellow);
	    table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 15));
//===================================================
	    
		table.setBounds(10, -3, 723, 473);
		table.setRowHeight(30);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setColumnSelectionAllowed(true);
		table.setSelectionBackground(Color.BLUE);
		JTableUtilities.setCellsAlignment(table, SwingConstants.CENTER);
		
		final TableRowSorter<TableModel>sorter= new TableRowSorter<TableModel>(model);
		table.setRowSorter(sorter);
		
		contentPane.add(table);
//=============================================================================================
		
		JScrollPane scrolltable = new JScrollPane(table);
		scrolltable.setBounds(10, 24, 798, 445);                     
        getContentPane().add(scrolltable);
        
        table.getTableHeader().setReorderingAllowed(false);		//rendo non trascinabili le colonne
//PULSANTE CHIUDI========================================================
        JButton btnChiudi = new JButton("Chiudi");
        btnChiudi.setFont(new Font("Tahoma", Font.PLAIN, 15));
        btnChiudi.setBounds(685, 482, 123, 45);
        
//imposto l'icona del pulsante===========================================
        String nomeIconaChiudi = "chiudi.png";
        try {
			BufferedImage iconaChiudi = ImageIO.read(this.getClass().getResource(nomeIconaChiudi));
			Image newimg4 = iconaChiudi.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaChiudi1 = new ImageIcon(newimg4);  // transform it back, y);
			btnChiudi.setIcon(iconaChiudi1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//=======================================================================
        
        btnChiudi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
        contentPane.add(btnChiudi);
//=======================================================================
   
//PULSANTE ESPORTA=======================================================
        btnNewButton = new JButton("Esporta");
        btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 15));
        
//imposto l'icona del pulsante===========================================
        String nomeIconaEsporta = "esporta.png";
        try {
			BufferedImage iconaEsporta = ImageIO.read(this.getClass().getResource(nomeIconaEsporta));
			Image newimg3 = iconaEsporta.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaEsporta1 = new ImageIcon(newimg3);  // transform it back, y);
			btnNewButton.setIcon(iconaEsporta1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//========================================================================
        
        btnNewButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
			try {
				exportTable(table,pathDelFileDiLog);		//DA CAMBIARE la variabile pathDelFileDiLog
				JOptionPane.showMessageDialog(null, "Log esportato in " + pathDelFileDiLog);
			} catch (IOException e2) {
				e2.printStackTrace();
			}; //DA CAMBIARE -> PATH DOVE VIENE SALVATA L'ESPORTAZIONE LOG  
        	}
        });
        btnNewButton.setBounds(550, 482, 123, 45);
        contentPane.add(btnNewButton);
        
//Pulsante stampa========================================================
        JButton btnStampa = new JButton("Stampa");
        btnStampa.setFont(new Font("Tahoma", Font.PLAIN, 15));
        
//imposto l'icona del pulsante===========================================
        String nomeIconaStampa = "stampa.png";
        try {
			BufferedImage iconaStampa = ImageIO.read(this.getClass().getResource(nomeIconaStampa));
			Image newimg2 = iconaStampa.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaStampa1 = new ImageIcon(newimg2);  // transform it back, y);
			btnStampa.setIcon(iconaStampa1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//========================================================================
        
        btnStampa.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        	MessageFormat header = new MessageFormat("Resoconto ciclo attivo");
        	MessageFormat footer = new MessageFormat("");
        	try {
				table.print(JTable.PrintMode.NORMAL, header, footer);
			} catch (PrinterException e1) { e1.printStackTrace(); }
        	}
        });
        
        btnStampa.setBounds(415, 482, 123, 45);
        contentPane.add(btnStampa);
//====================================================================================  
        
//=======================================================================
        
//Pulsante per caricare (visibile una volta che tutte le righe diventano verdi)
        JButton btnCarica = new JButton("Carica");
        btnCarica.setBounds(10, 482, 123, 45);
//imposto l'icona del pulsante===========================================        
        String nomeIconaCarica = "carica.png";
        try {
			BufferedImage iconaCarica = ImageIO.read(this.getClass().getResource(nomeIconaCarica));
			Image newimg2 = iconaCarica.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaCarica1 = new ImageIcon(newimg2);  		// transform it back, y);
			btnCarica.setIcon(iconaCarica1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//========================================================================        
        btnCarica.setFont(new Font("Tahoma", Font.PLAIN, 15));
       
//rendo visibile il pulsante Carica solo se tutte le righe sono verdi ( corrispondenza file 1 a 1)
        int valoriEsatti=0;
		for(int i=0; i< model.getRowCount(); i++) {
        	for(int j=1; j < model.getColumnCount(); j++) {
        		
        		if ((Integer)model.getValueAt(i,j)==1) {
        			valoriEsatti++;
        			}
        	}
        }
		
		if (table.getRowCount()>0 && valoriEsatti==model.getRowCount()*2) {
			btnCarica.setEnabled(true);
		} else btnCarica.setEnabled(false);
		
  		btnCarica.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent arg0) {
  				btnCarica.setEnabled(false);

				//configuro i path locali e remoti giusti per la company corrente
				switch (compagnia) {
				case "Berardi": 
					nomePathRemotoTXT="B2B/ATTIVO/009306_00628991200/";	
					nomePathRemotoPDF="B2B/ATTIVO/009306_00628991200/";	
					nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\BER";
					nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\BER";	
		            break; 
				case "Vitman": 
					nomePathRemotoTXT="B2B/ATTIVO/009308_02091110409/";	
					nomePathRemotoPDF="B2B/ATTIVO/009308_02091110409/";	
					nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\VIT";
					nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\VIT";		
		            break; 
				case "Vibolt": 
					nomePathRemotoTXT="B2B/ATTIVO/009307_02100250352/";	
					nomePathRemotoPDF="B2B/ATTIVO/009307_02100250352/";	
					nomePathTXT="\\\\BER-OFFICE\\ARCHIVA_Export\\VIB";
					nomePathPDF="\\\\BER-OFFICE\\FattureB2B\\Cli\\VIB";	
		            break; 
				default:
					JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP");
				}
		//====================================================================
  				start(model, compagnia, sftpChannel, sessione);
  				//System.out.print("Start fatto");
//Nascondi il pulsante "Scarica"==============================================
  		        if (uploadCompletato==true) {
  					btnCarica.setEnabled(false);
  				}
//=================================================================================
  			}
  		});
        contentPane.add(btnCarica);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        textArea.setBounds(58, 540, 900, 83);
        contentPane.add(textArea);
        
        JScrollPane scrolltable2 = new JScrollPane(textArea);
		scrolltable2.setBounds(10, 577, 798, 119);                     
        getContentPane().add(scrolltable2);
        
        progressBar.setStringPainted(false);
		progressBar.setString("Operazione di controllo congruità in corso...");
	
		progressBar.setBounds(10, 540, 798, 29);
		contentPane.add(progressBar);
		
//Pulsante per aggiornare jtable===================
        btnAggiorna = new JButton("Aggiorna");
        btnAggiorna.setFont(new Font("Tahoma", Font.PLAIN, 15));
        btnAggiorna.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		btnAggiorna.setEnabled(false);
        		btnStampa.setEnabled(false);
        		btnNewButton.setEnabled(false);
        		//btnApri.setEnabled(false);
        		progressBar.setStringPainted(true);

				SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
					
					@Override
					protected Void doInBackground() throws Exception {
	
						progressBar.setVisible(true);
						progressBar.setIndeterminate(true);
						try {
							scaricaFile ScFi= new scaricaFile(compagnia);
							ScFi.calcolaTabella(compagnia, canSFTP, sessione);
						} catch (JSchException | SftpException | IOException e1) { e1.printStackTrace(); }
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
        		
        btnAggiorna.setBounds(145, 482, 123, 45);
        
        //imposto l'icona del pulsante===========================================
        String nomeIconaAggiorna = "aggiorna.png";
        try {
			BufferedImage iconaAggiorna = ImageIO.read(this.getClass().getResource(nomeIconaAggiorna));
			Image newimg = iconaAggiorna.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaAggiorna1 = new ImageIcon(newimg);  // transform it back, y);
			btnAggiorna.setIcon(iconaAggiorna1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//==================================================================================
        contentPane.add(btnAggiorna);
        
//Pulsante filtro irregolarità==========================================
        JButton buttonFiltra = new JButton("Filtra");
        buttonFiltra.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (filtro ==false) {
        			sorter.setRowFilter(RowFilter.regexFilter("^0$"));		//filtro solo le righe che hanno uno 0 (da solo)
        			filtro=true;
        		} else {
        			sorter.setRowFilter(null);								//tolgo il filtro
        			filtro=false;
        		}
        	}
        });
        buttonFiltra.setFont(new Font("Tahoma", Font.PLAIN, 15));
        buttonFiltra.setBounds(280, 482, 123, 45);
        
        //imposto l'icona del pulsante===========================================
        String nomeIconaFiltra = "filtra.png";
        try {
			BufferedImage iconaFiltra = ImageIO.read(this.getClass().getResource(nomeIconaFiltra));
			Image newimgf = iconaFiltra.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaFiltra1 = new ImageIcon(newimgf);  // transform it back, y);
			buttonFiltra.setIcon(iconaFiltra1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
        
        contentPane.add(buttonFiltra);
	}
//==================================================================================
	
	public void start(DefaultTableModel model, String compagnia, ChannelSftp sftpChannel, Session sessione) {
		
		SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
			
			@Override
			protected Void doInBackground() throws Exception {

  				File f = new File(nomePathTXT);
  				ArrayList<File> listaFileTXT = new ArrayList<File>(Arrays.asList(f.listFiles()));
  				File f2 = new File(nomePathPDF);
  				ArrayList<File> listaFilePDF = new ArrayList<File>(Arrays.asList(f2.listFiles()));
  				///////////////////////////////////////////////////////////////////////
  				
  				try {
  					uploadFile(nomePathPDF, nomePathRemotoPDF, listaFilePDF, sftpChannel, compagnia, sessione, model);
				} catch (IOException e) { e.printStackTrace(); }			
  				//carico tutti i file TXT nella cartella selezionata
  				try {
					
  					if (uploadSospeso==false) {			//carica i file csv se e solo se l'operazione di upload non è stata sospesa
  						//System.out.println("Upload sospeso");
  						uploadFile(nomePathTXT, nomePathRemotoTXT, listaFileTXT, sftpChannel, compagnia, sessione, model);
  					}
  					
				} catch (IOException e) { e.printStackTrace(); }			//carico tutti i file PDF nella cartella selezionata
  				try {
  					exportLogTransfer(textArea,pathDelFileDiLog);
  					JOptionPane.showMessageDialog(null, "Upload completato.");  					
  	   				dispose();
  					scaricaFile ScFi= new scaricaFile(compagnia);
  					ScFi.calcolaTabella(compagnia, sftpChannel, sessione);
  				} catch (JSchException | SftpException | IOException e1) { e1.printStackTrace(); }
  				
  				sftpChannel.disconnect();	//chiudo il canale SFTP
				return null;
			}

			@Override
			protected void done() {
				
			}
		};
		worker.execute();
	}
	
	public String prendiListaFile(String path) throws SftpException {
		File folder = new File(path);											//path= da passare in input sia quello dei TXT che PDF
		File[] listOfFiles = folder.listFiles();
		String elementiLista = "";
		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
			  elementiLista = elementiLista + listOfFiles[i].getName() + "\n";
		  }
		}
		return elementiLista;
	}
//===================================================================================================================
	
	public void uploadFile(String cartellaOrigine, String cartellaDestinazione, ArrayList<File> listaFile, ChannelSftp sftpChannel, String compagnia, Session sessione, DefaultTableModel model) throws IOException {
		//Connessione SFTP (con passaggio di parametri ( richiama la funzione connetti)===================
		try {
			String user = null;
			String host="ftp.archivagroup.it";
			String pwd = null;
			switch (compagnia) {
					case "Berardi": 
						user="berardibulloneriesrl";	
						pwd="fzU4@wUD_Joy";
			            break; 
					case "Vitman": 
						user="vitmansrl";
						pwd="HMFs>3cm@tez";
			            break; 
					case "Vibolt": 
						user="viboltsrlunipersonale";
						pwd="QeHz^BHK>rGc";
			            break; 
					default:
						JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP");
			}
			//================================================================================================	
	        JSch jsch = new JSch();
	        session = jsch.getSession(user, host, 2411);				//creo una nuova sessione verso l'host
	        session.setPassword(pwd);									//immetto la password
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.connect();											//avvio la sessione
	        sftpChannel = (ChannelSftp) session.openChannel("sftp");
	        sftpChannel.connect();
	        sftpChannel = (ChannelSftp)sftpChannel;							//creo nuovo canale SFTP

	    } catch(JSchException e) {						//gestisco le eccezioni	
	    	JOptionPane.showMessageDialog(null, "ERRORE! Non è stato possibile raggiungere il server SFTP");
			JLabel label = new JLabel("ERRORE! Non è stato possibile raggiungere il server SFTP");
			contentPane.add(label, BorderLayout.NORTH);
	    } 
		
		segnaLista=listaFile.size();
		
		String CartellaProcessati= "Fatture inviate/";			//creo la cartella dove poi sposto le fatture inviate
		File dir = new File(CartellaProcessati);	
		dir.mkdir();
		
		//le prossime due righe mi servono per trasformare le stringhe in File per la funzione copyFileToDirectory che mi sposta i file
	
		File cartellaDelFileDaUploadare = new File(cartellaOrigine+"/"+CartellaProcessati);
		   
			for (int i=0; i<listaFile.size(); i++) { // Iterate objects in the list to get file/folder names.		   
			   
			   uploadFileInCorso =listaFile.get(i).toString();
			   //System.out.println("uploadFileInCorso" + uploadFileInCorso);
			   File fileDaUploadare = new File(uploadFileInCorso);
			   if (!fileDaUploadare.isDirectory()) {
				   segnaLista = segnaLista - 1;
				   Boolean success = false;
				   try {
					   textArea.append("\n Sto caricando il file " + fileDaUploadare.getName() + "...");
					   sftpChannel.put(uploadFileInCorso, cartellaDestinazione);	//carico i file nella cartella selezionata
					   textArea.append("\n Caricamento di " + fileDaUploadare.getName() + " completato.");
					   textArea.setCaretPosition(textArea.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
					   success=true;
				   } catch (SftpException e) {
					   //System.out.println("Sospendo l'upload");
					   uploadSospeso=true;
					   success = false;
					   textArea.append("\n Upload del file " + fileDaUploadare.getName() + " non riuscito. L'intero download verrà sospeso.");
					   textArea.setCaretPosition(textArea.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
					   JOptionPane.showMessageDialog(null, "Upload del file :" + uploadFileInCorso + " non riuscito. L'intero download verrà sospeso.");
					   e.printStackTrace(); 
					   break; 
				   } catch (Exception e) { 
					   //System.out.println("Sospendo l'upload");
					   uploadSospeso=true;
					   success = false;
					   textArea.append("\n Upload del file " + fileDaUploadare.getName() + " non riuscito. L'intero download verrà sospeso.");
					   textArea.setCaretPosition(textArea.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
					   JOptionPane.showMessageDialog(null, "Upload del file :" + uploadFileInCorso + " non riuscito. L'intero download verrà sospeso.");
					   e.printStackTrace(); 
					   break; 
					   }
				   if (success==true) {	//se l'upload è andato a buon fine sposto i file nella cartella processati
					   //muovo il file nella cartella dei file inviati
					   FileUtils.copyFileToDirectory(fileDaUploadare, cartellaDelFileDaUploadare);
					   fileDaUploadare.delete();
				   }
		   		}
		   }
		   //exportLogTransfer(textArea,pathDelFileDiLog);
		   uploadCompletato=true;
	}
	
//Funzione che esporta tutto il contenuto della JTextArea che mostra lo storico dei trasferimenti operati sui file
	public void exportLogTransfer(JTextArea logJtext, String path) throws IOException {

        Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
		File file = new File(path + "/" + dateFormat.format(date) +"(Trasferimento)" + ".log") ;		//DA CAMBIARE: CREARE UNA CARTELLA APPOSITA PER IL LOG
		file.createNewFile();

		String nomeFileLog = FileUtils.readFileToString(file, "UTF-8");
		try (BufferedWriter fileOut = new BufferedWriter(new FileWriter(file))) {
		    logJtext.write(fileOut);									//scrivo il contenuto della jtextarea all'interno del file di log
		}
	 }
	
	
//Funzione per prendere la data che da il nome al file di log=================================
	public String getLogName() throws IOException {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
		File file = new File(dateFormat.format(date) + ".log") ;
		
		String nomeFileLog = FileUtils.readFileToString(file, "UTF-8");
		return nomeFileLog;
	}
//==========================================================================================
	
//Funzione per esportare la tabella in .txt
	public void exportTable(JTable table, String path) throws IOException {
		
        TableModel model = table.getModel();
        
        Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
		File file = new File(path + "/" +  "(Lista file processati) " + dateFormat.format(date) + ".log") ;		//DA CAMBIARE: CREARE UNA CARTELLA APPOSITA PER IL LOG
		file.createNewFile();
		String nomeFileLog = FileUtils.readFileToString(file, "UTF-8");

        FileWriter out = new FileWriter(file);
        
        //scrivo gli header delle colonne
        //for(int i=0; i < model.getColumnCount(); i++) {
        		out.write(model.getColumnName(0) /*+ "\t\t"*/);
        //}
        out.write("\n");

        //scrive i dati delle celle della tabella
        for(int i=0; i< model.getRowCount(); i++) {
        	//for(int j=0; j < model.getColumnCount(); j++) {
        		out.write(model.getValueAt(i,0).toString()/*+"\t"*/);
        //}
        out.write("\n");
        }
    out.close();
	}
}
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.printing.Orientation;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

//librerie per SSH (SFTP)
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.Sides;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;

import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.JTable;
import java.awt.Component;

public class archiviaFile extends JFrame {

	private JPanel contentPane;
	
    Vector<ChannelSftp.LsEntry> list;	//lista di file presenti nella cartella remota
    //Vector<ChannelSftp.LsEntry> lista;
    File[] listaF;
    ChannelSftp sftpChannel;			//canale SFTP per il trasferimento dei file
    Session session;					//nome delle sessione SFTP
    String downloadFileInCorso;			//nome del file che si sta scaricando.
    int segnaLista;						//numero di file rimanenti per la fine del download
    String nomePath;
    String path;						//PATH IN CUI HO SCARICATO LE FATTURE CICLO PASSIVO E A CUI HO APPLICATO IL BARCODE
    String pathArchiviati;
    String pathAllegati;
    boolean downloadCompletato;			//true=pulsante scarica ha finito il suo lavoro ; false=ho annullato il download
    JLabel lblNewLabel = new JLabel();
    boolean dwnInCorso=false;			//mi serve per capire se sto scaricando i file oppure no, così aggiorno il task che mostra il progresso 
    String elementiLista;				//lista da mostrare sulla JTextArea
    int numeroFileSuSFTP;
    List<String> listaContenutoCartellaScaricati;
    JScrollPane scrollPane;
    String fileDaArchInCorso;				//nome del file che si sta caricando
    List<String> listaFileStampati = new ArrayList<String>();
    JTable table;

//CREARE UNA LISTA DEI FILE DA STAMPARE, IN CUI SI AGGIUNGE QUANDO SI SELEZIONA DA CHECKBOX E IN CUI SI TOGLIE QUANDO SI DESELEZIONA
	public static void main(String args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					archiviaFile frame = new archiviaFile(args);
					frame.setVisible(true);
					frame.setResizable(false);
					//frame.setUndecorated(true);
				} catch (Exception e) { e.printStackTrace(); }
			}
		});
	}
	
//inizializzazione del programma e dell'interfaccia grafica===============================================
	public archiviaFile(String compagniaScelta) throws JSchException, SftpException, IOException {
		
		switch (compagniaScelta) {
		case "Berardi": 
			path="\\\\BER-OFFICE\\FattureB2B\\For\\BER\\Da stampare\\Barcode\\"; 	//DA CAMBIARE 	
			pathAllegati="\\\\BER-OFFICE\\FattureB2B\\For\\BER\\Da stampare\\";	//DA CAMBIARE
			pathArchiviati="\\\\BER-OFFICE\\FattureB2B\\For\\BER\\Archiviati\\"; 	//DA CAMBIARE	
            break; 
		case "Vitman": 
			path="\\\\BER-OFFICE\\FattureB2B\\For\\VIT\\Da stampare\\Barcode\\"; 	//DA CAMBIARE	
			pathAllegati="\\\\BER-OFFICE\\FattureB2B\\For\\VIT\\Da stampare\\";	//DA CAMBIARE
			pathArchiviati="\\\\BER-OFFICE\\FattureB2B\\For\\VIT\\Archiviati\\"; 	//DA CAMBIARE
            break; 
		case "Vibolt": 
			path="\\\\BER-OFFICE\\FattureB2B\\For\\VIB\\Da stampare\\Barcode\\"; 	//DA CAMBIARE	
			pathAllegati="\\\\BER-OFFICE\\FattureB2B\\For\\VIB\\Da stampare\\";	//DA CAMBIARE
			pathArchiviati="\\\\BER-OFFICE\\FattureB2B\\For\\VIB\\Archiviati\\"; 	//DA CAMBIARE
            break; 
		default:
			//System.out.println("Errore in stampaFile.");
		}
		
		JPanel contentPane;
	    
	    JCheckBox checkbox;
	    
		setTitle("Fatturazione ciclo passivo");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) { e.printStackTrace(); } catch (InstantiationException e) {e.printStackTrace();} catch (IllegalAccessException e) {e.printStackTrace();} catch (UnsupportedLookAndFeelException e) {e.printStackTrace();}
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 979, 549);
		setResizable(false);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
//=====================================================================
        
//Titolo a video della lista
		Fatture1 f = new Fatture1(compagniaScelta);
		JLabel lblListaDeiFile = f.initializeLabel("Fatture archiviate:", 36, 42, 233, 39, 20);     
		JLabel label = f.initializeLabel("",  777, 23, 158, 90, 20);     
		

		JButton btnIndietro = f.createJButton("Indietro", "indietro.png", true, 757, 460, 188, 44);
		JButton btnristampaSelezionati = f.createJButton("Ristampa selezionati", "stampa.png", true, 36, 460, 188, 44);
        JButton buttonVisualizza = f.createJButton("Visualizza", "visualizza.png", true, 257, 460, 188, 44);
        
        btnIndietro.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		//System.out.println(compagniaScelta);
				Fatture1 f1 = new Fatture1(compagniaScelta);
				f1.frmFatturazioneCicloPassivo.setVisible(true);
				dispose();
        	}
        });
        contentPane.add(btnIndietro);
		contentPane.add(lblListaDeiFile);
		contentPane.add(label);

        Object[] columns = {"Nome File", "Da stampare"};
        Object[][] data = null;
        
        DefaultTableModel model = new DefaultTableModel(data, columns);
        File folderA = new File(pathArchiviati+"Barcode\\");  
		File[] listOfFilesInA = folderA.listFiles();
    	ArrayList<String> listaFile = new ArrayList<String>();
    	
//Aggiungo le righe in tabella e leggo sul file del numero delle ristampe
    	String nomeFileInTxt= null;
    	String numeroVolteStampato = null;
		for (File fileA : listOfFilesInA) {	

        	String nomeFile = fileA.getName();

        	/*BufferedReader in = new BufferedReader(new FileReader(pathArchiviati+"Barcode\\prova.txt"));
        	String line = null;
        	System.out.println(fileA.getName() + " 0");
     		while( (line = in.readLine())!= null ){
     		        // \\s+ means any number of whitespaces between tokens
     		       String [] tokens = line.split("\\s+");
     		       nomeFileInTxt = tokens[0];
     		       numeroVolteStampato = tokens[1];
     		       
     		      if (fileA.getName().equals(nomeFileInTxt)) {
     		    	  System.out.println(fileA.getName() + "   " + nomeFileInTxt);*/
                  	  model.addRow(new Object[]{nomeFile, Boolean.FALSE});			//QUI DEVO METTERE LA VARIABILE NUMERO RISTAMPE
                  	/*}
     		    }*/
        }
//==================================================================
		
        final JCheckBox checkBox = new JCheckBox();

        table = new JTable(model) {        	
            private static final long serialVersionUID = 1L;
            
          	/*public boolean isCellEditable(int row, int column){  //toglie l'edit dalle righe
                return false;  
            }*/
            
            
            public boolean isCellEditable(int row, int column)
            {
                // make read only fields except column 0,13,14
                return column == 1;
            }
            
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return Boolean.class;
                    /*case 2:
                    	return JButton.class;*/
                    default:
                        return String.class;
                }
            }
        };
        table.setRowHeight(30);
        table.setBounds(2, 5, 300, 500);
        contentPane.add(table);
        //table.setPreferredScrollableViewportSize(table.getPreferredSize());
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(36, 86, 909, 361);
        getContentPane().add(scrollPane);
        
        JCheckBox checkBox_1 = new JCheckBox("Deseleziona tutti");
        JCheckBox chckbxSelezionaTutti = new JCheckBox("Seleziona tutti");
//SELEZIONA TUTTI============================================
        chckbxSelezionaTutti.setFont(new Font("Tahoma", Font.PLAIN, 17));
        chckbxSelezionaTutti.setBounds(702, 52, 148, 25);
        chckbxSelezionaTutti.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                for (int row=0; row < table.getRowCount(); row++) {
                	checkBox_1.setSelected(false);
                    table.setValueAt(true, row, 1);
                }
            } 
        } );
        contentPane.add(chckbxSelezionaTutti);
//============================================================
        btnristampaSelezionati.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
					ristampaSelezionati(compagniaScelta,table,listaFile);
					archiviaFile a= new archiviaFile(compagniaScelta);
					a.setVisible(true);
					dispose();
				} catch (InvalidPasswordException e2) {e2.printStackTrace();} catch (IOException e2) {e2.printStackTrace();} catch (PrintException e2) {e2.printStackTrace();} catch (PrinterException e2) {e2.printStackTrace();} catch (JSchException e2) {e2.printStackTrace();} catch (SftpException e2) {e2.printStackTrace();}		
        	}
        });
        
        
        int numeroFattureArchiviate=listOfFilesInA.length-1;
        JLabel lblNewLabel_1 =f.initializeLabel(""+numeroFattureArchiviate, 222, 46, 96, 30, 20);
        
        buttonVisualizza.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                //for (int row=0; row < table.getRowCount(); row++) {
                	int selectedRow = table.getSelectedRow();
                    try {
                  	  //System.out.println(pathArchiviati +(String) table.getValueAt(selectedRow, 2));
                        Desktop.getDesktop().open(new File(pathArchiviati +"\\Barcode\\"+(String) table.getValueAt(selectedRow, 0)));
                    } catch (IOException e1) {e1.printStackTrace();}
            		} 
        } );
        
        //Con il doppio click del mouse apro il file della riga===========================
        table.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                  if (e.getClickCount() == 2) {
                      int selectedRow = table.getSelectedRow();
                      try {
                    	  //System.out.println(pathArchiviati +(String) table.getValueAt(selectedRow, 2));
                          Desktop.getDesktop().open(new File(pathArchiviati +"\\Barcode\\"+(String) table.getValueAt(selectedRow, 0)));
                      } catch (IOException e1) { e1.printStackTrace();}
                      }
                  }
              });
        
        contentPane.add(buttonVisualizza);
        contentPane.add(lblNewLabel_1);
        contentPane.add(btnristampaSelezionati);
     }

//==========================================================================================================
	
	//funzione per la stampa dei file
	public void stampaDoc(String filePath) throws InvalidPasswordException, IOException, PrinterException {
		PDDocument doc = PDDocument.load(new File(filePath));
		PDFPrintable printable = new PDFPrintable(doc);
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(printable);
		job.print();
		doc.close();																	//chiudo il documento
	}
	
	@SuppressWarnings("deprecation")
	public void ristampaSelezionati(String compagniaScelta , JTable tabella, ArrayList<String> listaFile) throws InvalidPasswordException, IOException, PrintException, PrinterException, JSchException, SftpException {
		int rows = tabella.getRowCount();
		//System.out.println("tabella.getRowCount()" + tabella.getRowCount());
		List<String> listaFileDaRistampare = new ArrayList<String>(); 
		//System.out.println(rows);
		
		//prendo tutti i valori true della seconda colonna della tabella e memorizzo in nomi dei file (prima colonna) in un arraylist che poi manderò in stampa
		for(int i=0; i<rows; i++) {
			//System.out.println(tabella.getValueAt(i,1));
			if((boolean) tabella.getValueAt(i,1)==true) {
				listaFileDaRistampare.add((String) tabella.getValueAt(i,0));
				//System.out.println("tabella.getValueAt(i,0) "+(String) tabella.getValueAt(i,0));
			}
		}
	    
		for(int i=0; i<listaFileDaRistampare.size(); i++) {
			stampaDoc(pathArchiviati +"\\Barcode\\" +listaFileDaRistampare.get(i));

//controllo se il file ha allegato la copia di cortesia e se c'è la stampo tutta
			    File percorsoDaStampare = new File(pathArchiviati);													//DA MODIFICARE
			    File [] fileInDaStampare = percorsoDaStampare.listFiles();											//elenco tutti i file della cartella del percorso path
			    //System.out.println("fileInDaStampare " + fileInDaStampare);
			    String nomeFileArchivaSenzaEstensione = listaFileDaRistampare.get(i).replaceFirst("[.][^.]+$", "");	//prendo solo il nome file escludendo l'estensione
			    for (int k = 0; k < fileInDaStampare.length; k++){													//ciclo tutti i file nella stampati

		    		String nomeFileAllegato = fileInDaStampare[k].getName();
		    		
		    		//se esiste un allegato del file col barcode lo stampo tutto
			    	if(fileInDaStampare[k].isFile() & nomeFileAllegato.equals(nomeFileArchivaSenzaEstensione+"_A1.pdf")) {
			    		stampaDoc(percorsoDaStampare +"\\"+ fileInDaStampare[k].getName());
			    	} 
			    }	
		}
	}
}
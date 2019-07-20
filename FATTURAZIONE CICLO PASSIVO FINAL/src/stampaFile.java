import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.printing.Orientation;
import org.apache.pdfbox.printing.PDFPageable;

import com.itextpdf.awt.geom.Rectangle;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
//librerie per SSH (SFTP)
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;

import javax.swing.SwingConstants;
import javax.swing.JTable;

public class stampaFile extends JFrame {

	private JPanel contentPane;
	
    Vector<ChannelSftp.LsEntry> list;	//lista di file presenti nella cartella remota
    //Vector<ChannelSftp.LsEntry> lista;
    File[] listaF;
    ChannelSftp sftpChannel;			//canale SFTP per il trasferimento dei file
    Session session;					//nome delle sessione SFTP
    String downloadFileInCorso;			//nome del file che si sta scaricando
    int segnaLista;						//numero di file rimanenti per la fine del download
    String nomePath;
    String path;						
    String pathBarcode;					//PATH IN CUI HO SCARICATO LE FATTURE CICLO PASSIVO E A CUI HO APPLICATO IL BARCODE
    String pathDaStampare;				//path in cui ho gli allegati da stampare
    String pathArchiviati;
    boolean downloadCompletato;			//true=pulsante scarica ha finito il suo lavoro ; false=ho annullato il download
    JLabel lblNewLabel = new JLabel();
    boolean dwnInCorso=false;			//mi serve per capire se sto scaricando i file oppure no, così aggiorno il task che mostra il progresso 
    String elementiLista;				//lista da mostrare sulla JTextArea
    int numeroFileSuSFTP;
    List<String> listaContenutoCartellaScaricati;
    JScrollPane scrollPane;
    int numeroDiFileDaStampare=0;
    List<String> listaFileDaStampare = new ArrayList<String>(); 
    
    Object[] listaFileStampatiDaArch;	//array di oggetti che verrà riempito con i file da archiviare
    List<String> listaFileStampati = new ArrayList<String>();
    //CREARE UNA LISTA DEI FILE DA STAMPARE, IN CUI SI AGGIUNGE QUANDO SI SELEZIONA DA CHECKBOX E IN CUI SI TOGLIE QUANDO SI DESELEZIONA
    
	public static void main(String args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					stampaFile frame = new stampaFile(args);
					
					frame.setVisible(true);
					frame.setResizable(false);
					//frame.setUndecorated(true);
				} catch (Exception e) { e.printStackTrace(); }
			}
		});
	}
	
//inizializzazione del programma e dell'interfaccia grafica===============================================
	public stampaFile(String compagniaScelta) throws JSchException, SftpException, IOException {
		/*switch (compagniaScelta) {
		case "Berardi": 
			path="\\\\BER-OFFICE\\FattureB2B\\For\\BER\\Barcode\\"; 	//DA CAMBIARE	
            break; 
		case "Vitman": 
			path="\\\\BER-OFFICE\\FattureB2B\\For\\VIT\\Barcode\\";	
            break; 
		case "Vibolt": 
			path="\\\\BER-OFFICE\\FattureB2B\\For\\VIB\\Barcode\\"; \\BER-OFFICE\FattureB2B\For\VIT\Da stampare
            break; 
		default:
			//System.out.println("Errore in stampaFile.");
		}*/
		
		switch (compagniaScelta) {
		case "Berardi": 
			pathDaStampare="\\\\BER-OFFICE\\FattureB2B\\For\\BER\\Da stampare\\";
			pathBarcode="\\\\BER-OFFICE\\FattureB2B\\For\\BER\\Da stampare\\Barcode\\"; 	//DA CAMBIARE	
			pathArchiviati="\\\\BER-OFFICE\\FattureB2B\\For\\BER\\Archiviati\\"; 	//DA CAMBIARE	
			//pathDaStampare="C:\\Users\\tommolini\\Desktop\\FCP\\Da stampare\\";
			//pathBarcode="C:\\Users\\tommolini\\Desktop\\FCP\\Da stampare\\"; 	//DA CAMBIARE	
			//pathArchiviati="C:\\Users\\tommolini\\Desktop\\FCP\\Archiviati\\"; 	//DA CAMBIARE	
            break; 
		case "Vitman": 
			pathDaStampare="\\\\BER-OFFICE\\FattureB2B\\For\\VIT\\Da stampare\\";
			pathBarcode="\\\\BER-OFFICE\\FattureB2B\\For\\VIT\\Da stampare\\Barcode\\"; 	//DA CAMBIARE	
			pathArchiviati="\\\\BER-OFFICE\\FattureB2B\\For\\VIT\\Archiviati\\"; 	//DA CAMBIARE	
            break; 
		case "Vibolt": 
			pathDaStampare="\\\\BER-OFFICE\\FattureB2B\\For\\VIB\\Da stampare\\";
			pathBarcode="\\\\BER-OFFICE\\FattureB2B\\For\\VIB\\Da stampare\\Barcode\\"; 	//DA CAMBIARE	
			pathArchiviati="\\\\BER-OFFICE\\FattureB2B\\For\\VIB\\Archiviati\\"; 	//DA CAMBIARE	
            break; 
		default:
			//System.out.println("Errore in stampaFile.");
		}
		
		JPanel contentPane;
	    JTable table;
	    JCheckBox checkbox;
	    
		setTitle("Fatturazione ciclo passivo");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) { e.printStackTrace(); } catch (InstantiationException e) {e.printStackTrace();} catch (IllegalAccessException e) {e.printStackTrace();} catch (UnsupportedLookAndFeelException e) {e.printStackTrace();}
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 979, 564);
		setResizable(false);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
//=====================================================================
        
//Titolo a video della lista
		JLabel lblListaDeiFile = new JLabel("PDF fatture:");
		lblListaDeiFile.setBounds(36, 42, 130, 39);
		lblListaDeiFile.setFont(new Font("Tahoma", Font.PLAIN, 20));
		contentPane.add(lblListaDeiFile);
		
		JLabel label = new JLabel("");
		label.setBounds(777, 23, 158, 90);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(label);
        
        JButton btnIndietro = new JButton("Indietro");
        btnIndietro.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
//imposto l'icona del pulsante===========================================
        String nomeIconaChiudi = "indietro.png";
        try {
			BufferedImage iconaChiudi = ImageIO.read(this.getClass().getResource(nomeIconaChiudi));
			java.awt.Image newimg4 = iconaChiudi.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaChiudi1 = new ImageIcon(newimg4);  // transform it back, y);
			btnIndietro.setIcon(iconaChiudi1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
        
//========================================================================
        btnIndietro.setBounds(757, 460, 188, 44);
        btnIndietro.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		//System.out.println(compagniaScelta);
				Fatture1 f1 = new Fatture1(compagniaScelta);
				f1.frmFatturazioneCicloPassivo.setVisible(true);
				dispose();
        	}
        });
        contentPane.add(btnIndietro);

        Object[] columns = {"Nome File", "Da stampare"};		//nome colonne tabella
        
//prendo tutti i file della cartella Barcode per mostrarli nella table, per poi selezionarli, stamparli ed archiviarli
        File folderA = new File(pathBarcode);  
		File[] listOfFilesInA = folderA.listFiles();
    	ArrayList<String> listaFile = new ArrayList<String>();
    	
		for (File fileA : listOfFilesInA) {	
			if (fileA.isFile()) {
				//System.out.print("fileA.getName() " + fileA.getName() + "    ");
				//System.out.print(FilenameUtils.getExtension(fileA.toString())+ "    ");
				
				//rendo visibili da stampare solo i file delle fatture in con estensione .pdf e non gli allegati (che hanno _A nel nome)
			if ((fileA.getName().toString().endsWith(".pdf")) && (!fileA.getName().contains("_A"))) {	
				//System.out.println("IF");
       	 		listaFile.add(fileA.getName());		//aggiungo i nomi alla lista che popolerà la tabella
				} /*else {	//gli altri file (xml e allegati) li archivio subito
					//System.out.println("ELSE");
					String cartellaArchiviati = path + "Stampati/";
					File dir = new File(cartellaArchiviati);	
					dir.mkdir();
					File fileDaArchiviare = new File(path + fileA.getName());
					FileUtils.copyFileToDirectory(fileDaArchiviare, dir);
				}*/
			}
		}
		
		//System.out.println(listaFile);
		Object[] arrayDiFile = new Object[listaFile.size()];
		arrayDiFile = listaFile.toArray(arrayDiFile);
    	
        Object[][] data = null;
        
        DefaultTableModel model = new DefaultTableModel(data, columns);
        for(int i=0;i<arrayDiFile.length;i++){
	          model.addRow(new Object[]{arrayDiFile[i], Boolean.FALSE});
	     }
        final JCheckBox checkBox = new JCheckBox();

        table = new JTable(model) {
        	
            private static final long serialVersionUID = 1L;
            
           /* public boolean isCellEditable(int row, int column){  //toglie l'edit delle righe
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
                        return String.class;			//tipo di dato della prima colonna
                    case 1:
                        return Boolean.class;			//tipo di dato della seconda colonna
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
//SELEZIONA TUTTI============================================
        JCheckBox chckbxSelezionaTutti = new JCheckBox("Seleziona tutti");
        chckbxSelezionaTutti.setFont(new Font("Tahoma", Font.PLAIN, 17));
        chckbxSelezionaTutti.setBounds(690, 52, 148, 25);
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
   
//DESELEZIONA TUTTI==============================================        
        
        checkBox_1.setFont(new Font("Tahoma", Font.PLAIN, 17));
        checkBox_1.setBounds(490, 52, 160, 25);
        checkBox_1.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                for (int row=0; row < table.getRowCount(); row++) {
                	chckbxSelezionaTutti.setSelected(false);
                    table.setValueAt(false, row, 1);
                }
            } 
        } );
        contentPane.add(checkBox_1);
        
        JButton btnStampaSelezionati = new JButton("Stampa selezionati");
        btnStampaSelezionati.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        String nomeIconaStampa = "stampa.png";

        try {
			BufferedImage iconaStampa = ImageIO.read(this.getClass().getResource(nomeIconaStampa));
			java.awt.Image newimg2 = iconaStampa.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaStampa1 = new ImageIcon(newimg2);  // transform it back, y);
			btnStampaSelezionati.setIcon(iconaStampa1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
        btnStampaSelezionati.setBounds(36, 460, 188, 44);
        btnStampaSelezionati.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
					vaiInStampa(table);
					stampaFile s= new stampaFile(compagniaScelta);
					s.setVisible(true);
					dispose();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}
        });
        contentPane.add(btnStampaSelezionati); 
        
        numeroDiFileDaStampare=listaFile.size();
        JLabel label_1 = new JLabel(""+numeroDiFileDaStampare);
        label_1.setFont(new Font("Tahoma", Font.BOLD, 20));
        label_1.setBounds(157, 51, 77, 21);
        contentPane.add(label_1);
     //===============================================================	
        
        JButton buttonVisualizza = new JButton("Visualizza");
        buttonVisualizza.setFont(new Font("Tahoma", Font.PLAIN, 14));
        buttonVisualizza.setEnabled(true);
        buttonVisualizza.setBounds(257, 460, 188, 44);
        buttonVisualizza.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                //for (int row=0; row < table.getRowCount(); row++) {
                	int selectedRow = table.getSelectedRow();
                    try {
                  	  //System.out.println(pathArchiviati +(String) table.getValueAt(selectedRow, 2));
                        Desktop.getDesktop().open(new File(pathBarcode+(String) table.getValueAt(selectedRow, 0)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        }
               // }
            } 
        } );
        
      //imposto l'icona del pulsante===========================================
        String nomeIconaVisualizza = "visualizza.png";
        try {
			BufferedImage iconaVisualizza = ImageIO.read(this.getClass().getResource(nomeIconaVisualizza));
			java.awt.Image newimg6 = iconaVisualizza.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaVisualizza1 = new ImageIcon(newimg6);
			buttonVisualizza.setIcon(iconaVisualizza1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        contentPane.add(buttonVisualizza);
        
        
        //Con il doppio click del mouse apro il file della riga===========================
        table.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                  if (e.getClickCount() == 2) {
                      int selectedRow = table.getSelectedRow();
                      try {
                    	  //System.out.println(pathArchiviati +(String) table.getValueAt(selectedRow, 2));
                          Desktop.getDesktop().open(new File(pathBarcode+(String) table.getValueAt(selectedRow, 0)));
                      } catch (IOException e1) {
                          e1.printStackTrace();
                          }
                      }
                  }
              });
        //================================================================================
}
	
	public void stampaFatture(List<String> listaFileDaStampare) throws Exception, PrinterException, InvalidPasswordException, IOException {
		
		PrinterJob job = PrinterJob.getPrinterJob();
	    PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
	    boolean ok = job.printDialog(attributes);
	    
	    //stampo tutti i file selezionati nella tabella (questi sono i file di archiva pdf con barcode)
		for (int i=0; i<listaFileDaStampare.size(); i++) {
			PDDocument document = PDDocument.load(new File(pathBarcode + listaFileDaStampare.get(i)));
			System.out.println("a");
			 job.setPageable(new PDFPageable(document));
			 System.out.println("b");
			    Attribute[] attributeArray = attributes.toArray();
			    for (Attribute a : attributeArray) {
			        //System.out.println(a.getName() + ": " + a);
			    }
			    System.out.println("c");
			    Attribute copies = attributes.get(Copies.class);
			    Attribute media = attributes.get(Media.class);
			    Attribute mediaPrintableArea = attributes.get(MediaPrintableArea.class);
			    System.out.println("d");
			    Attribute mediaTray = attributes.get(MediaTray.class);
			    Attribute orientationRequested = attributes.get(OrientationRequested.class);
			    Attribute sides = attributes.get(Sides.class);
			    
			    //attributes.add(new PageRanges(1));		//STAMPO SOLO LA PRIMA PAGINA DELLA COPIA ARCHIVA
			    System.out.println("e");
			    attributes.remove(Sides.class);
			    attributes.add(Sides.DUPLEX);
			    attributes.add(MediaSizeName.ISO_A6);
			    System.out.println("f");
			    job.print();		//mando in stampa il documento
			    document.close();	//chiudo il documento
			    //job.cancel();
			    System.out.println("g");
//ARCHIVIO IL FILE PDF CON BARCODE IN ARCHIVIATI/BARCODE
			    File dirAllegatiBarcode = new File(pathArchiviati+"\\Barcode");
			    File fileDaArch= new File(pathBarcode+listaFileDaStampare.get(i));
				//System.out.println(dirAllegatiBarcode);
				//System.out.println("Sto archiviando il file "+listaFileDaStampare.get(i));
			    System.out.println("h");
				FileUtils.copyFileToDirectory(fileDaArch, dirAllegatiBarcode);
				//System.out.println("fileDaArch "+ fileDaArch);
				System.out.println("i");
//==============================================================================  
				
//controllo se il file ha allegato la copia di cortesia e se c'è la stampo tutta
			    File percorsoDaStampare = new File(pathDaStampare);	//DA MODIFICARE
			    File [] fileInDaStampare = percorsoDaStampare.listFiles();		//elenco tutti i file della cartella del percorso path
			    System.out.println("fileInDaStampare " + fileInDaStampare);
			    String nomeFileArchivaSenzaEstensione = listaFileDaStampare.get(i).replaceFirst("[.][^.]+$", "");	//prendo solo il nome file escludendo l'estensione
			    System.out.println("l");
			    for (int k = 0; k < fileInDaStampare.length; k++){	//ciclo tutti i file nella stampati
			    	System.out.println("m");
		    		String nomeFileAllegato = fileInDaStampare[k].getName();
		    		System.out.println(nomeFileAllegato);
		    		//se esiste un allegato del file col barcode lo stampo tutto
			    	if(fileInDaStampare[k].isFile() & nomeFileAllegato.equals(nomeFileArchivaSenzaEstensione+"_A1.pdf")) {
			    		System.out.println("nn");
			    		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
			    		PDDocument documentAllegato = PDDocument.load(new File(percorsoDaStampare +"\\"+ fileInDaStampare[k].getName()));
			    		
			    		System.out.println("oo");
			    		job.setPageable(new PDFPageable(documentAllegato, Orientation.AUTO, false, 300));
						 System.out.println("pp");
						    Attribute[] attributeArray2 = attributes.toArray();
						    for (Attribute a : attributeArray2) {
						        //System.out.println(a.getName() + ": " + a);
						    }
						    System.out.println("qq");
						    Attribute copies2 = attributes.get(Copies.class);
						    Attribute media2 = attributes.get(Media.class);
						    Attribute mediaPrintableArea2 = attributes.get(MediaPrintableArea.class);
						    Attribute mediaTray2 = attributes.get(MediaTray.class);
						    Attribute orientationRequested2 = attributes.get(OrientationRequested.class);
						    Attribute sides2 = attributes.get(Sides.class);
						    
						    System.out.println("rr");
						    attributes.remove(Sides.class);
						    attributes.add(Sides.DUPLEX);
						    //System.out.println("PRIMA DEL PRINT");
						    System.out.println("ss");
						    
						    job.print();
						    
						    System.out.println("tt");
						    documentAllegato.close();	//chiudo il documento
						    //System.out.println("Ho finito di stampare la copia cortesia");
						    System.out.println("uu");
						    //sposto la copia di cortesia in ARCHIVIATI
						    File dirArchiviati = new File(pathArchiviati);
						    File fileCortesiaDaArch= new File(""+fileInDaStampare[k]);
						    System.out.println("vv");
						    FileUtils.copyFileToDirectory(fileCortesiaDaArch, dirArchiviati);
							//System.out.println("fileDaArch "+ fileCortesiaDaArch);
						    System.out.println("zz");
							fileCortesiaDaArch.delete();
							System.out.println("FINE ALLEGATO");
							
			    	} else if(fileInDaStampare[k].isFile() & nomeFileAllegato.startsWith(nomeFileArchivaSenzaEstensione)){					//caso degli altri file(xml ecc...)
			    		//sposto gli altri file in ARCHIVIATI
					    File dirArchiviati = new File(pathArchiviati);
					    File fileAltroDaArch= new File(""+fileInDaStampare[k]);
					    FileUtils.copyFileToDirectory(fileAltroDaArch, dirArchiviati);
						//System.out.println("fileAltroDaArch "+ fileAltroDaArch);
					    System.out.println("p");
						fileAltroDaArch.delete();
						System.out.println("q");
			    	} else if (fileInDaStampare[k].isDirectory() & nomeFileAllegato.startsWith(nomeFileArchivaSenzaEstensione)) {
			    		File dirArchiviati = new File(pathArchiviati+nomeFileArchivaSenzaEstensione);
			    		File CartellaDaArch= new File(""+fileInDaStampare[k]);
			    		System.out.println("r");
			    		FileUtils.moveDirectory(CartellaDaArch, dirArchiviati);
			    		System.out.println("s");
			    	}
			    }
			    System.out.println("t");
			    fileDaArch.delete();		//elimino la copia di archiva con barcode dalla cartella /Da stampare/Barcode
			    System.out.println("u");
		}
	}
//==========================================================================================================
	
//funzione per mandare in stampa i file
	@SuppressWarnings("deprecation")
	public void vaiInStampa(JTable tabella) throws Exception {
		int rows = tabella.getRowCount();
		
		
		//prendo tutti i valori true della seconda colonna della tabella e memorizzo in nomi dei file (prima colonna) in un arraylist che poi manderò in stampa
		for(int i=0; i<rows; i++) {
			
			if((boolean) tabella.getValueAt(i,1)==true) {
				listaFileDaStampare.add((String) tabella.getValueAt(i,0));
			}
		}
		
		ArrayList<String> listaFileStampatiDaArchiviare = new ArrayList<String>();	//lista dei file mandati in stampa
		
		stampaFatture(listaFileDaStampare);
		
		listaFileStampatiDaArch = new Object[listaFileDaStampare.size()];
		listaFileStampatiDaArch = listaFileDaStampare.toArray(listaFileStampatiDaArch);
	}
}
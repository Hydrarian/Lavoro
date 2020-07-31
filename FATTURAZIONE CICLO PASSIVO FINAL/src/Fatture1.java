import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JComboBox;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.awt.Font;
import java.awt.Image;

import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.JProgressBar;


public class Fatture1 extends JFrame {

	
	JFrame frmFatturazioneCicloPassivo;
	public String msg; 								//stringa dove memorizzo l'azienda scelta
	JProgressBar progressBar = new JProgressBar();
	static String compagniaScelta= null;
	JButton btnScarica = createJButton("Scarica", "scarica.png", false, 115, 300, 217, 48);
	JButton btnStampa = createJButton("Stampa", "stampa.png", false, 115, 367, 217, 48);
	JButton btnExit = createJButton("Esci", "chiudi.png", true, 115, 493, 217, 48);
	JButton btnArchivia= createJButton("Archivia", "archivia.png", false, 115, 432, 217, 48);
	String[] elencoCompagnie = { "-Seleziona una company-" , "Berardi", "Vitman", "Vibolt" };
	JComboBox listaCompagnia = new JComboBox(elencoCompagnie);
	protected static final ActionEvent ActionEvent = null;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Fatture1 window = new Fatture1(compagniaScelta);
					window.frmFatturazioneCicloPassivo.setVisible(true);
					window.frmFatturazioneCicloPassivo.setResizable(false);
				} catch (Exception e) { e.printStackTrace(); }
			}
		});
	}
	
	public Fatture1(String compagniaScelta) {
		initialize(compagniaScelta);
	}
	
	//COMBOBOX=================================================================================
	public JComboBox inizializeCombobox(JFrame frame ) {

				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e1) { e1.printStackTrace(); } catch (InstantiationException e1) { e1.printStackTrace(); } catch (IllegalAccessException e1) { e1.printStackTrace(); } catch (UnsupportedLookAndFeelException e1) { e1.printStackTrace(); }
				
				
				

				frame.getContentPane().setLayout(null);
				listaCompagnia.setBounds(53, 93, 349, 37);
				listaCompagnia.setFont(new Font("Tahoma", Font.PLAIN, 18));
				frame.getContentPane().add(listaCompagnia);
				return(listaCompagnia);
				//listaCompagnia.setSelectedIndex(0);
	}
	
	//INIZIALIZZAZIONE FINESTRA PRINCIPALE======================================================
	private void initializeJFrameWindow(JFrame frame ) {
		frame.setTitle("Fatturazione ciclo passivo");
		frame.setBounds(100, 100, 461, 631);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);													//non rende modificabile la dimensione della finestra
	}
	
	//INIZIALIZZAZIONE LABEL SCELTA COMPANY
	public JLabel initializeLabel(String text, int boundX, int boundY, int w, int h, int fontSize) {
		//LABEL SCELTA COMPAGNIA========================================================
				JLabel label = new JLabel(text);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setBounds(boundX, boundY, w, h);
				label.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
				
				return label;
	}
	
	public JButton createJButton(String text, String icon, boolean enabled, int boundX, int boundY, int w, int h) {
		//PULSANTE SCARICA==============================================================
				JButton btn = new JButton(text);
				btn.setFont(new Font("Tahoma", Font.PLAIN, 14));
				btn.setBounds(boundX, boundY, w, h);
				if(enabled==false) btn.setEnabled(false); else btn.setEnabled(true);
					
				//imposto l'icona del pulsante===========================================
		        String nomeIcona = icon;
		        try {
					BufferedImage icona = ImageIO.read(this.getClass().getResource(nomeIcona));
					Image newimg = icona.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
					Icon iconaScarica1 = new ImageIcon(newimg);  // transform it back, y);
					btn.setIcon(iconaScarica1);
				} catch (IOException e2) {
					e2.printStackTrace();
				}
		return btn;
	}
	
	
	private void initialize(String compagniaScelta) {
		frmFatturazioneCicloPassivo = new JFrame();
		
		initializeJFrameWindow(frmFatturazioneCicloPassivo);										//set della finestra
		JComboBox listaCompagnia = inizializeCombobox(frmFatturazioneCicloPassivo);					//imposto il combobox
		JLabel labelSceltaCompany = initializeLabel("",63, 135, 349, 37, 15);		//imposto il label esplicativo di scelta company
		JLabel lblNewLabel = initializeLabel("",53, 143, 349, 148, 15);
		JLabel lblFatturazioneACiclo = initializeLabel("Fatturazione ciclo passivo",53, 33, 349, 41, 20);
		frmFatturazioneCicloPassivo.getContentPane().add(labelSceltaCompany);
		frmFatturazioneCicloPassivo.getContentPane().add(lblNewLabel);
		frmFatturazioneCicloPassivo.getContentPane().add(lblFatturazioneACiclo);
		
		frmFatturazioneCicloPassivo.getContentPane().add(btnExit);
		frmFatturazioneCicloPassivo.getContentPane().add(btnStampa);
		frmFatturazioneCicloPassivo.getContentPane().add(btnArchivia);
		frmFatturazioneCicloPassivo.getContentPane().add(btnScarica);
		
		progressBar.setStringPainted(false);
		progressBar.setString("Connessione ed ottenimento lista file in corso...");
		progressBar.setBounds(53, 554, 349, 29);
		frmFatturazioneCicloPassivo.getContentPane().add(progressBar);
		
//======================================================================
		btnScarica.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {actionEvent(1);
			}
		});

				
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		
		btnStampa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {actionEvent(2);
			}
		});
		
		
		
		btnArchivia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {actionEvent(3);
			}
		});	
//==============================================================================
				
		listaCompagnia.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == listaCompagnia) {
				msg = (String)listaCompagnia.getSelectedItem();
				
					switch (msg) {
					
						case "Berardi": 
							Icon logoBerardi = null;
							impostaLabelEAttivaPulsanti("BERARDI", logoBerardi, labelSceltaCompany, lblNewLabel, btnScarica, btnStampa, btnArchivia, 400, 310);
						break;
						
						case "Vitman": 
							Icon logoVitman = null;
							impostaLabelEAttivaPulsanti("VITMAN", logoVitman, labelSceltaCompany, lblNewLabel, btnScarica, btnStampa, btnArchivia, 320, 120);
						break;
						
						case "Vibolt": 
							Icon logoVibolt = null;
							impostaLabelEAttivaPulsanti("VIBOLT", logoVibolt, labelSceltaCompany, lblNewLabel, btnScarica, btnStampa, btnArchivia, 320, 120);
						break;
						
						case "-Seleziona una company-": 
							labelSceltaCompany.setText("Non hai selezionato nessuna company!");
							btnScarica.setEnabled(false);
							btnStampa.setEnabled(false);
							btnArchivia.setEnabled(false);
						break;
				}
			}
			}
		});
		
		if (compagniaScelta!=null) {
			listaCompagnia.setSelectedItem(compagniaScelta);
			listaCompagnia.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null) {
			    });
			}
		}
	
	public void actionEvent(int finestra) {
		btnScarica.setEnabled(false);
		progressBar.setStringPainted(true);
		listaCompagnia.setEnabled(false);
		btnArchivia.setEnabled(false);
		btnStampa.setEnabled(false);
		

		SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
			
			@Override
			protected Void doInBackground() throws Exception {
				 progressBar.setVisible(true);
				 progressBar.setIndeterminate(true);
				 String msg = (String)listaCompagnia.getSelectedItem();
				 switch(finestra) {
				 	case 1:
				 		scaricaFile SF;
						try {
							SF = new scaricaFile(msg);
							SF.setVisible(true);
						} catch (JSchException e) { e.printStackTrace(); } catch (SftpException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }		
				 		break;
				 	case 2:
				 		try {
							stampaFile Sf= new stampaFile(msg);
							Sf.setVisible(true);
							frmFatturazioneCicloPassivo.dispose();
							Sf.setResizable(false);
				 		} catch (JSchException e) { e.printStackTrace(); } catch (SftpException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
				 		break;
				 	case 3:
				 		archiviaFile AF;
						try {
							AF = new archiviaFile(msg);
							AF.setVisible(true);
							frmFatturazioneCicloPassivo.dispose();
						} catch (JSchException e) { e.printStackTrace(); } catch (SftpException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }

						break;
				 }
					
					frmFatturazioneCicloPassivo.dispose();
					return null;
			}

			@Override
			protected void done() {
				
			}
		};
		worker.execute();	
}
//=======================================

//NEL MOMENTO IN CUI SELEZIONO UNA COMPANY DAL COMBOBOX: imposta il label "hai scelto la company... e attiva i pulsanti==============================
    public void impostaLabelEAttivaPulsanti(String companySelezionata, Icon iconaDaImpostare, JLabel labelEsplic, JLabel nuovoLabel, JButton pulsanteScarica, JButton pulsanteStampa, JButton pulsanteArchivia, int dimLogoX, int dimLogoY) {
    	labelEsplic.setText("Hai selezionato come company:" + companySelezionata);
		//Icon iconaDaImpostare;
		try {
			iconaDaImpostare = impostaLogo(msg,dimLogoX,dimLogoY);
			nuovoLabel.setIcon(iconaDaImpostare);
		} catch (IOException e1) { e1.printStackTrace(); }
		pulsanteScarica.setEnabled(true);
		pulsanteStampa.setEnabled(true);
		pulsanteArchivia.setEnabled(true);
    }
//=========================================================================
    
    
//mostra il logo=========================
	public Icon impostaLogo(String compagnia, int x, int y) throws IOException {
		String nomeLogo = compagnia + ".png";
		BufferedImage logo = ImageIO.read(this.getClass().getResource(nomeLogo));
		Image newimg = logo.getScaledInstance(x, y,  java.awt.Image.SCALE_SMOOTH); 	// scale it the smooth way  
		Icon logo1 = new ImageIcon(newimg);  										// transform it back
		return logo1;
	}
}
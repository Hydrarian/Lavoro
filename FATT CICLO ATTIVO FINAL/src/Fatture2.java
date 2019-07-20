import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
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

public class Fatture2 {

	JFrame frmFatturazioneCicloAttivo;
	static String msg = null;; 							//stringa dove memorizzo l'azienda scelta
	JLabel labelEsplicativo = new JLabel();
	boolean caricamento=false;
	JProgressBar progressBar = new JProgressBar();
	
	

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Fatture2 window = new Fatture2(msg);
					window.frmFatturazioneCicloAttivo.setVisible(true);
				} catch (Exception e) { e.printStackTrace(); }
			}
		});
	}

	public Fatture2(String msg) {
		initialize(msg);
	}
	
	private void initialize(String msg) {
		frmFatturazioneCicloAttivo = new JFrame();
		frmFatturazioneCicloAttivo.setTitle("Fatturazione ciclo attivo");
		frmFatturazioneCicloAttivo.setBounds(100, 100, 378, 460);
		frmFatturazioneCicloAttivo.setLocationRelativeTo(null);
		frmFatturazioneCicloAttivo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFatturazioneCicloAttivo.setResizable(false);
		
//COMBOBOX======================================================================
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) { e1.printStackTrace(); } catch (InstantiationException e1) { e1.printStackTrace(); } catch (IllegalAccessException e1) { e1.printStackTrace(); } catch (UnsupportedLookAndFeelException e1) { e1.printStackTrace(); }
		String[] elencoCompagnie = { "-Seleziona una compagnia-" , "Berardi", "Vitman", "Vibolt" };
		frmFatturazioneCicloAttivo.getContentPane().setLayout(null);
		JComboBox listaCompagnia = new JComboBox(elencoCompagnie);
		listaCompagnia.setBounds(12, 66, 349, 37);
		listaCompagnia.setFont(new Font("Tahoma", Font.PLAIN, 18));
		frmFatturazioneCicloAttivo.getContentPane().add(listaCompagnia);
		listaCompagnia.setSelectedIndex(0);
//==============================================================================
		
//LABEL SCELTA COMPAGNIA========================================================
		
		labelEsplicativo.setHorizontalAlignment(SwingConstants.CENTER);
		labelEsplicativo.setBounds(12, 116, 349, 37);
		labelEsplicativo.setFont(new Font("Tahoma", Font.PLAIN, 15));
		frmFatturazioneCicloAttivo.getContentPane().add(labelEsplicativo);
//==============================================================================
		
//PULSANTE PROCEDI==============================================================
		JButton btnprocedi = new JButton("Procedi");
		btnprocedi.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnprocedi.setBounds(231, 313, 130, 50);
		
//imposto l'icona del pulsante=================================================
        String nomeIconaProcedi = "procedi.png";
        try {
			BufferedImage iconaProcedi = ImageIO.read(this.getClass().getResource(nomeIconaProcedi));
			Image newimg = iconaProcedi.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaProcedi1 = new ImageIcon(newimg);  // transform it back, y);
			btnprocedi.setIcon(iconaProcedi1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//=============================================================================
        
		btnprocedi.setEnabled(false);
		btnprocedi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnprocedi.setEnabled(false);
				progressBar.setStringPainted(true);
				listaCompagnia.setEnabled(false);

				SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
					
					@Override
					protected Void doInBackground() throws Exception {
						 progressBar.setVisible(true);
						 progressBar.setIndeterminate(true);
						 try {
							 	String msg = (String)listaCompagnia.getSelectedItem();
								scaricaFile SF;
								SF = new scaricaFile(msg);
								caricamento=false;
								SF.setVisible(true);
								frmFatturazioneCicloAttivo.dispose();
							} catch (JSchException e) { e.printStackTrace(); } catch (SftpException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }			
						return null;
					}

					@Override
					protected void done() {
						
					}
				};
				worker.execute();
			}
		});
		frmFatturazioneCicloAttivo.getContentPane().add(btnprocedi);
//==============================================================================
				
		JLabel label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(12, 166, 349, 134);
		frmFatturazioneCicloAttivo.getContentPane().add(label);
//==============================================================================
				
		listaCompagnia.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == listaCompagnia) {

				String msg = (String)listaCompagnia.getSelectedItem();
					switch (msg) {
					
						case "Berardi": labelEsplicativo.setText("Hai selezionato la company: BERARDI");
							Icon logoBerardi;
							try {
								logoBerardi = impostaLogo(msg,400,310);
								label.setIcon(logoBerardi);
							} catch (IOException e1) { e1.printStackTrace(); }
							btnprocedi.setEnabled(true);
						break;
						
						case "Vitman": labelEsplicativo.setText("Hai selezionato la company: VITMAN");
							Icon logoVitman;
							try {
								logoVitman = impostaLogo(msg,320,120);
								label.setIcon(logoVitman);
							} catch (IOException e1) { e1.printStackTrace(); }
							btnprocedi.setEnabled(true);
						break;
						
						case "Vibolt": labelEsplicativo.setText("Hai selezionato la company: VIBOLT");
							Icon logoVibolt;
							try {
								logoVibolt = impostaLogo(msg,320,120);
								label.setIcon(logoVibolt);
							} catch (IOException e1) {e1.printStackTrace(); }
							btnprocedi.setEnabled(true);
						break;
						
						case "-Seleziona una compagnia-": labelEsplicativo.setText("Non hai selezionato nessuna compagnia!");
							btnprocedi.setEnabled(false);
						break;
				}
			}
			}
		});
//=============================================================================
		
//PULSANTE EXIT=================================================================
		JButton button = new JButton("Esci");
		button.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
//imposto l'icona del pulsante===========================================
        String nomeIconaEsci = "chiudi.png";
        try {
			BufferedImage iconaEsci = ImageIO.read(this.getClass().getResource(nomeIconaEsci));
			Image newimg = iconaEsci.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaEsci1 = new ImageIcon(newimg);  // transform it back, y);
			button.setIcon(iconaEsci1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//========================================================================
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		button.setBounds(12, 313, 130, 50);
		frmFatturazioneCicloAttivo.getContentPane().add(button);
		
		JLabel lblFatturazioneCicloAttivo = new JLabel("Fatturazione ciclo attivo");
		lblFatturazioneCicloAttivo.setHorizontalAlignment(SwingConstants.CENTER);
		lblFatturazioneCicloAttivo.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblFatturazioneCicloAttivo.setBounds(12, 13, 349, 28);
		frmFatturazioneCicloAttivo.getContentPane().add(lblFatturazioneCicloAttivo);
		progressBar.setStringPainted(false);
		progressBar.setString("Connessione ed elaborazione dei file in corso...");
	
		progressBar.setBounds(12, 385, 349, 29);
		frmFatturazioneCicloAttivo.getContentPane().add(progressBar);
//==============================================================================
		
		if (msg!=null) {
			listaCompagnia.setSelectedItem(msg);
			listaCompagnia.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null) {
			    });
			}
	}
	
	
	
//mostra il logo dell'azienda========================================
		public Icon impostaLogo(String compagnia, int x, int y) throws IOException {
			String nomeLogo = compagnia + ".png";
			//System.out.println(nomeLogo);
			BufferedImage logo = ImageIO.read(this.getClass().getResource(nomeLogo));
			Image newimg = logo.getScaledInstance(x, y,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon logo1 = new ImageIcon(newimg);  // transform it back
			return logo1;
		}	
}

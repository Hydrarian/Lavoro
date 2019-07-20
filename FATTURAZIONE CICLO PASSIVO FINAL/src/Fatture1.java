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
	
	private void initialize(String compagniaScelta) {
		frmFatturazioneCicloPassivo = new JFrame();
		frmFatturazioneCicloPassivo.setTitle("Fatturazione ciclo passivo");
		frmFatturazioneCicloPassivo.setBounds(100, 100, 461, 631);
		frmFatturazioneCicloPassivo.setLocationRelativeTo(null);
		frmFatturazioneCicloPassivo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFatturazioneCicloPassivo.setResizable(false);				//non rende modificabile la dimensione della finestra
	
//COMBOBOX=================================================================================
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) { e1.printStackTrace(); } catch (InstantiationException e1) { e1.printStackTrace(); } catch (IllegalAccessException e1) { e1.printStackTrace(); } catch (UnsupportedLookAndFeelException e1) { e1.printStackTrace(); }
		
		String[] elencoCompagnie = { "-Seleziona una compagnia-" , "Berardi", "Vitman", "Vibolt" };
		JComboBox listaCompagnia = new JComboBox(elencoCompagnie);

		frmFatturazioneCicloPassivo.getContentPane().setLayout(null);
		listaCompagnia.setBounds(53, 93, 349, 37);
		listaCompagnia.setFont(new Font("Tahoma", Font.PLAIN, 18));
		frmFatturazioneCicloPassivo.getContentPane().add(listaCompagnia);
		//listaCompagnia.setSelectedIndex(0);
//==============================================================================
		
//LABEL SCELTA COMPAGNIA========================================================
		JLabel labelEsplicativo = new JLabel();
		labelEsplicativo.setHorizontalAlignment(SwingConstants.CENTER);
		labelEsplicativo.setBounds(63, 135, 349, 37);
		labelEsplicativo.setFont(new Font("Tahoma", Font.PLAIN, 15));
		frmFatturazioneCicloPassivo.getContentPane().add(labelEsplicativo);
//==============================================================================
		
//PULSANTE PROCEDI==============================================================
		JButton btnScarica = new JButton("Scarica");
		btnScarica.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnScarica.setBounds(115, 302, 217, 48);
		btnScarica.setEnabled(false);
		//imposto l'icona del pulsante===========================================
        String nomeIconaScarica = "scarica.png";
        try {
			BufferedImage iconaScarica = ImageIO.read(this.getClass().getResource(nomeIconaScarica));
			Image newimg = iconaScarica.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaScarica1 = new ImageIcon(newimg);  // transform it back, y);
			btnScarica.setIcon(iconaScarica1);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//======================================================================
		btnScarica.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					btnScarica.setEnabled(false);
					progressBar.setStringPainted(true);
					listaCompagnia.setEnabled(false);

					SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
						
						@Override
						protected Void doInBackground() throws Exception {
							 progressBar.setVisible(true);
							 progressBar.setIndeterminate(true);
							 String msg = (String)listaCompagnia.getSelectedItem();
								scaricaFile SF;
								try {
									SF = new scaricaFile(msg);
									SF.setVisible(true);
								} catch (JSchException e) { e.printStackTrace(); } catch (SftpException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }		
								frmFatturazioneCicloPassivo.dispose();
								return null;
						}

						@Override
						protected void done() {
							
						}
					};
					worker.execute();	
			}
		});
		frmFatturazioneCicloPassivo.getContentPane().add(btnScarica);
//==============================================================================
				
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
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
//======================================================================
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		button.setBounds(115, 493, 217, 48);
		frmFatturazioneCicloPassivo.getContentPane().add(button);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(53, 143, 349, 148);
		frmFatturazioneCicloPassivo.getContentPane().add(lblNewLabel);
		
		JLabel lblFatturazioneACiclo = new JLabel("Fatturazione ciclo passivo");
		lblFatturazioneACiclo.setHorizontalAlignment(SwingConstants.CENTER);
		lblFatturazioneACiclo.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblFatturazioneACiclo.setBounds(53, 33, 349, 41);
		frmFatturazioneCicloPassivo.getContentPane().add(lblFatturazioneACiclo);
		
		
		progressBar.setStringPainted(false);
		progressBar.setString("Connessione ed ottenimento lista file in corso...");
		progressBar.setBounds(53, 554, 349, 29);
		frmFatturazioneCicloPassivo.getContentPane().add(progressBar);
		
		JButton btnStampa = new JButton("Stampa");
		btnStampa.setFont(new Font("Tahoma", Font.PLAIN, 14));
		String nomeIconaStampa = "stampa.png";
        try {
			BufferedImage iconaStampa = ImageIO.read(this.getClass().getResource(nomeIconaStampa));
			Image newimgx = iconaStampa.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaStampa1 = new ImageIcon(newimgx);  // transform it back, y);
			btnStampa.setIcon(iconaStampa1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        JButton btnArchiviaFatture = new JButton("Archiviati");
//======================================================================
		btnStampa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnScarica.setEnabled(false);
				progressBar.setStringPainted(true);
				listaCompagnia.setEnabled(false);
				btnArchiviaFatture.setEnabled(false);
				
				SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
					
					@Override
					protected Void doInBackground() throws Exception {
						 progressBar.setVisible(true);
						 progressBar.setIndeterminate(true);
						 progressBar.setString("Creazione elenco fatture in corso...");
						 String msg = (String)listaCompagnia.getSelectedItem();
							try {
								stampaFile Sf= new stampaFile(msg);
								Sf.setVisible(true);
								frmFatturazioneCicloPassivo.dispose();
								Sf.setResizable(false);
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
							return null;
					}

					@Override
					protected void done() {
						
					}
				};
				worker.execute();	
		}
	});
		
		btnStampa.setBounds(115, 367, 217, 48);
		btnStampa.setEnabled(false);
		frmFatturazioneCicloPassivo.getContentPane().add(btnStampa);
		
		btnArchiviaFatture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnScarica.setEnabled(false);
				progressBar.setStringPainted(true);
				listaCompagnia.setEnabled(false);
				btnStampa.setEnabled(false);
				
				SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>(){
					
					@Override
					protected Void doInBackground() throws Exception {
						 progressBar.setVisible(true);
						 progressBar.setIndeterminate(true);
						 progressBar.setString("Creazione elenco fatture in corso...");
						 String msg = (String)listaCompagnia.getSelectedItem();
						 archiviaFile AF;
							try {
								AF = new archiviaFile(msg);
								AF.setVisible(true);
								frmFatturazioneCicloPassivo.dispose();
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
							
							return null;
					}

					@Override
					protected void done() {
						
					}
				};
				worker.execute();	
		}
				
				
		});
		btnArchiviaFatture.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnArchiviaFatture.setBounds(115, 432, 217, 48);
		String nomeIconaArchiviati = "archivia.png";
        try {
			BufferedImage iconaArchivia = ImageIO.read(this.getClass().getResource(nomeIconaArchiviati));
			Image newimgy = iconaArchivia.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			Icon iconaArchivia1 = new ImageIcon(newimgy);  // transform it back, y);
			btnArchiviaFatture.setIcon(iconaArchivia1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
//======================================================================
		btnArchiviaFatture.setEnabled(false);
		frmFatturazioneCicloPassivo.getContentPane().add(btnArchiviaFatture);
//==============================================================================
				
		listaCompagnia.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == listaCompagnia) {
				msg = (String)listaCompagnia.getSelectedItem();
					switch (msg) {
					
						case "Berardi": labelEsplicativo.setText("Hai selezionato come company: BERARDI");
							Icon logoBerardi;
							try {
								logoBerardi = impostaLogo(msg,400,310);
								lblNewLabel.setIcon(logoBerardi);
							} catch (IOException e1) { e1.printStackTrace(); }
							btnScarica.setEnabled(true);
							btnStampa.setEnabled(true);
							btnArchiviaFatture.setEnabled(true);
						break;
						
						case "Vitman": labelEsplicativo.setText("Hai selezionato come company: VITMAN");
							Icon logoVitman;
							try {
								logoVitman = impostaLogo(msg,320,120);
								lblNewLabel.setIcon(logoVitman);
							} catch (IOException e1) { e1.printStackTrace(); }
							btnScarica.setEnabled(true);
							btnStampa.setEnabled(true);
							btnArchiviaFatture.setEnabled(true);
						break;
						
						case "Vibolt": labelEsplicativo.setText("Hai selezionato come company: VIBOLT");
							Icon logoVibolt;
							try {
								logoVibolt = impostaLogo(msg,320,120);
								lblNewLabel.setIcon(logoVibolt);
							} catch (IOException e1) { e1.printStackTrace(); }
							btnScarica.setEnabled(true);
							btnStampa.setEnabled(true);
							btnArchiviaFatture.setEnabled(true);
						break;
						
						case "-Seleziona una compagnia-": labelEsplicativo.setText("Non hai selezionato nessuna compagnia!");
							btnScarica.setEnabled(false);
							btnStampa.setEnabled(false);
							btnArchiviaFatture.setEnabled(false);
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
//=======================================

//mostra il logo=========================
	public Icon impostaLogo(String compagnia, int x, int y) throws IOException {
		String nomeLogo = compagnia + ".png";
		BufferedImage logo = ImageIO.read(this.getClass().getResource(nomeLogo));
		Image newimg = logo.getScaledInstance(x, y,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		Icon logo1 = new ImageIcon(newimg);  // transform it back
		return logo1;
	}
}
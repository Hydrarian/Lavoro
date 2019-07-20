import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EseguiQuery extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldCodiceVecchio;
	static 	JTextArea textArea = new JTextArea();
	private JTextField textFieldCodiceNew;
	
	static int accumulatore=0;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					EseguiQuery frame = new EseguiQuery();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public EseguiQuery() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// aggiunta della barra per scorrere===================================
	
		JTextFieldRegularPopupMenu cm = new JTextFieldRegularPopupMenu();
		
		setTitle("Variazione codice LIFO");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 512, 453);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textFieldCodiceVecchio = new JTextField();
		textFieldCodiceVecchio.setBounds(172, 12, 157, 28);
		contentPane.add(textFieldCodiceVecchio);
		textFieldCodiceVecchio.setColumns(10);
		cm.addTo(textFieldCodiceVecchio);
		
		JLabel lblVecchioCodice = new JLabel("Vecchio codice:");
		lblVecchioCodice.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblVecchioCodice.setBounds(39, 11, 123, 28);
		contentPane.add(lblVecchioCodice);
		
		textFieldCodiceNew = new JTextField();
		textFieldCodiceNew.setColumns(10);
		textFieldCodiceNew.setBounds(172, 54, 157, 28);
		contentPane.add(textFieldCodiceNew);
		cm.addTo(textFieldCodiceNew);
		
		textArea.setBounds(12, 11, 476, 214);
		contentPane.add(textArea);
		textArea.setCaretPosition(textArea.getText().length());		//Autoscroll all'ultima riga
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setBounds(10, 93, 476, 289);                     
		getContentPane().add(scroll);
		
		
		
		JButton btnSostituisci = new JButton("Sostituisci");
		btnSostituisci.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnSostituisci.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if (textFieldCodiceVecchio.getText().length()!=0 && textFieldCodiceNew.getText().length()!=0) {
				String codiceOld = textFieldCodiceVecchio.getText();
				String codiceNew = textFieldCodiceNew.getText();
				
				if (codiceOld.length()==0)  System.out.println("Codice di lunghezza zero");
					else {
							
							// Create a variable for the connection string.
							String connectionUrl = "jdbc:sqlserver://172.16.1.15:1433;" + "databaseName=GAMMAtest;user=sa;password=ber-sql-sa";

							// Declare the JDBC objects.
							Connection con = null;
							//Statement stmt = null;
							//ResultSet rs = null;
							// Establish the connection.
							try {
								Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					    	try {
								con = DriverManager.getConnection(connectionUrl);
								executeTransaction(con, codiceOld, codiceNew);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
					}
			} else stampaTitolo("Uno dei campi non può essere vuoto. Query non eseguita.\n");
			}
			
		});
		
		btnSostituisci.setBounds(372, 12, 89, 28);
		contentPane.add(btnSostituisci);

		JLabel label = new JLabel("Nuovo codice:");
		label.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label.setBounds(39, 53, 123, 28);
		contentPane.add(label);
		
		JButton btnChiudi = new JButton("Chiudi");
		btnChiudi.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnChiudi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		btnChiudi.setBounds(372, 54, 89, 28);
		contentPane.add(btnChiudi);
		
		JLabel lblConnessoA = new JLabel("Connesso a 172.16.1.15:1433", SwingConstants.CENTER);
		lblConnessoA.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblConnessoA.setBounds(10, 386, 476, 21);
		contentPane.add(lblConnessoA);
		
	}
	
	//stampa risultato righe interessate dalle query
	public static void stampaRigheInteressate(int numeroRighe, String company) {
		textArea.append(" " + company +" ( "+ numeroRighe + " righe interessate) \n");
		textArea.setCaretPosition(textArea.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
    	textArea.update(textArea.getGraphics());
    	
	}
	
	//stampa titolo query
	public static void stampaTitolo(String stringa) {
		textArea.append(" "+stringa);
		textArea.setCaretPosition(textArea.getText().length());		//Scrollo automaticamente all'ultima riga della textarea
    	textArea.update(textArea.getGraphics());
    	
	}
	
	public static void executeTransaction(Connection con, String codiceOld, String codiceNew) {
		int rowsAffectedBERARDI;
		int rowsAffectedVIBOLT;
		int rowsAffectedVITMAN;
		
	    try {
	        //Switch to manual transaction mode by setting
	        //autocommit to false. Note that this starts the first manual transaction.
	    	
	    	con.setAutoCommit(false);
	    	textArea.setText("");
	    	//System.out.println("--- LIFO ---");
    		stampaTitolo("--- LIFO --- \n");
    		PreparedStatement stmt = con.prepareStatement("UPDATE GAMMAtest.dbo.MG89_LIFOART     SET MG89_CODART_MG66 = ? WHERE MG89_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceNew); stmt.setString(2, codiceOld);
    		rowsAffectedBERARDI = stmt.executeUpdate();		//righe modificate dal preparedStatement stmt
    		accumulatore=accumulatore+rowsAffectedBERARDI;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedBERARDI, "BERARDI");
    		
    		stmt = con.prepareStatement("UPDATE VIBOLT.dbo.MG89_LIFOART     SET MG89_CODART_MG66 = ? WHERE MG89_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceNew); stmt.setString(2, codiceOld);
    		rowsAffectedVIBOLT = stmt.executeUpdate();
    		accumulatore=accumulatore+rowsAffectedVIBOLT;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedVIBOLT, "VIBOLT");
    		
    		stmt = con.prepareStatement("UPDATE VITMAN.dbo.MG89_LIFOART     SET MG89_CODART_MG66 = ? WHERE MG89_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceNew); stmt.setString(2, codiceOld);
    		rowsAffectedVITMAN = stmt.executeUpdate();
    		accumulatore=accumulatore+rowsAffectedVITMAN;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedVITMAN, "VITMAN");
    		
    		stampaTitolo("--- MG1Q_ARTNOMENCLATURE ---\n");
    		//System.out.println("--- MG1Q_ARTNOMENCLATURE ---");
    		stmt = con.prepareStatement("DELETE from GAMMAtest.dbo.MG1Q_ARTNOMENCLATURE     WHERE MG1Q_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		int rowsAffectedDELETE1BERARDI = stmt.executeUpdate();
    		//accumulatore=accumulatore+rowsAffectedDELETE1BERARDI;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedDELETE1BERARDI, "BERARDI");
    		
    		stmt = con.prepareStatement("DELETE from VIBOLT.dbo.MG1Q_ARTNOMENCLATURE     WHERE MG1Q_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		int rowsAffectedDELETE1VIBOLT = stmt.executeUpdate();
    		//accumulatore=accumulatore+rowsAffectedDELETE1VIBOLT;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedDELETE1VIBOLT, "VIBOLT");
    		
    		stmt = con.prepareStatement("DELETE from VITMAN.dbo.MG1Q_ARTNOMENCLATURE     WHERE MG1Q_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		int rowsAffectedDELETE1VITMAN = stmt.executeUpdate();
    		//accumulatore=accumulatore+rowsAffectedDELETE1VITMAN;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedDELETE1VITMAN, "VITMAN");

    		
    		stampaTitolo("--- MG43_ARTPERS ---\n");
    		//System.out.println("--- MG43_ARTPERS ---");
    		stmt = con.prepareStatement("DELETE from GAMMAtest.dbo.MG43_ARTPERS      	   where MG43_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		int rowsAffectedDELETE2BERARDI = stmt.executeUpdate();
    		//accumulatore=accumulatore+rowsAffectedDELETE2BERARDI;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedDELETE2BERARDI,"BERARDI");
    		
    		stmt = con.prepareStatement("DELETE from VIBOLT.dbo.MG43_ARTPERS      	   where MG43_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		int rowsAffectedDELETE2VIBOLT = stmt.executeUpdate();
    		//accumulatore=accumulatore+rowsAffectedDELETE2VIBOLT;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedDELETE2VIBOLT,"VIBOLT");
    		
    		stmt = con.prepareStatement("DELETE from VITMAN.dbo.MG43_ARTPERS      	   where MG43_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		int rowsAffectedDELETE2VITMAN = stmt.executeUpdate();
    		//accumulatore=accumulatore+rowsAffectedDELETE2VITMAN;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedDELETE2VITMAN,"VITMAN");
    		
    		
    		stampaTitolo("--- PD27_ANAFORMULE ---\n");
    		stmt = con.prepareStatement("DELETE FROM GAMMAtest.dbo.PD27_ANAFORMULE \r\n");
    		int rowsAffectedDELETE3BERARDI = stmt.executeUpdate();
    		//accumulatore=accumulatore+rowsAffectedDELETE3BERARDI;
    		//System.out.println("rowsAffected "+ rowsAffected);
    		stampaRigheInteressate(rowsAffectedDELETE3BERARDI,"BERARDI");
    		con.commit();
    		
    		SQLWarning warning = stmt.getWarnings();
    		//System.out.println(warning);
    		while (warning != null)
    		{
    			//System.out.println(warning.getMessage());
    		   warning = warning.getNextWarning();
    		   stampaTitolo(warning.getMessage() + "\n");
    		   // System.out.println(warning);
    		}
    		
    		stmt.close();
    		stampaTitolo("Transazione avvenuta con successo.\n");
    		if (accumulatore!=0){sendEmail(codiceOld, codiceNew, rowsAffectedBERARDI, rowsAffectedVIBOLT, rowsAffectedVITMAN);}
    		else stampaTitolo("-Operazione conclusa-\n");
	    }
	    catch (SQLException ex) {
	        ex.printStackTrace();
	        try {
	        	stampaTitolo("Transazione fallita.\n");
	            con.rollback();
	        }
	        catch (SQLException se) {
	            se.printStackTrace();
	        }
	    }
	}
	
	public static void sendEmail(String codiceOld, String codiceNew, int rowsAffectedBERARDI, int rowsAffectedVIBOLT, int rowsAffectedVITMAN) {
			
            String to="f.farne@gberardi.com";
            stampaTitolo("Sto mandando un'email  di notifica a " + to + "...\n");
            String from="berardi.bot@gmail.com";

            Properties props = new Properties();
//            props.put("mail.smtp.socketFactory.port", "587");
//            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//            props.put("mail.smtp.socketFactory.fallback", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true"); //TLS

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication("berardi.bot@gmail.com","te312502");
                        }
                    });


            String msgBody = textArea.getText();

            Message msg = new MimeMessage(session);
            try {
            	String oggettoEmail="";
            	if (rowsAffectedBERARDI>0) { oggettoEmail = oggettoEmail+" BERARDI: " + rowsAffectedBERARDI + " ";}
            	if (rowsAffectedVIBOLT>0) { oggettoEmail = oggettoEmail+" VIBOLT: " + rowsAffectedVIBOLT + " ";}
            	if (rowsAffectedVITMAN>0) { oggettoEmail = oggettoEmail+" VITMAN: " + rowsAffectedVITMAN + " ";}
				msg.setFrom(new InternetAddress(from, "VariazioneCodiceLIFO"));
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, "Fiorenzo Farnè"));
	            msg.setSubject("Switch LIFO: " + oggettoEmail + "[ " + codiceOld + " ==> " + codiceNew +" ]");
	            msg.setText(msgBody);
	            
	            Transport.send(msg);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            stampaTitolo("Email mandata con successo!\n");
            stampaTitolo("-Operazione conclusa-\n");
            //System.out.println("Email mandata con successo...");
	}
}

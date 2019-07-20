import java.io.*;
import java.sql.*;
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


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;



public class Esporta {

	public static void main(String[] args) {
		  String[] driverDB =       new String[] {"com.microsoft.sqlserver.jdbc.SQLServerDriver"};
		  String[] stringConnDB =   new String[] {"jdbc:sqlserver://172.16.1.15:1433"};
		  String[] userDB =         new String[] {"sa"};
		  String[] passDB =         new String[] {"ber-sql-sa"};
		  String[] charSep =        new String[] {";"};
		  Boolean colomn=   		new Boolean (true);
		  String[] queryDB =        new String[] {"select * FROM GAMMAtest.dbo.BER_CittaProvincieRegioni"};


		try{
		    System.out.println("---------------File exist?------------" + "C:\\Users\\tommolini\\Desktop\\test.csv");
		    File fileTemp = new File("C:\\Users\\tommolini\\Desktop\\test.csv");
		    if (fileTemp.exists()){ 
		        fileTemp.delete();
		        System.out.println("---------------DELETE FILE------------" + "C:\\Users\\tommolini\\Desktop\\test.csv" );
		                } 
		   System.out.println("QUERY: ---->"+ queryDB[0].toString()); 
		   exportQueryToCsv exp = new exportQueryToCsv();
		   exp.exportQueryToCsv("C:\\Users\\tommolini\\Desktop\\test.csv",driverDB,stringConnDB,userDB,passDB,queryDB, colomn,charSep);
		   if (fileTemp.exists()){ 
		     System.out.println("---File created---" + "C:\\Users\\tommolini\\Desktop\\test.csv");
		  }

		}
		catch(Exception e){
		         e.printStackTrace();
		      }
	}
}

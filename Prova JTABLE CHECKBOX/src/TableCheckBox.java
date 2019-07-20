import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;

class TableCheckBox extends JFrame {

    private JPanel contentPane;
    private JTable table;
    private JCheckBox checkbox;
    private static final long serialVersionUID = 1L;

    /**
     * Create the frame.
     */
    public TableCheckBox() {
        Object[] columns = {"Nome File", "Da stampare"};
        
        
        File folderA = new File("C:\\Users\\tommolini\\Desktop\\FatturazioneCP\\Barcode");
		File[] listOfFilesInA = folderA.listFiles();
    	ArrayList<String> listaFile = new ArrayList<String>();
		for (File fileA : listOfFilesInA) {	
			if (fileA.isFile()) {	
				//int indiceUnderscoreA = fileA.getName().indexOf(")");	//prendo la posizione dell'ultimo carattere che voglio considerare di un file
       	 		//String nomeFileASenzaSuffisso = fileA.getName().substring(0,indiceUnderscoreA+1);	//prendo il nome del file senza suffisso
       	 		listaFile.add(fileA.getName());
				}
		}
		System.out.println(listaFile);
		Object[] arrayDiFile = new Object[listaFile.size()];
		arrayDiFile = listaFile.toArray(arrayDiFile);
		
    	for(int i=0;i<arrayDiFile.length;i++){
	          //model.addRow(new Object[]{arrayDiFile[i], Boolean.FALSE});
	     }
    	
    	
        Object[][] data = null;
        
        DefaultTableModel model = new DefaultTableModel(data, columns);
        for(int i=0;i<arrayDiFile.length;i++){
	          model.addRow(new Object[]{arrayDiFile[i], Boolean.FALSE});
	     }
        final JCheckBox checkBox = new JCheckBox();

        table = new JTable(model) {

            private static final long serialVersionUID = 1L;

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return Boolean.class;
        
                    default:
                        return String.class;
                }
            }
        };

        //table.setPreferredScrollableViewportSize(table.getPreferredSize());
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
            	TableCheckBox frame = new TableCheckBox();
                frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocation(150, 150);
                frame.setVisible(true);
            }

        });
    }
}
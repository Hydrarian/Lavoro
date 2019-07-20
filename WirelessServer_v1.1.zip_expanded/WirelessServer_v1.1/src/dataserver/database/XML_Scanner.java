package dataserver.database;

import dataserver.SystemConstant;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.File;
import org.xml.sax.SAXException;

public class XML_Scanner implements Callable<Object> {
    private static long last_data_file = 0;
    public static List<Gateway_Config> Gateway_Conf = new ArrayList<Gateway_Config>();
    
    private enum Gateway_Conf_Pos {
        MAC(0),
        DatabaseV(1),
        FileExport(2),
        Timer1(3),
        Timer2(4),
        Timer3(5),
        Timer4(6),
        Timer5(7),
        Timer6(8),
        Timer7(9),
        Timer8(10),
        Timer9(11),
        Timer10(12),
        StatNod(13),
        StatGat(14),
        MedSens(15),
        TimerS(16),
        LimiteTX(17),
        FinestraStat(18),
        AttRete(19),
        N_PARAMS(20);
        
        private final int value;
        private Gateway_Conf_Pos(int value){
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    }

    public static void ScanFile() throws InterruptedException {
        File file;
        while (true) {
            Thread.sleep(1000);
            /* Check the date of File */
            try {
                file = new File((String)SystemConstant.serverParameters.get(SystemConstant.CONFIG_FILE_PATH));
                /* check if the path is correct */
                if (!file.canRead()){
                     System.out.println("\n Config File not found in: " + (String)SystemConstant.serverParameters.get(SystemConstant.CONFIG_FILE_PATH));
                }
            }catch (Exception e){
                file = null;
                System.out.println("\n Config File not found in: " + (String)SystemConstant.serverParameters.get(SystemConstant.CONFIG_FILE_PATH));
            }
            long last = file.lastModified();
            
            if (last != last_data_file) {
                /* File modified, update new config !!! */
                try {
                    /* Open the Config XML file */
                    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

                    DocumentBuilder builder = documentFactory.newDocumentBuilder();
                    Document document = builder.parse(new File((String)SystemConstant.serverParameters.get(SystemConstant.CONFIG_FILE_PATH)));

                    NodeList Gateway = document.getElementsByTagName("Gateway");

                    System.out.println("\nTotale Gateway: " + Gateway.getLength());
                    
                    /* clear the list */
                    Gateway_Conf.clear();
                    /* Update configuration and update the list */
                    for (int i = 0; i < Gateway.getLength(); i++) {
                        Node nodo = Gateway.item(i);

                        if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                            Element Gat = (Element) nodo;
                            
                            /* add new element into the list */
                            Gateway_Conf.add(i,new Gateway_Config());
                            Gateway_Config Gateway_C = (Gateway_Config)Gateway_Conf.get(i);

                            Gateway_C.MAC = Gat.getElementsByTagName("MAC").item(0).getFirstChild().getNodeValue();
                            Gateway_C.DatabaseV = Integer.parseInt(Gat.getElementsByTagName("DatabaseV").item(0).getFirstChild().getNodeValue());
                            Gateway_C.FileExport = Gat.getElementsByTagName("FileExport").item(0).getFirstChild().getNodeValue();
                            Gateway_C.Timer1 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer1").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer2 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer2").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer3 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer3").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer4 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer4").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer5 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer5").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer6 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer6").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer7 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer7").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer8 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer8").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer9 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer9").item(0).getFirstChild().getNodeValue());
                            Gateway_C.Timer10 = Gateway_C.TimerConversion(Gat.getElementsByTagName("Timer10").item(0).getFirstChild().getNodeValue());
                            Gateway_C.StatNod = Gateway_C.Statistic(Gat.getElementsByTagName("StatisticheNodi").item(0).getFirstChild().getNodeValue());
                            Gateway_C.StatGat = Gateway_C.Statistic(Gat.getElementsByTagName("StatisticheGateway").item(0).getFirstChild().getNodeValue());
                            Gateway_C.MedSens = Gateway_C.MediaSensori(Gat.getElementsByTagName("MediaSensori").item(0).getFirstChild().getNodeValue());
                            Gateway_C.TimerS = Gateway_C.TimerSensori(Gat.getElementsByTagName("TimerSensori").item(0).getFirstChild().getNodeValue());
                            Gateway_C.LimiteTX = Gateway_C.Limite_TX(Gat.getElementsByTagName("LimiteTX").item(0).getFirstChild().getNodeValue());
                            Gateway_C.FinestraStat = Gateway_C.FinestraStatistica(Gat.getElementsByTagName("FinestraStatistica").item(0).getFirstChild().getNodeValue());
                            Gateway_C.AttRete = Gateway_C.AttivazioneRete(Gat.getElementsByTagName("AttivazioneRete").item(0).getFirstChild().getNodeValue());
                            
                            /* Validate version */
                            Gateway_C.DatabaseV = Gateway_C.DatabaseV % 1000;
                            if (Gateway_C.DatabaseV == 0)
                            {
                                Gateway_C.DatabaseV = 1;
                            }
                            
                            System.out.println("\nNome: " + Gateway_C.MAC + "  Versione: " + Gat.getElementsByTagName("DatabaseV").item(0).getFirstChild().getNodeValue());

                        }
                    }
                    /* Operation Completed */
                    last_data_file = file.lastModified();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /* Checks if a MAC in in List */
    public static boolean IS_inList(String Mac){
        boolean ret = false;
        for (Gateway_Config Gateway_C : Gateway_Conf){
            if (Gateway_C.MAC.equals(Mac)){
                ret = true;
            }
        }
        return ret;
    }
    
    /* Return the paramters associated to a identified MAC */
    public static Gateway_Config ScanGatewayConf(String Mac) {
        Gateway_Config ret = null;
        for (Gateway_Config Gateway_C : Gateway_Conf) {
            if (Gateway_C.MAC.equals(Mac)) {
                ret = Gateway_C;
                return ret;
            }
        }
        return ret;
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object call() throws Exception {
        try {
            ScanFile ();
        }catch(Exception e){
            throw new UnsupportedOperationException("Not supported yet.");
        }
        return null;
    }

    public static void AddGateway(String[] Gateway_IN) throws SAXException, XPathExpressionException {
        /*
         *      public String MAC;          0
                public int  DatabaseV;      1
                public String FileExport    2
                public char Timer1;         3
                public char Timer2;         4
                public char Timer3;         5
                public char Timer4;         6
                public char Timer5;         7
                public char Timer6;         8
                public char Timer7;         9
                public char Timer8;         10
                public char Timer9;         11
                public char Timer10;        12
                public char StatNod;        13
                public char StatGat;        14
                public char MedSens;        15
                public char TimerS;         16
                public char LimiteTX;       17
                public char FinestraStat;   18
                public char AttRete;        19
         * 
         */
        
        if (Gateway_Conf_Pos.N_PARAMS.equals(Gateway_IN.length)){
            System.out.print("Error AddGateway");
            return;
        }
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            //Parse existing file to DOM
            Document doc;
            Element rootElement;
            
            try {
                doc = docBuilder.parse(new File((String)SystemConstant.serverParameters.get(SystemConstant.CONFIG_FILE_PATH)));
                // root elements
                rootElement = doc.getDocumentElement();
            } catch (IOException ex) {
//                Logger.getLogger(XML_Scanner.class.getName()).log(Level.SEVERE, null, ex);
                System.out.print("Save new File");
                doc = docBuilder.newDocument();
                rootElement = doc.createElement("configurazioni");
                doc.appendChild(rootElement);
            }
            
            /* VERIFICO LA PRESENZA DI UNA VECCHIA CONFIGURAZIONE SUL MAC SELEZIONATO */
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            XPathExpression expression = xpath.compile("//configurazioni/Gateway[MAC='"+Gateway_IN[Gateway_Conf_Pos.MAC.getValue()]+"']");
            
            Element MacElement = (Element) expression.evaluate(doc,XPathConstants.NODE);
            int old_DatV = 1;
            if (MacElement != null){
                /* Esisteva una configurazione precedente che cancello, ma prima salvo il valore di DatabaseV e lo incremento di 1*/
                old_DatV = Integer.parseInt(MacElement.getElementsByTagName("DatabaseV").item(0).getFirstChild().getNodeValue()) + 1;
                MacElement.getParentNode().removeChild(MacElement);
            }
            Gateway_IN[Gateway_Conf_Pos.DatabaseV.getValue()] = Integer.toString(old_DatV);
            
            // Gateway
            Element Gateway = doc.createElement("Gateway");
            rootElement.appendChild(Gateway);
            
            // nickname elements
            Element MAC = doc.createElement("MAC");
            MAC.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.MAC.getValue()]));
            Gateway.appendChild(MAC);
            
            Element DatabaseV = doc.createElement("DatabaseV");
            DatabaseV.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.DatabaseV.getValue()]));
            Gateway.appendChild(DatabaseV);
            
            Element FileExport = doc.createElement("FileExport");
            FileExport.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.FileExport.getValue()]));
            Gateway.appendChild(FileExport);
            
            Element Timer1 = doc.createElement("Timer1");
            Timer1.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer1.getValue()]));
            Gateway.appendChild(Timer1);
            
            Element Timer2 = doc.createElement("Timer2");
            Timer2.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer2.getValue()]));
            Gateway.appendChild(Timer2);
            
            Element Timer3 = doc.createElement("Timer3");
            Timer3.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer3.getValue()]));
            Gateway.appendChild(Timer3);
            
            Element Timer4 = doc.createElement("Timer4");
            Timer4.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer4.getValue()]));
            Gateway.appendChild(Timer4);
            
            Element Timer5 = doc.createElement("Timer5");
            Timer5.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer5.getValue()]));
            Gateway.appendChild(Timer5);
            
            Element Timer6 = doc.createElement("Timer6");
            Timer6.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer6.getValue()]));
            Gateway.appendChild(Timer6);
            
            Element Timer7 = doc.createElement("Timer7");
            Timer7.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer7.getValue()]));
            Gateway.appendChild(Timer7);
            
            Element Timer8 = doc.createElement("Timer8");
            Timer8.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer8.getValue()]));
            Gateway.appendChild(Timer8);
            
            Element Timer9 = doc.createElement("Timer9");
            Timer9.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer9.getValue()]));
            Gateway.appendChild(Timer9);
            
            Element Timer10 = doc.createElement("Timer10");
            Timer10.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.Timer10.getValue()]));
            Gateway.appendChild(Timer10);
            
            Element StatisticheNodi = doc.createElement("StatisticheNodi");
            StatisticheNodi.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.StatNod.getValue()]));
            Gateway.appendChild(StatisticheNodi);
            
            Element StatisticheGateway = doc.createElement("StatisticheGateway");
            StatisticheGateway.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.StatGat.getValue()]));
            Gateway.appendChild(StatisticheGateway);
            
            Element MediaSensori = doc.createElement("MediaSensori");
            MediaSensori.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.MedSens.getValue()]));
            Gateway.appendChild(MediaSensori);
            
            Element TimerSensori = doc.createElement("TimerSensori");
            TimerSensori.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.TimerS.getValue()]));
            Gateway.appendChild(TimerSensori);
            
            Element LimiteTX = doc.createElement("LimiteTX");
            LimiteTX.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.LimiteTX.getValue()]));
            Gateway.appendChild(LimiteTX);
            
            Element FinestraStatistica = doc.createElement("FinestraStatistica");
            FinestraStatistica.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.FinestraStat.getValue()]));
            Gateway.appendChild(FinestraStatistica);
            
            Element AttivazioneRete = doc.createElement("AttivazioneRete");
            AttivazioneRete.appendChild(doc.createTextNode(Gateway_IN[Gateway_Conf_Pos.AttRete.getValue()]));
            Gateway.appendChild(AttivazioneRete);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult((String)SystemConstant.serverParameters.get(SystemConstant.CONFIG_FILE_PATH));

            // Output to console for testing
//            StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("\nFile saved!\n");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    // shorten way
    // staff.setAttribute("id", "1");
}




package dataserver;

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Utils {

    static protected Logger log = Logger.getLogger(Utils.class);

    public static void initLogger() {
        BasicConfigurator.configure();
        Properties props = new Properties();
        ResourceBundle log4jb = ResourceBundle.getBundle("log4j");
        Enumeration keys = log4jb.getKeys();
        while (keys.hasMoreElements()) {
            String k = (String) keys.nextElement();
            String v = log4jb.getString(k);
            props.put(k, v);
        }
        org.apache.log4j.LogManager.resetConfiguration();
        PropertyConfigurator.configure(props);
    }

     public static String buildSchemaFromParamterBytes(String schema, byte[] param) throws Exception {
        int length = 0;
        String temp = schema;
        StringBuffer newSchema = new StringBuffer();
        StringTokenizer r=new StringTokenizer(schema,"\r\n");
        while (r.hasMoreTokens()){
           String[] s = r.nextToken().split(",");
           length = length + Integer.parseInt(s[3]);
           System.out.println(s[2] + " " + s[3]);
        }        
        if (param.length != length) {
            throw new Exception("Errore lunghezza Parametri ricevuti");
        }else{
        length = 0;
        r=new StringTokenizer(schema,"\r\n");
        while (r.hasMoreTokens()){
           String[] s = r.nextToken().split(",");            
            int dim = Integer.parseInt(s[3]);
            byte[] value = new byte[dim];
            System.arraycopy(param, length, value, 0, dim);
            if (s[5].equals("") & s[6].equals("")) {
                newSchema.append(newLineSchema(s, new String(value).trim()));
            } else {
                int k = 0;
                 for (int i = 0; i < dim; i++) {
                    int shift = (dim - 1 - i) * 8;
                    int t= (value[dim-1-i] & 0x000000FF) << shift;
                    k+=t;
                }
                newSchema.append(newLineSchema(s, Integer.toString(k)));            
            }        
           length = length + dim;
          }
        return newSchema.toString();
        
        }
}
    private static String newLineSchema(String[] s, String NewValue) {
        StringBuffer newline = new StringBuffer();
        s[1] = "," + NewValue;
        for (int i = 1; i < s.length - 1; i++) {
            newline.append(s[i] + ",");
        }
        newline.append(s[s.length - 1]);
        newline.append("\r\n");
        return newline.toString();
    }

    public static char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    public static String byteToHex(byte data) {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }


    public static void main(String[] args) {
        Utils u = new Utils();
        String str = "0708090a0b0c";

        Long decimal = Long.parseLong(str,16);
        String hexString = Long.toHexString(decimal);
        System.out.println("Hex to decimal =  [" + decimal + "]" );
        System.out.println("Decimal to hex =  [" + hexString + "]" );
    }
}

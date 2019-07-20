package dataserver.database;

import dataserver.SystemConstant;

/**
 *
 * @author MediCon
 */
public class Gateway_Config {
    /* STRUTTURA CONFIGURAZIONI */
    public String MAC;
    public int  DatabaseV;
    public String FileExport;
    public char Timer1;
    public char Timer2;
    public char Timer3;
    public char Timer4;
    public char Timer5;
    public char Timer6;
    public char Timer7;
    public char Timer8;
    public char Timer9;
    public char Timer10;
    public char StatNod;
    public char StatGat;
    public char MedSens;
    public char TimerS;
    public char LimiteTX;
    public char FinestraStat;
    public char AttRete;
    
    /* CONVERSIONI DATI */
    public char TimerConversion (String args){
        char ret;
        if (args.equals("OFF")) {
            ret = SystemConstant.GATEWAY_TIMER_OFF;
        } else {
            String[] hourMin = args.split(":");
            int hour = Integer.parseInt(hourMin[0]);
            int mins = Integer.parseInt(hourMin[1]);
            int hoursInsec = hour * 3600;
            hoursInsec = hoursInsec + (mins * 60);
            hoursInsec = hoursInsec / 600;
            ret = (char)(hoursInsec);
        }
        return ret;
    }

    public char Statistic(String args) {
        char ret = 0;
        if (args.equals("OFF")) {
            ret = 0;
        } else if (args.equals("ON")) {
            ret = 1;
        }
        return ret;
    }
    
    public char MediaSensori(String args) {
        char ret = 0;
        if (args.equals("OFF")) {
            ret = 0;
        } else {
            int num = Integer.parseInt(args);
            if ((num <= SystemConstant.GATEWAY_MEDIA_SENSORI_MAX) && (num >= SystemConstant.GATEWAY_MEDIA_SENSORI_MIN)){
                ret = (char)num;
            } else {
                ret = 0;
            }
        }
        return ret;
    }
    
    public char TimerSensori(String args) {
        char ret = 0;
        if (args.equals("OFF")) {
            ret = 0;
        } else {
            int num = Integer.parseInt(args);
            if ((num <= SystemConstant.GATEWAY_TIMER_SENSORI_MAX) && (num >= SystemConstant.GATEWAY_TIMER_SENSORI_MIN)) {
                ret = (char) num;
            } else {
                ret = 0;
            }
        }
        return ret;
    }
    
    public char Limite_TX(String args) {
        char ret = 0;
        int num = Integer.parseInt(args);
        if ((num <= 255)) {
            ret = (char) num;
        } else {
            ret = 0;
        }

        return ret;
    }
    
    public char FinestraStatistica (String args) {
        return Limite_TX(args);
    }
    
    public char AttivazioneRete(String args) {
        return Statistic(args);
    }
}

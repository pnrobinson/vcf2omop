package org.monarchinitiative.oncembobulator.data;

import javafx.beans.property.IntegerProperty;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class OncoKBVariant {
    static Logger logger = Logger.getLogger(OncoKBVariant.class.getName());
    private String genesymbol=null;
    private String mutation=null;
    private String cancer=null;
    private String level=null;
    private String treatment=null;
    private List<Integer> pmidlst = null;

    public String getGenesymbol() {
        return genesymbol;
    }

    public String getMutation() {
        return mutation;
    }

    public String getCancer() {
        return cancer;
    }

    public String getLevel() {
        return level;
    }

    public String getTreatment() {
        return treatment;
    }

    public List<Integer> getPmidlst() {
        return pmidlst;
    }

    public OncoKBVariant(String symbol, String mutation, String cancer, String lev, String rx, String pmids) {
        this.genesymbol=symbol;
        this.mutation=mutation;
        this.cancer=cancer;
        this.level=lev;
        this.treatment=rx;
        this.pmidlst=new ArrayList<>();
        String A[]=pmids.split(",");
        for (String p: A) {
            try {
                Integer pmid = Integer.parseInt(p.trim());
                pmidlst.add(pmid);
            } catch (NumberFormatException e) {
                //logger.error("Unable to parse pmid list: "+pmids);
                //logger.error(e,e);
                // Skip, there are some oncoKB entries that are blank etc.
                continue;
            }

        }
        //System.out.println(toString());
    }





    @Override
    public String toString() {
        String pmidlist="";
        if (pmidlst.size()>0) {
            StringBuilder sb=new StringBuilder();
            sb.append(String.format("pmid:%d",pmidlst.get(0)));
            for (int i=1;i<pmidlst.size();i++) {
                sb.append(String.format(";pmid:%d",pmidlst.get(i)));
            }
            pmidlist=sb.toString();
        }
        return String.format("%s\t%s\t%s\t%s\t%s\t%s",genesymbol,mutation,cancer,level,treatment,pmidlist);
    }
}

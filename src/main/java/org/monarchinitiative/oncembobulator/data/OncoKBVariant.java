package org.monarchinitiative.oncembobulator.data;

import java.util.List;

public class OncoKBVariant {

    private String genesymbol=null;
    private String mutation=null;
    private String cancer=null;
    private String level=null;
    private String treatment=null;
    private List<Integer> pmidlst = null;


    public OncoKBVariant(String symbol,String mutation, String cancerType, String category, String rx, String pmids) {
        System.out.println("variant: "+ symbol +":"+mutation);
    }
}

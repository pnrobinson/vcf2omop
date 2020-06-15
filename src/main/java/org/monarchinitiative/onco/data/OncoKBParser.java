package org.monarchinitiative.onco.data;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OncoKBParser {
    static Logger logger = LoggerFactory.getLogger(OncoKBParser.class);
    private List<OncoKBVariant> variants=null;

    public OncoKBParser(String path){
        variants=new ArrayList<>();
        parse(path);
    }

    public List<OncoKBVariant> getOncoKBVariants() { return this.variants; }

    /**
     * Parse the OncoKB file. A typical line looks like this
     * <pre>
     *    KIT	K642E	Gastrointestinal Stromal Tumor	1	Imatinib	18235121, 12181401, 16098458, 15451219	von Mehren et al. Abstract# 10016, ASCO 2011 http://meetinglibrary.asco.org/content/82574-102
     * </pre>
     * @param path
     */
    private void parse(String path){

        try{
            BufferedReader br = new BufferedReader(new FileReader(path));
            String header=br.readLine(); /* skip the header line */
            String line=null;
            while ((line=br.readLine())!=null) {
                //System.out.println(line);
                String A[]=line.split("\t");
                String symbol=A[0];
                String mutation=A[1];
                String cancer=A[2];
                String level=A[3];
                String Rx=A[4];
                String pmids=A[5];
                OncoKBVariant var=new OncoKBVariant(symbol,mutation,cancer,level,Rx,pmids);
                variants.add(var);
            }
            br.close();
        }catch (IOException e){
            logger.error("Could not parse "+path);
            logger.error(e.getMessage());
        }
        logger.info("Parsed a total of "+ variants.size()+" variants from "+path);
    }
}

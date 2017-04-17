
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by nronsse on 4/5/17.
 */
public class SliSku {


    private static String PATH = "/Users/nronsse/Downloads/OneDrive_2017-04-05/data_small.txt";
    private static final ConcurrentHashMap<String, Map<String,Double>>  productMap = new ConcurrentHashMap<>(); // unused saved for later
    private static int THREAD_POOL = 0; //unused saved for later
    private static Map<String, List<SkuEntity>> MY_MAP = new HashMap<String, List<SkuEntity>>(); // forced into use half way through an implementation.

    /**
     *Provide the application with a sku and Path it will return the top 5 sku values related to the sku provided.
     * Accpets arguments --sku --path
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException,InterruptedException {

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("s","sku",true,"Sku value");
        options.addOption("p", "path",true,"Path value for file location");
        CommandLine line = null;
        try {
            line = parser.parse(options,args);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (line.hasOption("sku")){

        }
        else
            System.out.println("Use --sku= to enter a sku value");

        PATH = line.getOptionValue("p","/Users/nronsse/Downloads/OneDrive_2017-04-05/data_small.txt");
        VisitorLogLoader DataLoader = new VisitorLogLoader(PATH);
        MY_MAP = DataLoader.buildVisitorRecord();


        final CommandLine finalLine = line;
        MY_MAP.forEach((s, skuEntities) -> {
            DataLoader.buildSpecificProductRank(skuEntities, finalLine.getOptionValue("s"));

        }
        );
        try {

            DataLoader.topFiveSku(line.getOptionValue("sku"));
        }
        catch (NullPointerException e){
            System.out.println("Sku "+line.getOptionValue("sku")+ "not found please enter a valid Sku");
        }
    }









}

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by nronsse on 4/7/17.
 */
public class VisitorLogLoader {
    private static String PATH;
    private static Map<String, List<SkuEntity>> MY_MAP = new HashMap<String, List<SkuEntity>>();
    private static ConcurrentHashMap<String, Map<String,Double>> myProdMap = new ConcurrentHashMap<>();

    public VisitorLogLoader(String path) {
        this.PATH = path;

    }

    /**
     * Builds the initial list of Visitor product relationships. Ideally should be split into a producer, consumer message bus applicaiton.
     * @return MY_MAP - unused return right now. I want to return this so that it can be split out and called with a threaded worker.
     * @throws IOException
     */
    public Map<String,List<SkuEntity>> buildVisitorRecord() throws IOException {
        String line;
        String[] words;
        FileInputStream inputStream = null;
        List<SkuEntity> skuList;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(PATH);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                // could use substring for faster splits.
                words=line.split("\t");
                    skuList = MY_MAP.getOrDefault(words[1], new ArrayList<>());
                    skuList.add(new SkuEntity(words[2], words[0]));
                MY_MAP.put(words[1],skuList);
            }

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
        return MY_MAP;
    }
/*
Unused
 */

    /**
     * Used for testing, there is a logic bug in here for me to find.
     * @param entityList
     */
    public void buildFullProductRank(List<SkuEntity> entityList) {
        Calendar now = Calendar.getInstance();
        long compareTime = now.getTimeInMillis();

        Map<String, Double> prodList;

        for (int i = 0; i < entityList.size(); i++) {
            for (int j = i + 1; j < entityList.size(); j++) {
                String skuId1 = entityList.get(i).getId();
                long skuTs1 = entityList.get(i).getTs();
                String skuId2 = entityList.get(j).getId();
                long skuTs2 = entityList.get(j).getTs();

                double skuScore1 = SkuScore(compareTime, Long.valueOf(skuTs1));
                double skuScore2 = SkuScore(compareTime, Long.valueOf(skuTs2));
                //should be thrown in a function to avoid duplicate code but my desire for readability ATM is stronger
                prodList = myProdMap.getOrDefault(skuId1,new HashMap<>());
                prodList.put(skuId2, prodList.getOrDefault(skuId2,0.0) + skuScore1 * skuScore2);
                myProdMap.put(skuId1,prodList);
                prodList = myProdMap.getOrDefault(skuId1,new HashMap<>());
                prodList.put(skuId1, prodList.getOrDefault(skuId1,0.0) + skuScore1 * skuScore2);
                myProdMap.put(skuId2,prodList);
            }
        }
    }

    /**
     * great candidate as a threaded operation as this scans only one of the many visitor product lists.
     * Only loads the map of scores with values that are needed to find the target SKU items.
     * Began work on splitting this into a threaded task but that will have to be in my own time.
     * @param entityList
     * @param targetSku
     */
    public void buildSpecificProductRank(List<SkuEntity> entityList,String targetSku){
        Calendar now = Calendar.getInstance();
        long compareTime = now.getTimeInMillis();

        Map<String,Double> prodList;
        //
        for (int i = 0; i < entityList.size(); i++) {
            for (int j = i+1; j < entityList.size(); j++) {
                String skuId1 = entityList.get(i).getId();
                long skuTs1 = entityList.get(i).getTs();
                String skuId2 = entityList.get(j).getId();
                long skuTs2 = entityList.get(j).getTs();

                //Only Bother with relevant sku's
                if (skuId1.equals(targetSku) || skuId2.equals(targetSku)) {
                    double skuScore1 = SkuScore(compareTime, Long.valueOf(skuTs1));
                    double skuScore2 = SkuScore(compareTime, Long.valueOf(skuTs2));
                    if(skuId1.equals(targetSku)) {
                        prodList = myProdMap.getOrDefault(skuId1, new HashMap<>());
                        prodList.put(skuId2, prodList.getOrDefault(skuId2, 0.0) + skuScore1 * skuScore2);
                        myProdMap.put(skuId1, prodList);
                    }
                    else {
                        prodList = myProdMap.getOrDefault(skuId1, new HashMap<>());
                        prodList.put(skuId1, prodList.getOrDefault(skuId1, 0.0) + skuScore1 * skuScore2);
                        myProdMap.put(skuId2, prodList);
                    }
                }
            }
        }

    }

    /**
     * converts map to a list and sorts by value. then returns the map
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Takes in target sku and loops 5 times.. Yup.
     * @param sku
     */
    public void topFiveSku(String sku){
        Map<String,Double> prodList;
        prodList=myProdMap.get(sku);
        System.out.println("The top 5 related Sku's for "+sku);
        System.out.println("SkuIn\tSkuout\tSkuscore");
        prodList=sortByValue(prodList);
        Iterator it = prodList.entrySet().iterator();
        for (int i = 0; i < 5 ; i++) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(sku+"\t"+pair.getKey() +"\t"+pair.getValue().toString());
        }
    }
    //some testing method
    public void printAllSku(String sku){
        Map<String,Double> prodList;
        prodList=myProdMap.get(sku);
        prodList=sortByValue(prodList);
        System.out.println("the SKU is : "+sku);
        prodList.forEach((k,v) ->System.out.println("   Sku: "+k+"   value: " +v.toString()));

    }
    //some testing method
    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    /**
     * this does not match the logic in the SLI document, it seemed that the document used a decimal representation of days.
     * I am sure there is an easy implementation of decimal day I just used the difference in unix timestamp hours which should yield equivalent results.
     * @param now -- timestamp of the application start, dont want to have the results skew with a long running application.
     * @param start -- timestamp of the sku
     * @return
     */
    public static double SkuScore(long now,long start){
        long timeDifference = TimeUnit.MILLISECONDS.toHours(now-start);
        double score = timeDifference * .95;
        return score;

    }


}


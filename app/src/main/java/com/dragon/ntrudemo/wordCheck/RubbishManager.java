package com.dragon.ntrudemo.wordCheck;

import android.content.Context;

import com.dragon.ntrudemo.wordCheck.knife.Beef;
import com.dragon.ntrudemo.wordCheck.knife.CharSet;
import com.dragon.ntrudemo.wordCheck.knife.Collector;
import com.dragon.ntrudemo.wordCheck.knife.Paoding;
import com.dragon.ntrudemo.wordCheck.knife.PaodingMaker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/30 0030.
 */
public class RubbishManager {

    private float just = 0.9f;
    private Paoding PAO = null;
    private HashMap<String, Double> MapPossible =
            new HashMap<String, Double>(3000, 0.75f);


    public RubbishManager(Context context) {
        PAO = PaodingMaker.make();
        try {
            readMap(context, "fremap1.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readMap(Context context, String mapName) throws IOException {

        InputStream is = context.getResources().getAssets().open(mapName);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean firstInMap = true;
        while ((line = buf.readLine()) != null) {
            if (firstInMap) {
                firstInMap = false;
                if (line.length() > 0 && CharSet.isBom(line.charAt(0))) {
                    line = line.substring(1);
                }
            }
            if (line.startsWith("<")) {
                int indexPre = line.indexOf('<');
                int midIndex = line.indexOf(',');
                int indexAfter = line.indexOf('>');
                String word = line.substring(indexPre + 1, midIndex);
                String freTemp = line.substring(midIndex + 1, indexAfter);
                double fre = Double.parseDouble(freTemp);
                System.out.println("word = " + word + ",fre = " + fre);
                MapPossible.put(word, fre);
            }
        }
        buf.close();
    }


    /**
     * justifyRubbish类用于识别垃圾短信
     *
     * @param collect 文本分词之后的结果
     * @return boolean true如果短信是正常短信
     */

    public boolean justifyRubbish(WordToken collect) {

        boolean isOrdinary = true;

        //短信是垃圾短信的概率
        double pR = 0;
        //所有特征词同时出现的概率
        double pRSimul = 1;
        //所有特征词同时不出现的概率
        double pRDiff = 1;

        boolean hasWord = false;
        boolean firstInMsg = true;
        Map<String, Integer> CountBad = new HashMap<String, Integer>(collect.size());
        Map<String, Double> FreqBad = new HashMap<String, Double>(collect.size());

        for (Iterator<Token> iter = collect.iterator(); iter.hasNext(); ) {
            Token t = iter.next();
            String key = t.getWord();
            System.out.println("key = " + key);
            Double freq = MapPossible.get(key);
            if (freq != null) {
                if (firstInMsg) {
                    hasWord = true;
                    firstInMsg = false;
                }
                if (CountBad.containsKey(key)) {
                    int keyCount = CountBad.get(key).intValue();
                    keyCount++;
                    CountBad.put(key, keyCount);
                } else
                    CountBad.put(key, Integer.valueOf(1));
                FreqBad.put(key, freq);
            }
        }

        for (Map.Entry<String, Double> m : FreqBad.entrySet()) {
            double valOfWord = m.getValue().doubleValue();
            String key = m.getKey();
            int cWord = CountBad.get(key).intValue();
            pRSimul = pRSimul * Math.pow(valOfWord, cWord * key.length());
            pRDiff = pRDiff * Math.pow((1 - valOfWord), cWord * key.length());
        }

        if (hasWord)
            pR = pRSimul / (pRSimul + pRDiff);

        System.out.println(pR);
        if (pR > just)
            isOrdinary = false;

        return isOrdinary;
    }

    /**
     * cutWord方法用于对输入的中文短信正文进行分词处理
     *
     * @param msgContent 输入的短信文本正文
     * @return Collector 分词结果收集类
     */
    public WordToken cutWord(String msgContent) {

        WordToken collect = new WordToken();
        Beef msgBeef = null;

        //去除传入的信息中的符号
        int testOffset = 0;
        int testLength = msgContent.length();
        for (int i = testOffset; i < testLength; i++) {
            char word = msgContent.charAt(i);
            if (!CharSet.isArabianNumber(word))
                if (!CharSet.isCjkUnifiedIdeographs(word))
                    if (!CharSet.isLantingLetter(word)) {
                        msgContent = msgContent.substring(0, i) +
                                msgContent.substring(i + 1, testLength);
                        //System.out.println(msgContent);
                        testLength--;
                        i--;
                    }
        }
        msgContent = msgContent + ".";

        char[] ch = msgContent.trim().toCharArray();
        msgBeef = new Beef(ch, 0, ch.length);

        PAO.dissect((Collector) collect, msgBeef, 0);
        return collect;
    }
}

/**
 * Copyright 2007 The Apache Software Foundation
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dragon.ntrudemo.wordCheck.knife;

import com.dragon.ntrudemo.wordCheck.dictionary.BinaryDictionary;
import com.dragon.ntrudemo.wordCheck.dictionary.Dictionary;
import com.dragon.ntrudemo.wordCheck.dictionary.HashBinaryDictionary;
import com.dragon.ntrudemo.wordCheck.dictionary.ReadDictionary;
import com.dragon.ntrudemo.wordCheck.dictionary.Word;
import com.dragon.ntrudemo.wordCheck.dictionary.support.detection.Detector;
import com.dragon.ntrudemo.wordCheck.dictionary.support.detection.DifferenceListener;
import com.dragon.ntrudemo.wordCheck.dictionary.support.filewords.FileWordsReader;
import com.dragon.ntrudemo.wordCheck.exception.PaodingAnalysisException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 中文字典缓存根据地,为{@link CJKKnife}所用。<br>
 * 从本对象可以获取中文需要的相关字典。包括词汇表、姓氏表、计量单位表、忽略的词或单字等。
 * <p/>
 *
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * @see CJKKnife
 * @since 1.0
 */
public class CompiledFileDictionaries implements Dictionaries {

    /**
     * 词汇表字典
     */
    protected Dictionary vocabularyDictionary;

    /**
     * lantin+cjk的词典
     */
    protected Dictionary combinatoricsDictionary;

    /**
     * 姓氏字典
     */
    protected Dictionary confucianFamilyNamesDictionary;

    /**
     * 忽略的单字
     */
    protected Dictionary noiseCharactorsDictionary;

    /**
     * 忽略的词语
     */
    protected Dictionary noiseWordsDictionary;

    /**
     * 计量单位
     */
    protected Dictionary unitsDictionary;

    // -------------------------------------------------

    protected String dicHome;
    protected String noiseCharactor;
    protected String noiseWord;
    protected String unit;
    protected String confucianFamilyName;
    protected String combinatorics;
    protected String charsetName;

    // ----------------------

    public CompiledFileDictionaries() {
    }

    public CompiledFileDictionaries(String dicHome, String noiseCharactor,
                                    String noiseWord, String unit, String confucianFamilyName,
                                    String combinatorics, String charsetName) {
        this.dicHome = dicHome;
        this.noiseCharactor = noiseCharactor;
        this.noiseWord = noiseWord;
        this.unit = unit;
        this.confucianFamilyName = confucianFamilyName;
        this.combinatorics = combinatorics;
        this.charsetName = charsetName;
    }

    public String getDicHome() {
        return dicHome;
    }

    public void setDicHome(String dicHome) {
        this.dicHome = dicHome;
    }

    public String getNoiseCharactor() {
        return noiseCharactor;
    }

    public void setNoiseCharactor(String noiseCharactor) {
        this.noiseCharactor = noiseCharactor;
    }

    public String getNoiseWord() {
        return noiseWord;
    }

    public void setNoiseWord(String noiseWord) {
        this.noiseWord = noiseWord;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getConfucianFamilyName() {
        return confucianFamilyName;
    }

    public void setConfucianFamilyName(String confucianFamilyName) {
        this.confucianFamilyName = confucianFamilyName;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public void setLantinFllowedByCjk(String lantinFllowedByCjk) {
        this.combinatorics = lantinFllowedByCjk;
    }

    public String getLantinFllowedByCjk() {
        return combinatorics;
    }

    // -------------------------------------------------

    /**
     * 词汇表字典
     *
     * @return
     */
    public synchronized Dictionary getVocabularyDictionary() {
        if (vocabularyDictionary == null) {
            // 大概有5639个字有词语，故取0x2fff=x^13>8000>8000*0.75=6000>5639
            vocabularyDictionary = new ReadDictionary();
            //vocabularyDictionary = new HashBinaryDictionary(
            //		getVocabularyWords(), 0x2fff, 0.75f);
        }
        return vocabularyDictionary;
    }

    /**
     * 姓氏字典
     *
     * @return
     */
    public synchronized Dictionary getConfucianFamilyNamesDictionary() {
        if (confucianFamilyNamesDictionary == null) {
            confucianFamilyNamesDictionary = new BinaryDictionary(
                    getConfucianFamilyNames());
        }
        return confucianFamilyNamesDictionary;
    }

    /**
     * 忽略的词语
     *
     * @return
     */
    public synchronized Dictionary getNoiseCharactorsDictionary() {
        if (noiseCharactorsDictionary == null) {
            noiseCharactorsDictionary = new HashBinaryDictionary(
                    getNoiseCharactors(), 256, 0.75f);
        }
        return noiseCharactorsDictionary;
    }

    /**
     * 忽略的单字
     *
     * @return
     */
    public synchronized Dictionary getNoiseWordsDictionary() {
        if (noiseWordsDictionary == null) {
            noiseWordsDictionary = new BinaryDictionary(getNoiseWords());
        }
        return noiseWordsDictionary;
    }

    /**
     * 计量单位
     *
     * @return
     */
    public synchronized Dictionary getUnitsDictionary() {
        if (unitsDictionary == null) {
            unitsDictionary = new HashBinaryDictionary(getUnits(), 1024, 0.75f);
        }
        return unitsDictionary;
    }

    public synchronized Dictionary getCombinatoricsDictionary() {
        if (combinatoricsDictionary == null) {
            combinatoricsDictionary = new BinaryDictionary(
                    getCombinatoricsWords());
        }
        return combinatoricsDictionary;
    }



    private Detector detector;
    @Override
    public synchronized void startDetecting(int interval, DifferenceListener l) {
        if (detector != null || interval < 0) {
            return;
        }
        Detector detector = new Detector();
        detector.setHome(dicHome);
        detector.setFilter(null);
        detector.setFilter(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getPath().endsWith(".dic.compiled")
                        || pathname.getPath().endsWith(".metadata");
            }
        });
        detector.setLastSnapshot(detector.flash());
        detector.setListener(l);
        detector.setInterval(interval);
        detector.start(true);
        this.detector = detector;
    }

    public synchronized void stopDetecting() {
        if (detector == null) {
            return;
        }
        detector.setStop();
        detector = null;
    }

    // ---------------------------------------------------------------
    // 以下为辅助性的方式-类私有或package私有

    protected Word[] getDictionaryWords(String dicNameRelativeDicHome) {
        File f = new File(this.dicHome + "/" + dicNameRelativeDicHome
                + ".dic.compiled");
        System.out.println(dicNameRelativeDicHome);
        if (!f.exists()) {
            return new Word[0];
        }
        try {
            Map<String, Collection<Word>> map = FileWordsReader.readWords(f.getAbsolutePath(),
                    charsetName, LinkedList.class, ".dic.compiled");
            System.out.println("succeed");
            List<Word> wordsList = (List<Word>) map.values().iterator().next();
            return (Word[]) wordsList.toArray(new Word[wordsList.size()]);
        } catch (IOException e) {
            throw toRuntimeException(e);
        }
    }


    protected Word[] getVocabularyWords() {
        return getDictionaryWords("vocabulary");
    }

    protected Word[] getConfucianFamilyNames() {
        return getDictionaryWords(confucianFamilyName);
    }

    protected Word[] getNoiseWords() {
        return getDictionaryWords(noiseWord);
    }

    protected Word[] getNoiseCharactors() {
        return getDictionaryWords(noiseCharactor);
    }

    protected Word[] getUnits() {
        return getDictionaryWords(unit);
    }

    protected Word[] getCombinatoricsWords() {
        return getDictionaryWords(combinatorics);
    }

    // --------------------------------------

    protected RuntimeException toRuntimeException(IOException e) {
        return new PaodingAnalysisException(e);
    }
}

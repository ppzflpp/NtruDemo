package com.dragon.ntrudemo.wordCheck.dictionary.support.filewords;

import com.dragon.ntrudemo.wordCheck.dictionary.Word;
import com.dragon.ntrudemo.wordCheck.knife.CharSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


/**
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * @since 1.0
 */
public class FileWordsReader {

    public static Map<String, Set<Word>> readWords(
            String fileOrDirectory, String charsetName) throws IOException {
        SimpleReadListener l = new SimpleReadListener();
        readWords(fileOrDirectory, l, charsetName);
        return l.getResult();
    }

    public static Map<String, Collection<Word>> readWords(
            String fileOrDirectory, String charsetName, Class collectionClass, String ext) throws IOException {
        SimpleReadListener2 l = new SimpleReadListener2(collectionClass, ext);
        readWords(fileOrDirectory, l, charsetName);
        return l.getResult();
    }

    public static void readWords(String fileOrDirectory, ReadListener l, String charsetName)
            throws IOException {
        File file;
        file = new File(fileOrDirectory);
        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + fileOrDirectory + "\" not found!");
        }
        ArrayList<File> dirs = new ArrayList<File>();
        LinkedList<File> dics = new LinkedList<File>();
        String dir;
        //文件为文件夹
        if (file.isDirectory()) {
            dirs.add(file);
            dir = file.getAbsolutePath();
        } else {
            dics.add(file);
            dir = file.getParentFile().getAbsolutePath();
        }
        int index = 0;
        while (index < dirs.size()) {
            File cur = (File) dirs.get(index++);
            File[] files = cur.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    dics.add(f);
                }
            }
        }
        int i = 0;

        //读取每个文件
        for (Iterator<File> iter = dics.iterator(); iter.hasNext(); ) {
            File f = (File) iter.next();
            String name = f.getAbsolutePath().substring(
                    dir.length() + 1);
            name = name.replace('\\', '/');
            if (!l.onFileBegin(name)) {
                continue;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(f), charsetName));
            String word;
            boolean firstInDic = true;
            i++;
            System.out.println(i);
            while ((word = in.readLine()) != null) {
                if (firstInDic) {
                    firstInDic = false;
                    // ref:http://www.w3.org/International/questions/qa-utf8-bom
                    // ZERO WIDTH NO-BREAK SPACE
                    // notepad将文件保存为unitcode或utf-8时会在文件开头保存bom字符串
                    // notepad根据是否有bom来识别该文件是否是utf-8编码存储的。
                    // 庖丁字典需要将这个字符从词典中去掉
                    if (word.length() > 0 && CharSet.isBom(word.charAt(0))) {
                        word = word.substring(1);
                    }
                }
                l.onWord(word);
            }
            l.onFileEnd(name);
            in.close();
        }
    }

}

package com.dragon.ntrudemo.wordCheck.dictionary;

import com.dragon.ntrudemo.wordCheck.knife.CharSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;


/**
 * ReadDictionary类为针对android手机设计的Dictionary拓展类
 * 通过减少时间效率换取空间效率的提高
 * 词典文件被分割成若干个小文件，根据需要读取其中的一个进行分词
 * @author chencongjie
 */

public class ReadDictionary implements Dictionary {

	// -------------------------------------------------
	
	/**
	 * @param ascWords 
	 *        一组升序排列的词语
	 * @param last 
	 *        上一个输入词语第一个字的unicode码
	 * @param now 
	 *        本次输入词语第一个字的unicode码
	 * @param PATH
	 *        小词典文件存储的位置
	 */

	private Vector<Word> ascWords;
	private    int       last=0;
	private    int       now=0;

	private final static String PATH="/data/data/mars.activity05/files";

	// -------------------------------------------------

	/**
	 * 初始化ascWords
	 * 每个小词典文件一般至少包含7000个词
	 */
	public ReadDictionary() {
		ascWords=new Vector<Word>(7000,500);
	}

	// -------------------------------------------------

	/**
	 * 获得指定位置的词语
	 * @param index 词语所在位置的索引
	 * @return Word 词语
	 */
	public Word get(int index) {
		return ascWords.elementAt(index);
	}

	/**
	 * 获得ascWords的大小
	 * @return int ascWords的大小
	 */
	public int size() {
		return ascWords.size();
	}

	public Hit search(CharSequence input, int begin, int count) {
		
		//获得当前文本首字的unicode码
		if (now==0)
		   now=(int)input.charAt(begin);
		else{
			last=now;
		    now=(int)input.charAt(begin);
		}
		
		try{
			if(last !=now){
			   ascWords.clear();
	
			   //读取分词典
			   File fileIn=new File(PATH+"/"+now/1000+".dic");
			   BufferedReader in = new BufferedReader(new InputStreamReader(
					    new FileInputStream(fileIn), "UTF-8"));
			
			   String wordRead="";
			   boolean firstInDic=true;
			   
			   while((wordRead=in.readLine())!=null){
				   
		    	   if (firstInDic) {
					   firstInDic = false;
	     				//notepad将文件保存为unitcode或utf-8时会在文件开头保存bom字符串
					    // notepad根据是否有bom来识别该文件是否是utf-8编码存储的。
					    // 需要将这个字符从词典中去掉
					   if (wordRead.length() > 0 && CharSet.isBom(
							   wordRead.charAt(0))){
						   wordRead = wordRead.substring(1);
					   }
				   }
		    	   onWord(wordRead);
		       }
			}
			
		int left =0;
		int right =ascWords.size()-1;
		int pointer = 0;
		Word word = null;
		int relation;
		//二分查找
		while (left <= right) {
			pointer = (left + right) >> 1;
			word = ascWords.elementAt(pointer);
			relation = compare(input, begin, count, word);
			if (relation == 0) {
				int nextWordIndex = pointer + 1;
				if (nextWordIndex >= ascWords.size()) {
					return new Hit(pointer, word, null);
				} else {
					return new Hit(pointer, word, ascWords.elementAt(nextWordIndex));
				}
			}
			if (relation < 0)
				right = pointer - 1;
			else
				left = pointer + 1;
		}
		//没找到
		if (left >= ascWords.size()) {
			return Hit.UNDEFINED;
		}
		
		//是否为前缀
		boolean asPrex = true;
		Word nextWord = ascWords.elementAt(left);
		if (nextWord.length() < count) {
			asPrex = false;
		}
		for (int i = begin, j = 0; asPrex && j < count; i++, j++) {
			if (input.charAt(i) != nextWord.charAt(j)) {
				asPrex = false;
			}
		}
		return asPrex ? new Hit(Hit.UNCLOSED_INDEX, null, nextWord)
				: Hit.UNDEFINED;
		}catch(Exception e){
		    System.out.println(e.toString());
		    return Hit.UNDEFINED;
		}
	}

	/**
	 * 比较两个字符串
	 * @param one 其中一个字符串
	 * @param begin 首字符开始位置
	 * @param count 字符串长度
	 * @param theOther 另外一个字符串
	 * @return 比较结果
	 */
	public static int compare(CharSequence one, int begin, int count,
			CharSequence theOther) {
		for (int i = begin, j = 0; i < one.length()
				&& j < Math.min(theOther.length(), count); i++, j++) {
			if (one.charAt(i) > theOther.charAt(j)) {
				return 1;
			} else if (one.charAt(i) < theOther.charAt(j)) {
				return -1;
			}
		}
		return count - theOther.length();
	}
	
	/**
	 * 将字典文件中读入的文本进行包装
	 * @param wordText 读入的文本
	 */
	public void onWord(String wordText) {
		wordText = wordText.trim().toLowerCase();
		if (wordText.length() == 0 || wordText.charAt(0) == '#'
				|| wordText.charAt(0) == '-') {
			return;
		}
		
		if (!wordText.endsWith("]")) {
			ascWords.add(new Word(wordText));
		}
		else {
			int index = wordText.indexOf('[');
			Word w = new Word(wordText.substring(0, index));
			int mindex = wordText.indexOf("m=", index);
			int mEndIndex = wordText.indexOf("]", mindex);
			String m = wordText.substring(mindex + "m=".length(), mEndIndex);
			w.setModifiers(Integer.parseInt(m));
			ascWords.add(w);
		}
	}

}
 
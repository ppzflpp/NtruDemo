package com.dragon.ntrudemo.wordCheck;

/**
 * Token类是用来存储分词后词串中每个词语的数据结构
 *
 * @author chencongjie
 */

public class Token {
    /**
     * @param word 词语
     * @param offset 词语在句子中的起始位置
     * @param end   词语在句子中的结束位置
     */
    private String word;
    private int offset;
    private int end;

    public Token() {
    }

    public Token(String word, int offset, int end) {
        this.word = word;
        this.offset = offset;
        this.end = end;
    }

    public String getWord() {
        return word;
    }

    public int getOffset() {
        return offset;
    }

    public int getEnd() {
        return end;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}

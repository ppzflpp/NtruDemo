package com.dragon.ntrudemo.wordCheck;

import com.dragon.ntrudemo.wordCheck.knife.Collector;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * WordToken为{@link Collector}的派生类,用于收集knife切分出来的词语
 *
 * @author chencongjie
 */
public class WordToken implements Collector {
    /**
     * @param tokens 存储词串
     * @param candidate 存储备选进入词串的词语
     * @param last  存储上一个被选入词串的词语
     */
    private LinkedList<Token> tokens = new LinkedList<Token>();
    private Token candidate;
    private Token last;

    /**
     * 重写{@link LinkedList}的iterator()方法
     *
     * @return Iterator<Token>
     */
    public Iterator<Token> iterator() {
        if (candidate != null) {
            this.tokens.add(candidate);
            candidate = null;
        }
        Iterator<Token> iter = this.tokens.iterator();
        this.tokens = new LinkedList<Token>();
        return iter;
    }

    public int size() {
        int s = 0;
        s = tokens.size();
        return s;
    }

    /**
     * collect方法，用于收集knife切分出来的词语
     *
     * @param word   本次从文本流中分割出来的词语
     * @param offset 词语在文本中的起始位置
     * @param end    词语在文本中的结束位置
     */

    public void collect(String word, int offset, int end) {
        Token c = candidate != null ? candidate : last;

        //collector初始化
        if (c == null) {
            candidate = new Token(word, offset, end);

            //相同前缀词取最长的
        } else if (offset == c.getOffset()) {
            if (end > c.getEnd()) {
                candidate = new Token(word, offset, end);
            }

            //前缀不同的根据词语在句子中开始与结束的位置，取最长的
        } else if (offset > c.getOffset()) {
            if (candidate != null) {
                select(candidate);
            }
            if (end > c.getEnd()) {
                candidate = new Token(word, offset, end);
            } else {
                candidate = null;
            }
        } else if (end >= c.getEnd()) {
            if (last != null && last.getOffset() >= offset
                    && last.getEnd() <= end) {
                for (Iterator<Token> iter = tokens.iterator(); iter.hasNext(); ) {
                    last = (Token) iter.next();
                    if (last.getOffset() >= offset && last.getEnd() <= end) {
                        iter.remove();
                    }
                }
            }
            last = null;
            candidate = new Token(word, offset, end);
        }
    }

    protected void select(Token t) {
        this.tokens.add(t);
        this.last = t;
    }
}

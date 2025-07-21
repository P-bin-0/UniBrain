package com.bin.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bin
 * @since 2023/10/14 16:17
 * 文本切割（Java正则表达式和StringBuilder）
 */
public class TextSplitter {

    // 正则匹配中文或英文句子结尾（句号、问号、感叹号等）
    private static final String SPLIT_REGEX = "(?<=[。！？\\\\.\\\\?\\\\!])";
    private final int chunkSize; // 每个文本块的最大长度
    private final int chunkOverlap; // 文本块之间的重叠长度

    public TextSplitter(int chunkSize, int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public List<String> splitText(String text) {
        List<String> result = new ArrayList<>();
        String[] sentences = splitBySentence(text);
        StringBuilder currentChunk = new StringBuilder();
        int currentLength = 0;
        for(String sentence : sentences){
            int sentenceLength = sentence.length();
            //如果当前块为空直接加入
            if(currentChunk.isEmpty()){
                currentChunk.append(sentence);
                currentLength = sentenceLength;
            }
            //如果当前块+新句子长度小于最大长度，直接加入
            else if(currentLength + sentenceLength <= chunkSize){
                currentChunk.append(sentence);
                currentLength += sentenceLength;
            }
            //如果当前块+新句子长度大于最大长度，将当前块加入结果，重置当前块
            else{
                result.add(currentChunk.toString());
                //重叠部分，取当前块的最后chunkOverlap个字符
                String overlap = currentChunk.length() > chunkOverlap ?
                        currentChunk.substring(currentChunk.length() - chunkOverlap) : currentChunk.toString();
                currentChunk = new StringBuilder(overlap + sentence);
                currentLength = overlap.length() + sentenceLength;
            }
        }
        if(currentChunk.length() > 0){
            result.add(currentChunk.toString());
        }
        return result;
    }

    // 按句子分割文本
    private String[] splitBySentence(String text) {
        Pattern pattern = Pattern.compile(SPLIT_REGEX);
        Matcher matcher = pattern.matcher(text);
        List<String> result = new ArrayList<>();
        int start = 0;
        while (matcher.find()) {
            String sentence = text.substring(start,matcher.end());
            if(!sentence.trim().isEmpty()){
                result.add(sentence);
            }
            start = matcher.end();
        }
        if(start < text.length()){
            String last = text.substring(start);
            if(!last.trim().isEmpty()){
                result.add(last);
            }
        }
        return result.toArray(new String[0]);
    }
    // 测试
    /*public static void main(String[] args) {
        String text = "你好，这是一个测试文本。它包含多个句子。" +
                "我们希望将它按句子切分。这是另一个句子。" +
                "最后一句话。";

        TextSplitter splitter = new TextSplitter(30, 5);
        List<String> chunks = splitter.splitText(text);

        for (int i = 0; i < chunks.size(); i++) {
            System.out.println("Chunk " + (i + 1) + ": " + chunks.get(i));
        }
    }*/
}

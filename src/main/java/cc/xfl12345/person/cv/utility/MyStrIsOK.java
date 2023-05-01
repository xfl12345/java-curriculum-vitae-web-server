/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.xfl12345.person.cv.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xfl666
 */
public class MyStrIsOK {
    public static final Pattern matchLetterAndDigitOnly = Pattern.compile("^[a-z0-9A-Z]+$");
    public static final Pattern matchLetterOnly = Pattern.compile("^[a-zA-Z]+$");
    public static final Pattern matchDigitOnly = Pattern.compile("^[0-9]+$");
    // public static final Pattern matchIntegerWithSignOnly = Pattern.compile("^[0-9]+$");

    // "^[+-]?(?:[1-9]\\d+|\\d)(\\.\\d+)?$"
    // "(?:\\.\\d+|)"   解释：要么匹配一个小数点顺带多个数字，要么不匹配任何东西
    // "(?:[1-9]\\d+|\\d)"   解释：要么匹配不以零开头的多个位的数字，要么只匹配只有个位的数字
    public static final Pattern matchNumWithSignOnly = Pattern.compile("^(?:[-](?:[1-9]\\d+|[1-9])(?:\\.\\d+|)|(?:[1-9]\\d+|\\d)(?:\\.\\d+|))$");
    public static final Pattern matchEmailAddressOnly = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+$");
    public static final Pattern matchFilename = Pattern.compile("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");

    public static final Pattern containUppercaseLetter = Pattern.compile("[A-Z]");
    public static final Pattern containLowercaseLetter = Pattern.compile("[a-z]");
    public static final Pattern containLetter = Pattern.compile("[a-zA-Z]");
    public static final Pattern containNum = Pattern.compile("\\d");
    public static final Pattern containLetterAndDigit = Pattern.compile("[a-zA-Z0-9]");

    /**
     * 匹配如下特殊符号
     * ( ) ` ~ ! @ # $ % ^ & * - _ + = | { } [ ] : ; ' < > , . ? /
     */
    public static final Pattern containAllowedSpecialCharacter = Pattern.compile("[`~!@#$%^&*()+=|{}':;,\\[\\].\\\\<>/?—]");
    public static final Pattern containChineseInUTF8 = Pattern.compile("[\u4e00-\u9fa5]");

    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean arrIsOK(Collection<String> al) {
        boolean flag = true;
        for (String str : al) {
            if (str == null || str.isEmpty()) {
                flag = false;
                break;
            }
        }
        return flag;
    }


    public static boolean isLetterDigitOnly(String str) {
        if (isEmpty(str))
            return false;
        return matchLetterAndDigitOnly.matcher(str).find();
    }

    public static boolean isLetterOnly(String str) {
        if (isEmpty(str))
            return false;
        return matchLetterOnly.matcher(str).find();
    }

    public static boolean isDigitOnly(String str) {
        if (isEmpty(str))
            return false;
        return matchDigitOnly.matcher(str).find();
    }

    public static boolean isEmailAddress(String str) {
        if (isEmpty(str))
            return false;
        return matchEmailAddressOnly.matcher(str).find();
    }


    public static boolean isContainUppercaseLetter(String str) {
        if (isEmpty(str))
            return false;
        return containUppercaseLetter.matcher(str).find();
    }

    public static boolean isContainLowercaseLetter(String str) {
        if (isEmpty(str))
            return false;
        return containLowercaseLetter.matcher(str).find();
    }

    public static boolean isContainNum(String str) {
        if (isEmpty(str))
            return false;
        return containNum.matcher(str).find();
    }

    public static boolean isContainAllowedSpecialCharacter(String str) {
        if (isEmpty(str))
            return false;
        return containAllowedSpecialCharacter.matcher(str).find();
    }

    public static boolean isContainChineseInUTF8(String str) {
        if (isEmpty(str))
            return false;
        return containChineseInUTF8.matcher(str).find();
    }


    public static String removeMatchedChar(Pattern pattern, String content) {
        return pattern.matcher(content).replaceAll("").trim();
    }

    /**
     * 去除字符串中的数字
     *
     * @param str 给我一个字符串
     * @return 还你一个没有数字的字符串
     */
    public static String removeNum(String str) {
        return removeMatchedChar(containNum, str);
    }

    /**
     * 去除字符串中的小写英文字母
     *
     * @param str 给我一个字符串
     * @return 还你一个没有小写英文字母的字符串
     */
    public static String removeLowercaseLetter(String str) {
        return removeMatchedChar(containLowercaseLetter, str);
    }

    /**
     * 去除字符串中的大写英文字母
     *
     * @param str 给我一个字符串
     * @return 还你一个没有大写英文字母的字符串
     */
    public static String removeUppercaseLetter(String str) {
        return removeMatchedChar(containUppercaseLetter, str);
    }

    /**
     * 去除字符串中的英文字母
     *
     * @param str 给我一个字符串
     * @return 还你一个没有英文字母的字符串
     */
    public static String removeLetter(String str) {
        return removeMatchedChar(containLetter, str);
    }

    /**
     * 去除字符串中的英文字母和阿拉伯数字
     *
     * @param str 给我一个字符串
     * @return 还你一个没有英文字母、没有阿拉伯数字的字符串
     */
    public static String removeLetterAndDigit(String str) {
        return removeMatchedChar(containLetterAndDigit, str);
    }

    /**
     * 去除字符串中的 合法的特殊字符
     *
     * @param str 给我一个字符串
     * @return 还你一个没有合法的特殊字符的字符串
     */
    public static String removeAllowedSpecialCharacter(String str) {
        return removeMatchedChar(containAllowedSpecialCharacter, str);
    }


    /**
     * 从字符串里提取邮箱地址
     *
     * @param str 输入字符串
     * @return 以数组的形式，返回str字符串中包含的所有邮箱地址
     */
    public static ArrayList<String> getEmailFromString(String str) {
        ArrayList<String> email = new ArrayList<>();
        Matcher m = matchEmailAddressOnly.matcher(str);
        while (m.find()) {
            email.add(m.group());
        }
        return email;
    }

    /**
     * 判断一个文件名是否合法（目录名称通用）
     *
     * @param filename 文件名
     * @return 是否合法
     */
    public static boolean isValidFileName(String filename) {
        if (filename == null || "".equals(filename) || filename.length() > 255)
            return false;
        else
            return matchFilename.matcher(filename).find();
    }

}

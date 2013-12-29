/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cafeform.iumfs;

import com.cafeform.iumfs.twitterfs.MessageSeparator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * 文字列を 140 文字づつステータスとしてポスト.
 * 二回目以降の next() には「(cont)」という文字を先頭につけて
 * つづきの文章であることを表す。
 */
public class MessageSeparatorTest {
    String whole_msg = "Java（ジャバ）は、狭義ではオブジェクト指向プログラミング言語Javaであり、広義ではプログラミング言語Javaのプログラムの実行環境および開発環境をいう。本稿ではプログラミング言語としてのJava、および関連する技術や設計思想、およびJava言語の実行環境としてみたJavaプラットフォームについて解説する。クラスライブラリなどを含めた、Javaバイトコードの実行環境と開発環境（広義のJava）については、Javaプラットフォームを参照。また、言語の文法に関してはJavaの文法を参照。";
    String first_part = "Java（ジャバ）は、狭義ではオブジェクト指向プログラミング言語Javaであり、広義ではプログラミング言語Javaのプログラムの実行環境および開発環境をいう。本稿ではプログラミング言語としてのJava、および関連する技術や設計思想、およびJava言語の実行環境としてみたJavaプ";
    String second_part = "(contd) ラットフォームについて解説する。クラスライブラリなどを含めた、Javaバイトコードの実行環境と開発環境（広義のJava）については、Javaプラットフォームを参照。また、言語の文法に関してはJavaの文法を参照。";
    
    public MessageSeparatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of hasNext method, of class MessageSeparator.
     */
    @Test
    public void test() {
        
            MessageSeparator sep = new MessageSeparator(whole_msg);

            while (sep.hasNext()) {
                String msg = (String)sep.next();

                assertTrue((msg.length() == 140 && first_part.equals(msg)) 
                        || msg.length() == 114 && second_part.equals(msg));
                System.out.println("--------------------");
                System.out.println(msg.length() + " characters");
                System.out.println(msg);
            }        
    }
}

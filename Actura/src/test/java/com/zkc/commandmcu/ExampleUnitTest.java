package com.zkc.commandmcu;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);

        try{
            //some work
            IDModels models= new IDModels();
            //doSomeWork(models);
            assertTrue(models.getEPC()==100);
        }
        catch(Exception ex){
            assertTrue(ex != null);
        }
    }
}